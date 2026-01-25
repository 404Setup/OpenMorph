/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.entities.MorphBlock
import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.HostilityManager
import one.pkg.om.manager.OManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent

class MobTarget : Listener {
    @EventHandler
    fun onTarget(e: EntityTargetEvent) {
        val target = e.target
        if (target !is Player) return

        val data = OManager.playerMorph[target] ?: return
        val morph = data.current ?: return

        if (morph is MorphBlock) {
            e.isCancelled = true
            return
        }

        if (morph is MorphEntity) {
            if (e.reason == EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY ||
                e.reason == EntityTargetEvent.TargetReason.DEFEND_VILLAGE ||
                e.reason == EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY
            ) {
                return
            }

            if (HostilityManager.shouldAttack(e.entity.type, morph.entityType)) {
                return
            } else {
                e.isCancelled = true
            }
        }
    }
}
