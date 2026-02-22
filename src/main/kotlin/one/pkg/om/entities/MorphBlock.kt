/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import one.pkg.om.manager.BlockPosition
import one.pkg.om.manager.OManager
import one.pkg.om.utils.OmKeys
import one.pkg.om.utils.RestrictedBlocks
import one.pkg.om.utils.isIt
import one.pkg.om.utils.runAs
import one.pkg.om.utils.sendFailed
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
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
    // Optimization: Track last synced location to avoid redundant teleport packets
    private var lastSyncedLocation: Location? = null
    // Reuse location object to avoid allocations in tick loop
    private val scratchLocation = Location(player.world, 0.0, 0.0, 0.0)

    override fun start() {
        super.start()
        isRunning = true
        spawnDisplay()
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false))

        setSelfVisible(false)
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        super.stop(clearData, stopServer)
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

        setSelfVisible(true)
    }

    override fun tick() {
        if (!isRunning) return
        if (!isSolidified) {
            idleTicks++
            if (idleTicks >= 120 && idleTicks % 20 == 0) {
                trySolidify()
            }
            // Optimization: Remove redundant syncLocation() call.
            // Location updates are handled by onMove with MONITOR priority,
            // avoiding unnecessary player.getLocation() calls every tick when stationary.
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
            syncLocation(to)
        } else {
            if (!isSolidified) {
                syncLocation(to)
            }
        }
    }

    private fun syncLocation(target: Location? = null) {
        if (!isSolidified) {
            if (target != null) {
                // Optimization: Reuse event location to avoid expensive player.getLocation() call
                scratchLocation.world = target.world
                scratchLocation.x = target.x
                scratchLocation.y = target.y
                scratchLocation.z = target.z
                scratchLocation.yaw = target.yaw
                scratchLocation.pitch = 0f
            } else {
                player.getLocation(scratchLocation)
                scratchLocation.pitch = 0f
            }

            val last = lastSyncedLocation
            if (last != null &&
                last.world == scratchLocation.world &&
                last.x == scratchLocation.x &&
                last.y == scratchLocation.y &&
                last.z == scratchLocation.z &&
                last.yaw == scratchLocation.yaw &&
                last.pitch == scratchLocation.pitch
            ) return

            val loc = scratchLocation.clone()
            displayEntity?.teleportAsync(loc)
            interactionEntity?.teleportAsync(loc)
            lastSyncedLocation = loc
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

        // Optimization: Reuse scratchLocation to avoid allocating new Location objects
        player.getLocation(scratchLocation)
        val legs = scratchLocation.block
        val ground = legs.getRelative(BlockFace.DOWN)

        if (!player.hasPermission("om.morph.block.bypass_ground_check")) {
            if (ground.type.isAir) return
        }

        if (!legs.isReplaceable) return

        val centerLoc = legs.location.add(0.5, 0.0, 0.5)
        centerLoc.yaw = scratchLocation.yaw
        centerLoc.pitch = scratchLocation.pitch

        if (previousGameMode == null) {
            previousGameMode = player.gameMode
        }

        player.teleportAsync(centerLoc).thenRun {
            if (!isRunning) return@thenRun
            player.runAs { _ ->
                // Security fix: Re-validate block state to prevent race conditions (TOCTOU)
                if (!legs.isReplaceable) {
                    player.sendFailed("Cannot solidify: Block is obstructed.")
                    return@runAs
                }

                if (RestrictedBlocks.isRestricted(material) && !player.hasPermission("om.morph.block.bypass_restricted")) {
                    player.sendFailed("You cannot solidify as this block!")
                    return@runAs
                }

                // Security: Fire BlockPlaceEvent to check for build permissions and region protection
                // Fix: Temporarily set block type so event.getBlock() reflects the new block
                val replacedState = legs.state
                legs.setType(material, false)

                var success = false
                try {
                    val event = BlockPlaceEvent(
                        legs,
                        replacedState,
                        ground,
                        ItemStack(material),
                        player,
                        true,
                        EquipmentSlot.HAND
                    )
                    Bukkit.getPluginManager().callEvent(event)

                    if (!event.isCancelled && event.canBuild()) {
                        success = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (!success) {
                        replacedState.update(true, false) // Revert changes
                        player.sendFailed("Cannot solidify: Build permission denied.")
                        return@runAs
                    }
                }

                player.gameMode = GameMode.SPECTATOR
                solidifiedLocation = legs.location
                isSolidified = true
                removeDisplay()
                OManager.playerMorph[player]?.offlineData?.setSolidifiedBlock(
                    legs.world.name,
                    legs.x,
                    legs.y,
                    legs.z,
                    previousGameMode?.name ?: "SURVIVAL"
                )
                OManager.blockMorphs[BlockPosition(legs.world.name, legs.x, legs.y, legs.z)] = player
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
            OManager.blockMorphs.remove(
                BlockPosition(
                    solidifiedLocation!!.world.name,
                    solidifiedLocation!!.blockX,
                    solidifiedLocation!!.blockY,
                    solidifiedLocation!!.blockZ
                )
            )
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
