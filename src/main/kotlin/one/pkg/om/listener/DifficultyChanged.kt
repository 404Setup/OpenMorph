/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import io.papermc.paper.event.world.WorldDifficultyChangeEvent
import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.OManager
import one.pkg.om.utils.sendWarning
import org.bukkit.Difficulty
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DifficultyChanged : Listener {
    @EventHandler
    fun onDifficultyChanged(event: WorldDifficultyChangeEvent) {
        val world = event.world
        OManager.playerMorph.forEach {
            if (it.key.world == world) {
                val current = it.value.current
                current?.let { entities ->
                    if (entities !is MorphEntity) return
                    if (event.difficulty == Difficulty.PEACEFUL && entities.isEnemy()) {
                        it.key.sendWarning("Due to changes in world difficulty, the current disguise has been removed.")
                        entities.stop()
                        it.value.current = null
                        it.value.offlineData.clearActiveMorph()
                    }
                }
            }
        }
    }
}