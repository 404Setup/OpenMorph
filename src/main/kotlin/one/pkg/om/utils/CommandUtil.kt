/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.utils

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import one.pkg.om.manager.OManager
import one.pkg.om.manager.UiManager
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

val MORPH_TYPES = listOf("entity", "block", "player")

fun resolveTargetId(sender: CommandSender, type: String, id: String?, commandName: String): String? {
    if (id != null) return id

    if (type == "block") {
        if (sender !is Player) {
            sender.sendFailed("Console must specify ID.")
            return null
        }
        val item = sender.inventory.itemInMainHand
        if (!item.type.isBlock || item.type == Material.AIR) {
            sender.sendFailed("You must hold a block.")
            return null
        }
        return item.type.name
    } else {
        sender.sendWarning("Usage: /om $commandName <type> <id>")
        return null
    }
}

fun handleUiRedirect(sender: CommandSender, action: String): Boolean {
    if (sender is Player) {
        val data = OManager.playerMorph[sender]
        val uiMode = data?.offlineData?.uiMode ?: "COMMAND"
        if (uiMode.uppercase() != "COMMAND") {
            UiManager.openUi(sender, action)
            return true
        }
    }
    return false
}

fun suggestMorphTypes(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
    MORPH_TYPES
        .filter { it.startsWith(builder.remaining, true) }
        .forEach { builder.suggest(it) }
    return builder.buildFuture()
}

