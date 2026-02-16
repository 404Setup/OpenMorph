/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import one.pkg.om.OmMain
import one.pkg.om.manager.HostilityManager
import one.pkg.om.manager.OManager
import one.pkg.om.utils.OmKeys
import one.pkg.om.utils.runAs
import one.pkg.om.utils.scheduleResetHealth
import one.pkg.om.utils.sendWarning
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.loot.Lootable
import org.bukkit.persistence.PersistentDataType

open class MorphEntity(player: Player, val entityType: EntityType) : MorphEntities(player) {
    var disguisedEntity: Entity? = null
    private var lastSyncedLocation: Location? = null
    private var isStopped = false
    private var tickCounter = player.entityId

    open val hasKnockback: Boolean = true
    open val skills = mutableMapOf<Int, (Player) -> Unit>()
    open val passiveSkills = mutableListOf<() -> Unit>()
    open val skillCooldowns = mutableMapOf<Int, Long>()
    private val lastSkillUsage = mutableMapOf<Int, Long>()

    override fun start() {
        cleanupGhosts()
        spawnDisguise()

        if (!hasKnockback) {
            player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 1.0
        }

        Bukkit.getOnlinePlayers().forEach {
            if (it != player) it.hideEntity(OmMain.getInstance(), player)
        }
    }

    fun isEnemy(): Boolean {
        val clazz = entityType.entityClass
        return clazz != null && Enemy::class.java.isAssignableFrom(clazz)
    }

    private fun cleanupGhosts() {
        player.getNearbyEntities(10.0, 10.0, 10.0).forEach { e ->
            if (e.persistentDataContainer.has(
                    OmKeys.OWNER_KEY,
                    PersistentDataType.STRING
                )
            ) {
                val uuidStr = e.persistentDataContainer.get(
                    OmKeys.OWNER_KEY,
                    PersistentDataType.STRING
                )
                if (uuidStr == player.uniqueId.toString()) {
                    if (e != disguisedEntity) {
                        e.remove()
                    }
                }
            }
        }
    }

    private fun spawnDisguise() {
        if (disguisedEntity != null && disguisedEntity!!.isValid) return

        disguisedEntity?.let {
            OManager.entityToPlayerMap.remove(it.uniqueId)
            it.remove()
        }

        cleanupGhosts()

        val loc = player.location
        disguisedEntity = player.world.spawnEntity(loc, entityType)
        disguisedEntity?.let { entity ->
            entity.isSilent = true
            entity.isInvulnerable = false
            entity.isPersistent = false

            entity.persistentDataContainer.set(
                OmKeys.OWNER_KEY,
                PersistentDataType.STRING,
                player.uniqueId.toString()
            )

            if (entity is LivingEntity) {
                entity.isCollidable = false
                entity.setAI(false)
                entity.removeWhenFarAway = false
                entity.isInvisible = false

                if (entity is Lootable) entity.lootTable = null

                val attr = Attribute.MAX_HEALTH
                val maxHealth = entity.getAttribute(attr)?.value ?: 20.0
                player.getAttribute(attr)?.baseValue = maxHealth
                player.health = player.health.coerceAtMost(maxHealth)
                player.healthScale = maxHealth
                player.sendHealthUpdate()
            }
            OManager.entityToPlayerMap[entity.uniqueId] = player
            player.hideEntity(OmMain.getInstance(), entity)
            onEntitySpawned(entity)

            OManager.playerMorph[player]?.offlineData?.setActiveMorphEntity(entity.uniqueId)
        }
    }

    open fun onEntitySpawned(entity: Entity) {}

