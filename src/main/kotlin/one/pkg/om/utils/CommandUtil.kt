/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.utils

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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
