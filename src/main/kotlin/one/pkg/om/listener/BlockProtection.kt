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
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityExplodeEvent

class BlockProtection : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPistonExtend(e: BlockPistonExtendEvent) = checkPiston(e, e.block.world.name, e.blocks)

    @EventHandler(ignoreCancelled = true)
    fun onPistonRetract(e: BlockPistonRetractEvent) = checkPiston(e, e.block.world.name, e.blocks)

    private fun checkPiston(e: Cancellable, worldName: String, blocks: List<Block>) {
        if (blocks.any { OManager.blockMorphs.containsKey(BlockPosition(worldName, it.x, it.y, it.z)) }) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityExplode(e: EntityExplodeEvent) = checkExplosion(e.blockList(), e.location.world?.name)

    @EventHandler(ignoreCancelled = true)
    fun onBlockExplode(e: BlockExplodeEvent) = checkExplosion(e.blockList(), e.block.world.name)

    private fun checkExplosion(blockList: MutableList<Block>, worldName: String?) {
        if (worldName == null) return
        val iter = blockList.iterator()
        while (iter.hasNext()) {
            val block = iter.next()
            val player = OManager.blockMorphs[BlockPosition(worldName, block.x, block.y, block.z)]
            if (player != null) {
                iter.remove()
                (OManager.playerMorph[player]?.current as? MorphBlock)?.takeIf { it.isAt(block.location) }?.onDamage()
            }
        }
    }
}
