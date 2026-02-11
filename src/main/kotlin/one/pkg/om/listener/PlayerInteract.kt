/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.entities.MorphBlock
import one.pkg.om.manager.BlockPosition
import one.pkg.om.manager.OManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class PlayerInteract : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onInteract(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) return
        if (e.action != Action.LEFT_CLICK_BLOCK) return
        
        val block = e.clickedBlock ?: return
        val loc = block.location

        val pos = BlockPosition(loc.world.name, loc.blockX, loc.blockY, loc.blockZ)
        val p = OManager.blockMorphs[pos] ?: return
        val data = OManager.playerMorph[p] ?: return
        val current = data.current
        if (current is MorphBlock) {
            if (current.isAt(loc)) {
                current.onDamage()
                e.player.attack(p)
                return
            }
        }
    }
}
