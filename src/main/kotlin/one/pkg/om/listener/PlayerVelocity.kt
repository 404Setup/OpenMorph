/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.OManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerVelocityEvent

/**
 * Handles knockback prevention for morphs that have knockback disabled.
 * This acts as a safeguard when Attribute.KNOCKBACK_RESISTANCE is not sufficient.
 */
class PlayerVelocity : Listener {
    @EventHandler
    fun onVelocity(event: PlayerVelocityEvent) {
        val morph = OManager.playerMorph[event.player]?.current ?: return
        if (morph is MorphEntity && !morph.hasKnockback) {
            event.isCancelled = true
        }
    }
}
