/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import one.pkg.om.OmMain
import one.pkg.om.manager.OManager
import one.pkg.om.utils.runAs
import one.pkg.om.utils.scheduleResetHealth
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

    open val hasKnockback: Boolean = true
    open val skills = mutableMapOf<Int, (Player) -> Unit>()
    open val passiveSkills = mutableListOf<() -> Unit>()

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
                    NamespacedKey(OmMain.getInstance(), "om_owner"),
                    PersistentDataType.STRING
                )
            ) {
                val uuidStr = e.persistentDataContainer.get(
                    NamespacedKey(OmMain.getInstance(), "om_owner"),
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
                NamespacedKey(OmMain.getInstance(), "om_owner"),
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
        syncLocation()
    }

    private fun syncLocation() {
        val entity = disguisedEntity ?: return
        if (!entity.isValid) return

        val loc = player.location
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
        if (disguisedEntity == null || !disguisedEntity!!.isValid) {
            spawnDisguise()
            if (disguisedEntity == null) return
        }
        val entity = disguisedEntity ?: return
        if (!entity.isValid) {
            return
        }

        syncLocation()

        entity.runAs { _ ->
            if (entity is LivingEntity) {
                val attr = Attribute.MAX_HEALTH
                val targetHealth = player.health.coerceAtMost(entity.getAttribute(attr)?.value ?: 20.0)
                if (entity.health != targetHealth) {
                    entity.health = targetHealth
                }

                if (player.pose != entity.pose) {
                    // TODO: Implement pose synchronization
                }
            }

            passiveSkills.forEach { it.invoke() }
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (!hasKnockback && !event.isCancelled) {
            // TODO: Implement knockback handling
        }
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        swingHand()
    }

    fun swingHand() {
        if (disguisedEntity != null && disguisedEntity is LivingEntity)
            (disguisedEntity as LivingEntity).swingMainHand()
    }

    fun useSkill(id: Int) {
        skills[id]?.invoke(player)
    }
}