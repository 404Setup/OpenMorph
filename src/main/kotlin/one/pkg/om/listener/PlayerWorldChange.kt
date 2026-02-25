/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.OManager
import one.pkg.om.utils.getPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class PlayerWorldChange : Listener {

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        val world = player.world
        val plugin = getPlugin()

        // Case 1: Player (morphed or not) enters a world.
        // We need to ensure any EXISTING morphs in this world are hidden from this player.
        // This is necessary because setSelfVisible(false) only hides from players currently in the world.
        OManager.playerMorph.forEach { (morphedPlayer, data) ->
            // activeMorphEntityUuid check or similar ensures it's an active morph
            if (morphedPlayer.world == world && morphedPlayer != player) {
                // If the morphed player has an active disguise (is hidden)
                // We should hide them from the new player.
                // We can check if they are supposed to be hidden.
                // Generally, if they are in OManager.playerMorph and have an active morph, they are hidden.
                if (data.current != null) {
                    player.hideEntity(plugin, morphedPlayer)
                }
            }
        }

        // Case 2: The player WHO CHANGED WORLD is a morph.
        // We need to hide this player from everyone in the new world.
        if (OManager.playerMorph.containsKey(player)) {
            val data = OManager.playerMorph[player]
            if (data?.current != null) {
                // Iterate players in this world and hide self
                world.players.forEach { other ->
                    if (other != player) {
                        other.hideEntity(plugin, player)
                    }
                }
            }
        }
    }
}
