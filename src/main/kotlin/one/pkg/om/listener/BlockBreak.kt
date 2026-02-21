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
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreak : Listener {
    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        val loc = e.block.location
        // Optimization: Use O(1) lookup map instead of iterating all morphed players (O(N))
        val pos = BlockPosition(e.block.world.name, loc.blockX, loc.blockY, loc.blockZ)

        val player = OManager.blockMorphs[pos] ?: return
        val data = OManager.playerMorph[player] ?: return

        val current = data.current
        if (current is MorphBlock) {
             if (current.isAt(loc)) {
                 current.onDamage()
                 player.damage(5.0)
                 e.isDropItems = false
             }
        }
    }
}
