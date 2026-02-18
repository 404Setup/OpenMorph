/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.data

import one.pkg.om.entities.MorphEntities
import one.pkg.om.utils.runAs
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent

data class OnlineMorphData(
    var current: MorphEntities?,
    val offlineData: SaveMorphData
) {
    fun onMove(event: PlayerMoveEvent) {
        current?.onMove(event)
    }

    fun onDamage(event: EntityDamageEvent) {
        current?.onDamage(event)
    }

    fun onAttack(event: EntityDamageByEntityEvent) {
        current?.onAttack(event)
    }
}