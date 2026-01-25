/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent

abstract class MorphEntities(val player: Player) {
    abstract fun start()
    abstract fun stop(clearData: Boolean = true, stopServer: Boolean = false)
    open fun onMove(event: PlayerMoveEvent) {}
    open fun onDamage(event: EntityDamageEvent) {}
    open fun onAttack(event: EntityDamageByEntityEvent) {}
    open fun tick() {}
    open fun canUpdateInventory(): Boolean = false
    open fun updateInventory() {}

}