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
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import one.pkg.om.commands.SubCommand
import one.pkg.om.manager.BanManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class UnlockCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("unlock")
                .requires { it.sender.op() }
                .then(
                    Commands.argument("type", StringArgumentType.word())
                        .suggests { _, builder ->
                            listOf("entity", "block", "player")
                                .filter { it.startsWith(builder.remaining, true) }
                                .forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes { ctx -> execute(ctx, false) }
                        .then(
                            Commands.argument("id", StringArgumentType.string())
                                .suggests(this::suggestLocked)
                                .executes { ctx -> execute(ctx, true) }
                        )
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>, hasId: Boolean): Int {
        val sender = ctx.source.sender
        val type = StringArgumentType.getString(ctx, "type").lowercase()
        var id = if (hasId) StringArgumentType.getString(ctx, "id") else null

        if (id == null) {
            if (type == "block") {
                if (sender !is Player) {
                    sender.sendFailed("Console must specify ID.")
                    return 0
                }
                val item = sender.inventory.itemInMainHand
                if (!item.type.isBlock || item.type == Material.AIR) {
                    sender.sendFailed("You must hold a block.")
                    return 0
                }
                id = item.type.name
            } else {
                sender.sendWarning("Usage: /om unlock <type> <id>")
                return 0
            }
        }

        if (!BanManager.isLocked(type, id)) {
            sender.sendWarning("$type $id is not locked.")
            return 0
        }

        BanManager.unlock(type, id)
        sender.sendSuccess("Unlocked $type $id")
        return 1
    }

    private fun suggestLocked(
        ctx: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val type = StringArgumentType.getString(ctx, "type").lowercase()
        val input = builder.remaining
        BanManager.getLockedIds(type).filter { it.startsWith(input, true) }.forEach { builder.suggest(it) }
        return builder.buildFuture()
    }
}
