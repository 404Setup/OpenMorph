/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

fun CommandSender.op() = hasPermission("404morph.admin") || isOp

fun CommandSender.sendSuccess(message: String) {
    sendMessage(message.asComponent(NamedTextColor.GREEN))
}

fun CommandSender.sendWarning(message: String) {
    sendMessage(message.asComponent(NamedTextColor.YELLOW))
}

fun CommandSender.sendFailed(message: String) {
    sendMessage(message.asComponent(NamedTextColor.RED))
}

fun String.asComponent() = Component.text(this)

fun String.asComponent(color: NamedTextColor) = Component.text(this, color)