    override fun updateInventory() {
        if (!canUpdateInventory()) return
        disguisedEntity?.let {
            if (disguisedEntity is LivingEntity) {
                val livingEntity = disguisedEntity as LivingEntity
                livingEntity.equipment?.let {
                    it.armorContents = player.inventory.armorContents
                    it.setItemInMainHand(player.inventory.itemInMainHand)
                    it.setItemInOffHand(player.inventory.itemInOffHand)
                }
            }
        }
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        isStopped = true
        val data = OManager.playerMorph[player]
        data?.offlineData?.activeMorphEntityUuid?.let {
            OManager.entityToPlayerMap.remove(it)
            player.world.getEntity(it)?.let { entity ->
                if (entity != disguisedEntity) entity.remove()
            }
            if (clearData) data.offlineData.clearActiveMorphEntity(true)
        }

        disguisedEntity?.let {
            OManager.entityToPlayerMap.remove(it.uniqueId)
            try {
                it.remove()
            } catch (_: Exception) {
            }
        }
        disguisedEntity = null

        cleanupGhosts()

        Bukkit.getOnlinePlayers().forEach {
            if (it != player) it.showEntity(OmMain.getInstance(), player)
        }

        player.sendHealthUpdate()
        player.scheduleResetHealth(stopServer = stopServer)
    }

    override fun onMove(event: PlayerMoveEvent) {
        syncLocation(event.to)
    }

    private fun syncLocation(targetLoc: Location? = null) {
        val entity = disguisedEntity ?: return
        if (!entity.isValid) return

        val loc = targetLoc ?: player.location
        val last = lastSyncedLocation
        if (last != null &&
            last.world == loc.world &&
            last.x == loc.x &&
            last.y == loc.y &&
            last.z == loc.z &&
            last.yaw == loc.yaw &&
            last.pitch == loc.pitch
        ) return

        entity.teleportAsync(loc)
        lastSyncedLocation = loc
    }

    override fun tick() {
        if (isStopped) return

        val currentTick = tickCounter++

        if (currentTick % 40 == 0) {
            var count = 0
            // Optimization: Limit collection size at source to avoid large allocations and iterations
            // when many mobs are nearby (e.g. mob farms).
            player.world.getNearbyEntities(player.boundingBox.expand(15.0, 15.0, 15.0)) {
                if (count >= 50) return@getNearbyEntities false
                if (it is Mob && it.target == null && HostilityManager.shouldAttack(it.type, entityType)) {
                    count++
                    return@getNearbyEntities true
                }
                false
            }.forEach { entity ->
                (entity as Mob).target = player
            }
        }

        // Throttle tick execution if there are no passive skills to run.
        // Location sync is handled by PlayerMoveEvent, so this only affects
        // health/pose sync and disguise respawn, which don't need 20Hz updates.
        // This reduces scheduler overhead by 80%.
        if (passiveSkills.isEmpty() && currentTick % 5 != 0) return

        if (disguisedEntity == null || !disguisedEntity!!.isValid) {
            spawnDisguise()
            if (disguisedEntity == null) return
        }
        val entity = disguisedEntity ?: return
        if (!entity.isValid) {
            return
        }

        // Location sync is handled by onMove with MONITOR priority

        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            syncEntityState(entity)
        } else {
            entity.runAs { _ -> syncEntityState(entity) }
        }
    }

    private fun syncEntityState(entity: Entity) {
        if (entity is LivingEntity) {
            val attr = Attribute.MAX_HEALTH
            val targetHealth = player.health.coerceAtMost(entity.getAttribute(attr)?.value ?: 20.0)
            if (entity.health != targetHealth) {
                entity.health = targetHealth
            }

            if (player.pose != entity.pose) {
                entity.pose = player.pose
            }
        }

        if (passiveSkills.isNotEmpty()) {
            passiveSkills.forEach { it.invoke() }
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        swingHand()
    }

    fun swingHand() {
        if (disguisedEntity != null && disguisedEntity is LivingEntity)
            (disguisedEntity as LivingEntity).swingMainHand()
    }

    fun useSkill(id: Int) {
        val now = System.currentTimeMillis()
        val cooldown = skillCooldowns[id] ?: 0L
        val last = lastSkillUsage[id] ?: 0L

        if (now - last < cooldown) {
            player.sendWarning("Skill on cooldown: %.1fs".format((cooldown - (now - last)) / 1000.0))
            return
        }

        if (skills.containsKey(id)) {
            skills[id]?.invoke(player)
            lastSkillUsage[id] = now
        }
    }
}