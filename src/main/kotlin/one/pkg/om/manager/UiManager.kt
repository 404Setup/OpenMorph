/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.manager

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

object UiManager {
    @Suppress("UnstableApiUsage")
    fun openUi(player: Player, key: String) {
        val data = OManager.playerMorph[player]
        val uiMode = data?.offlineData?.uiMode ?: "COMMAND"

        when (uiMode.uppercase()) {
            "DIALOG" -> {
                val dialog = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.DIALOG)
                    .get(Key.key("404morph:$key"))
                if (dialog != null) {
                    player.showDialog(dialog)
                } else {
                    player.sendMessage("Dialog 404morph:$key not found.")
                }
            }

            "CONTAINER" -> {
                ContainerUiManager.openMenu(player, key)
            }

            else -> {
                player.sendMessage("You are in COMMAND mode. Commands execute immediately without UI.")
            }
        }
    }
}
