/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import one.pkg.om.commands.sub.*

class OmCommand {
    private val subCommands = listOf(
        MorphCommand(),
        UnmorphCommand(),
        RunCommand(),
        NametagCommand(),
        ReadCommand(),
        AppendCommand(),
        DropCommand(),
        InfoCommand(),
        GetCommand(),
        RequestCommand(),
        SaveAllCommand(),
        LockCommand(),
        UnlockCommand()
    )

    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val root = Commands.literal("om")
            .executes { ctx ->
                sendHelp(ctx.source)
                1
            }

        subCommands.forEach { it.register(root) }
        return root
    }

    private fun sendHelp(stack: CommandSourceStack) {
        val sender = stack.sender
        sender.sendMessage("OpenMorph Commands:")
        sender.sendMessage("/om morph <entity|block|player> <target> [player]")
        sender.sendMessage("/om unmorph [player]")
        sender.sendMessage("/om run <id> [player]")
        sender.sendMessage("/om read - Unlock held block morph")
        sender.sendMessage("/om drop <type> <id> <player>")
        sender.sendMessage("/om nametag - Toggle nametag")
        sender.sendMessage("/om request <send|yes|no> <player>")
        if (sender.hasPermission("404morph.admin")) {
            sender.sendMessage("/om append <player> <type> <id>")
            sender.sendMessage("/om info [player]")
            sender.sendMessage("/om get")
            sender.sendMessage("/om saveAll")
            sender.sendMessage("/om lock <type> <id>")
            sender.sendMessage("/om unlock <type> <id>")
        }
    }
}
