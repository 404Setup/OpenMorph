/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import one.pkg.om.manager.OManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent


class PlayerInvChange : Listener {
    @EventHandler
    fun onPlayerArmorChange(event: EntityEquipmentChangedEvent) {
        val player = event.entity as? Player ?: return
        OManager.playerMorph[player]?.let {
            val c = it.current
            if (c != null && c.canUpdateInventory()) {
                c.updateInventory()
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerSwapHand(e: PlayerSwapHandItemsEvent) {
        val player = e.getPlayer()
        OManager.playerMorph[player]?.let {
            val c = it.current
            if (c != null && c.canUpdateInventory()) {
                c.updateInventory()
            }
        }
    }
}