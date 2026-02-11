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
import one.pkg.om.utils.OmKeys
import one.pkg.om.utils.isIt
import one.pkg.om.utils.runAs
import org.bukkit.*
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class MorphBlock(player: Player, val material: Material) : MorphEntities(player) {
    var displayEntity: BlockDisplay? = null
    var interactionEntity: Interaction? = null
    private var idleTicks = 0
    private var isSolidified = false
    private var solidifiedLocation: Location? = null
    private var previousGameMode: GameMode? = null
    private var isRunning = false

    override fun start() {
        isRunning = true
        spawnDisplay()
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false))

        Bukkit.getOnlinePlayers().forEach {
            if (it != player) it.hideEntity(OmMain.getInstance(), player)
        }
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        isRunning = false
        if (isSolidified) {
            revertToDisplay()
        }

        val data = OManager.playerMorph[player]
        val storedUuid = data?.offlineData?.activeMorphEntityUuid
        if (storedUuid != null) {
            val storedEntity = Bukkit.getEntity(storedUuid)
            storedEntity?.remove()
            if (clearData) {
                data.offlineData.clearActiveMorphEntity()
            }
        }

        removeDisplay()
        player.removePotionEffect(PotionEffectType.INVISIBILITY)

        Bukkit.getOnlinePlayers().forEach {
            if (it != player) it.showEntity(OmMain.getInstance(), player)
        }
    }

    override fun tick() {
        if (!isRunning) return
        if (!isSolidified) {
            idleTicks++
            if (idleTicks >= 120) {
                trySolidify()
            }
            syncLocation()
        } else {
            val loc = solidifiedLocation ?: return
            if (loc.block.type != material) {
                revertToDisplay()
            }
        }
    }

    override fun onMove(event: PlayerMoveEvent) {
        if (!isRunning) return
        val from = event.from
        val to = event.to

        if (from.x != to.x || from.y != to.y || from.z != to.z) {
            idleTicks = 0
            if (isSolidified) {
                revertToDisplay()
            }
            syncLocation()
        } else {
            if (!isSolidified) {
                syncLocation()
            }
        }
    }

    private fun syncLocation() {
        if (!isSolidified) {
            val loc = player.location.clone()
            loc.pitch = 0f
            displayEntity?.teleportAsync(loc)
            interactionEntity?.teleportAsync(loc)
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        onDamage()
    }

    fun onDamage() {
        if (isSolidified) {
            revertToDisplay()
        }
    }

    private fun spawnDisplay() {
        if (displayEntity != null) return
        try {
            val spawnLoc = player.location.clone()
            spawnLoc.pitch = 0f

            displayEntity = player.world.spawn(spawnLoc, BlockDisplay::class.java) {
                it.block = material.createBlockData()
                it.isPersistent = false
                it.persistentDataContainer.set(
                    OmKeys.OWNER_KEY,
                    PersistentDataType.STRING,
                    player.uniqueId.toString()
                )

                val transformation = it.transformation
                transformation.translation.set(-0.5f, 0.0f, -0.5f)
                it.transformation = transformation
            }

            interactionEntity = player.world.spawn(spawnLoc, Interaction::class.java) {
                it.interactionWidth = 1.0f
                it.interactionHeight = 1.0f
                it.isPersistent = false
                it.persistentDataContainer.set(
                    OmKeys.OWNER_KEY,
                    PersistentDataType.STRING,
                    player.uniqueId.toString()
                )
            }
            interactionEntity?.let {
                OManager.entityToPlayerMap[it.uniqueId] = player
            }

            displayEntity?.let {
                OManager.playerMorph[player]?.offlineData?.setActiveMorphEntity(it.uniqueId)
            }
        } catch (_: NoClassDefFoundError) {
        }
    }

    private fun removeDisplay() {
        displayEntity?.remove()
        displayEntity = null

        interactionEntity?.let {
            OManager.entityToPlayerMap.remove(it.uniqueId)
            it.remove()
        }
        interactionEntity = null

        OManager.playerMorph[player]?.offlineData?.clearActiveMorphEntity()
    }

    private fun trySolidify() {
        if (isSolidified || !isRunning) return

        val legs = player.location.block
        val ground = player.location.clone().subtract(0.0, 1.0, 0.0).block

        if (!player.hasPermission("om.morph.block.bypass_ground_check")) {
            if (ground.type.isAir) return
        }

        if (!legs.isReplaceable) return

        val centerLoc = legs.location.clone().add(0.5, 0.0, 0.5)
        centerLoc.yaw = player.location.yaw
        centerLoc.pitch = player.location.pitch

        if (previousGameMode == null) {
            previousGameMode = player.gameMode
        }

        player.teleportAsync(centerLoc).thenRun {
            if (!isRunning) return@thenRun
            player.runAs { _ ->
                player.gameMode = GameMode.SPECTATOR
                solidifiedLocation = legs.location
                legs.setType(material, false)
                isSolidified = true
                removeDisplay()
                OManager.playerMorph[player]?.offlineData?.setSolidifiedBlock(
                    legs.world.name,
                    legs.x,
                    legs.y,
                    legs.z,
                    previousGameMode?.name ?: "SURVIVAL"
                )
            }
        }
    }

    private fun revertToDisplay() {
        idleTicks = 0
        if (isSolidified && solidifiedLocation != null) {
            var success = false
            try {
                if (solidifiedLocation!!.block.type == material) {
                    solidifiedLocation!!.block.type = Material.AIR
                }
                success = true
            } catch (_: Exception) {
            }

            if (success) {
                OManager.playerMorph[player]?.offlineData?.clearSolidifiedBlock()
            }
            solidifiedLocation = null
        }

        if (previousGameMode != null) {
            player.gameMode = previousGameMode!!
            previousGameMode = null
        }

        isSolidified = false
        if (isRunning) {
            spawnDisplay()
        }
    }

    fun isAt(loc: Location): Boolean {
        val solidLoc = solidifiedLocation ?: return false
        return solidLoc.blockX == loc.blockX && solidLoc.blockY == loc.blockY && solidLoc.blockZ == loc.blockZ && solidLoc.world == loc.world
    }
}
