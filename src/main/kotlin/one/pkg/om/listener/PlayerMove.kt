/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.OManager
import one.pkg.om.utils.isIt
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMove : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val from = e.from
        val to = e.to
        if (from.isIt(to)) return

        val data = OManager.playerMorph[e.player] ?: return
        data.onMove(e)
    }
}