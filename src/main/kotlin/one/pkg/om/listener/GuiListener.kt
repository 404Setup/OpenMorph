/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.GuiHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class GuiListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? GuiHolder ?: return
        event.isCancelled = true

        val player = event.whoClicked as? Player ?: return
        val clickedInventory = event.clickedInventory ?: return

        if (clickedInventory == event.inventory) {
            val action = holder.actions[event.rawSlot]
            action?.invoke(player)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        if (event.inventory.holder is GuiHolder) {
            event.isCancelled = true
        }
    }
}
