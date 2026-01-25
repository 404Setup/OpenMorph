/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.OManager
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerRespawn : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onRespawn(e: PlayerRespawnEvent) {
        val player = e.player
        if (OManager.pendingRespawnResets.remove(player.uniqueId)) {
            player.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 20.0
            player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 0.0
        }
    }
}
