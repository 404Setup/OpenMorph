/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.commands.sub

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import one.pkg.om.commands.SubCommand
import one.pkg.om.manager.OManager
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import org.bukkit.entity.Player

class SelectUiCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("selectui")
                .then(
                    Commands.argument("mode", StringArgumentType.word())
                        .suggests { _, builder ->
                            listOf("COMMAND", "DIALOG", "CONTAINER")
                                .filter { it.startsWith(builder.remaining, true) }
                                .forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes { ctx -> execute(ctx) }
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendFailed("This command is for players only.")
            return 0
        }

        val modeInput = StringArgumentType.getString(ctx, "mode").uppercase()
        if (modeInput !in listOf("COMMAND", "DIALOG", "CONTAINER")) {
            sender.sendFailed("Invalid UI mode. Choose from: COMMAND, DIALOG, CONTAINER")
            return 0
        }

        val data = OManager.playerMorph[sender] ?: return 0
        data.offlineData.uiMode = modeInput
        data.offlineData.markDirty()
        sender.sendSuccess("Your running mode has been set to: $modeInput")
        return 1
    }
}
