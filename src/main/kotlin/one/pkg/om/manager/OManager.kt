/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.manager

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import one.pkg.om.data.OnlineMorphData
import one.pkg.om.utils.runAsync
import one.pkg.om.utils.runGlobalTask
import one.pkg.om.utils.runTaskLater
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object OManager {
    val playerMorph = linkedMapOf<Player, OnlineMorphData>()
    val entityToPlayerMap = ConcurrentHashMap<UUID, Player>()
    val pendingRespawnResets: ConcurrentHashMap.KeySetView<UUID?, Boolean> = ConcurrentHashMap.newKeySet<UUID>()
    private val pendingRemoval = ConcurrentHashMap<UUID, ScheduledTask>()

    fun cancelRemoval(player: Player) {
        pendingRemoval.remove(player.uniqueId)?.cancel()
    }

    fun scheduleRemoval(player: Player) {
        cancelRemoval(player)
        val task = runTaskLater(45 * 20L) {
            pendingRemoval.remove(player.uniqueId)
            savePlayer(player, false)
            playerMorph.remove(player)
        }
        pendingRemoval[player.uniqueId] = task
    }

    fun saveAll() {
        val snapshots = playerMorph.values.mapNotNull { it.offlineData.getSnapshotAndClearDirty() }

        if (snapshots.isEmpty()) return

        runAsync {
            snapshots.forEach { it.saveToDisk() }
            runGlobalTask {
                playerMorph.keys.removeIf { player -> !player.isConnected }
            }
        }
    }

    fun savePlayer(player: Player, sync: Boolean = false) {
        val data = playerMorph[player] ?: return
        val snapshot = data.offlineData.getSnapshotAndClearDirty() ?: return

        if (sync) {
            snapshot.saveToDisk()
        } else {
            runAsync {
                snapshot.saveToDisk()
            }
        }
    }

    fun saveAllSync() {
        playerMorph.values.forEach { it.offlineData.saveMe() }
    }
}
