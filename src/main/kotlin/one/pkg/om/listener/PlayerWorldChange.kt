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

        OManager.playerMorph.forEach { (morphedPlayer, data) ->
            if (morphedPlayer.world == world && morphedPlayer != player) {
                if (data.current != null) {
                    player.hideEntity(plugin, morphedPlayer)
                }
            }
        }

        if (OManager.playerMorph.containsKey(player)) {
            val data = OManager.playerMorph[player]
            if (data?.current != null) {
                world.players.forEach { other ->
                    if (other != player) {
                        other.hideEntity(plugin, player)
                    }
                }
            }
        }
    }
}
