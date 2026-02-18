/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import one.pkg.om.utils.runAtFixedRate
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent

abstract class MorphEntities(val player: Player) {
    private var task: ScheduledTask? = null

    open fun start() {
        if (task == null || task!!.isCancelled) {
            task = player.runAtFixedRate(1L, 1L) {
                this.tick()
            }
        }
    }

    open fun stop(clearData: Boolean = true, stopServer: Boolean = false) {
        task?.cancel()
        task = null
    }

    open fun onMove(event: PlayerMoveEvent) {}
    open fun onDamage(event: EntityDamageEvent) {}
    open fun onAttack(event: EntityDamageByEntityEvent) {}
    open fun tick() {}
    open fun canUpdateInventory(): Boolean = false
    open fun updateInventory() {}

}
