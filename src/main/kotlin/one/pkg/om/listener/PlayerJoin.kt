/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.OmMain
import one.pkg.om.entities.*
import one.pkg.om.manager.OManager
import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.utils.OmKeys
import one.pkg.om.utils.runDelayed
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType

class PlayerJoin : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.runDelayed(1L) {
            if (!player.isOnline) return@runDelayed
            val current = OManager.playerMorph[player]?.current
            player.getNearbyEntities(15.0, 15.0, 15.0).forEach { e ->
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
                        if (!isSameEntity(e, current)) {
                            e.remove()
                        }
                    }
                }
            }
        }

        OManager.cancelRemoval(player)

        var onlineData = OManager.playerMorph[player]
        val saveMorphData: SaveMorphData

        if (onlineData != null) {
            onlineData.offlineData.updatePlayer(player)
            OManager.playerMorph.remove(player)
            OManager.playerMorph[player] = onlineData
            saveMorphData = onlineData.offlineData
        } else {
            saveMorphData = SaveMorphData.create(player)
            onlineData = OnlineMorphData(null, saveMorphData)
            OManager.playerMorph[player] = onlineData
        }

        val ghostUuid = saveMorphData.activeMorphEntityUuid
        if (ghostUuid != null) {
            val ghost = Bukkit.getEntity(ghostUuid)
            ghost?.remove()
            saveMorphData.clearActiveMorphEntity()
        }

        val solidified = saveMorphData.solidifiedBlockParams
        var blockRemoved = false
        if (solidified != null) {
            val parts = solidified.split(";")
            if (parts.size == 4) {
                val wName = parts[0]
                val x = parts[1].toIntOrNull()
                val y = parts[2].toIntOrNull()
                val z = parts[3].toIntOrNull()
                if (x != null && y != null && z != null) {
                    val world = Bukkit.getWorld(wName)
                    if (world != null) {
                        try {
                            val block = world.getBlockAt(x, y, z)
                            block.type = Material.AIR
                            blockRemoved = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    blockRemoved = true
                }
            } else {
                blockRemoved = true
            }

            val forcedMode = saveMorphData.forcedKeyGameMode
            if (forcedMode != null) {
                val mode = runCatching { GameMode.valueOf(forcedMode) }.getOrNull()
                if (mode != null) {
                    player.gameMode = mode
                }
            } else {
                if (player.gameMode == GameMode.SPECTATOR) {
                    player.gameMode = GameMode.SURVIVAL
                }
            }

            if (blockRemoved) {
                saveMorphData.clearSolidifiedBlock()
            }
        }

        val type = saveMorphData.activeMorphType
        val name = saveMorphData.activeMorphName
        if (type != null && name != null) {
            val morph = when (type) {
                "ENTITY" -> {
                    val et = runCatching { EntityType.fromName(name) }.getOrNull()
                    if (et != null) MorphFactory.create(player, et) else null
                }

                "BLOCK" -> {
                    val mat = runCatching { Material.getMaterial(name) }.getOrNull()
                    if (mat != null) MorphBlock(player, mat) else null
                }

                "PLAYER" -> {
                    MorphPlayer(player, name, saveMorphData.activeMorphSkin, saveMorphData.activeMorphSignature)
                }

                else -> null
            }
            if (morph != null) {
                onlineData.current = morph
                morph.start()
            }
        }

        OManager.playerMorph.forEach { (morphedPlayer, data) ->
            if (data.current is MorphEntity) {
                player.hideEntity(OmMain.getInstance(), morphedPlayer)
            }
        }

        player.runDelayed(20L) {
            if (!player.isOnline) return@runDelayed
            
            val retrySolidified = saveMorphData.solidifiedBlockParams
            if (retrySolidified != null) {
                val parts = retrySolidified.split(";")
                if (parts.size == 4) {
                    val wName = parts[0]
                    val x = parts[1].toIntOrNull()
                    val y = parts[2].toIntOrNull()
                    val z = parts[3].toIntOrNull()
                    if (x != null && y != null && z != null) {
                        val world = Bukkit.getWorld(wName)
                        if (world != null) {
                            try {
                                val block = world.getBlockAt(x, y, z)
                                block.type = Material.AIR
                                saveMorphData.clearSolidifiedBlock()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        saveMorphData.clearSolidifiedBlock()
                    }
                } else {
                    saveMorphData.clearSolidifiedBlock()
                }
            }

            val current = OManager.playerMorph[player]?.current

            if (ghostUuid != null) {
                val ghost = Bukkit.getEntity(ghostUuid)
                if (ghost != null && !isSameEntity(ghost, current)) {
                    ghost.remove()
                }
            }

            player.getNearbyEntities(20.0, 20.0, 20.0).forEach { e ->
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
                        if (!isSameEntity(e, current)) {
                            e.remove()
                        }
                    }
                }
            }
        }
    }

    private fun isSameEntity(entity: Entity, morph: MorphEntities?): Boolean {
        if (morph == null) return false
        if (morph is MorphEntity) {
            return morph.disguisedEntity == entity
        }
        if (morph is MorphBlock) {
            return morph.displayEntity == entity || morph.interactionEntity == entity
        }
        return false
    }
}