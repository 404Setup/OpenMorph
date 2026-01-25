/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.OManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeath : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.entity
        val data = OManager.playerMorph[player]
        if (data?.current != null) {
            if (data.current is MorphEntity)
                OManager.pendingRespawnResets.add(player.uniqueId)
            data.current?.stop()
            data.current = null
            data.offlineData.clearActiveMorph()
        }
    }
}
