/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
import one.pkg.om.OmMain
import one.pkg.om.manager.OManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedMainHandEvent

class PlayerInvChange : Listener {
    @EventHandler
    fun onPlayerArmorChange(event: PlayerArmorChangeEvent) {
        val player = event.getPlayer()
        OManager.playerMorph[player]?.let {
            val c = it.current
            if (c != null && c.canUpdateInventory()) {
                c.updateInventory()
            }
        }
    }

    @EventHandler
    fun onPlayerChangedMainHand(event: PlayerChangedMainHandEvent) {
        OmMain.getInstance().logger.info { "Player inventory changed for ${event.player.name}" }
    }
}