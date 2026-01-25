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
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import one.pkg.om.commands.SubCommand
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import java.util.concurrent.CompletableFuture

class DropCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("drop")
                .then(
                    Commands.argument("player", ArgumentTypes.player())
                        .then(
                            Commands.argument("type", StringArgumentType.word())
                                .suggests { _, builder ->
                                    listOf("entity", "block", "player")
                                        .filter { it.startsWith(builder.remaining, true) }
                                        .forEach { builder.suggest(it) }
                                    builder.buildFuture()
                                }
                                .then(
                                    Commands.argument("id", StringArgumentType.string())
                                        .suggests(this::suggestIds)
                                        .executes { ctx -> execute(ctx) }
                                )
                        )
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        val isAdmin = sender.op()

        val targetPlayer =
            ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()

        if (targetPlayer == null) {
            sender.sendFailed("Player not found.")
            return 0
        }

        val targetName = targetPlayer.name

        if (!isAdmin) {
            if (!targetName.equals(sender.name, ignoreCase = true)) {
                sender.sendFailed("You can only drop your own morphs.")
                return 0
            }
        }

        val data = OManager.playerMorph[targetPlayer] ?: run {
            sender.sendFailed("Player $targetName has no morph data.")
            return 0
        }
        val type = StringArgumentType.getString(ctx, "type").lowercase()
        val id = StringArgumentType.getString(ctx, "id")

        when {
            type.startsWith("entity") -> {
                if (data.offlineData.removeEntity(id))
                    sender.sendSuccess("Removed entity $id from $targetName")
                else sender.sendWarning("$targetName don't have entity $id")
            }

            type.startsWith("block") -> {
                if (data.offlineData.removeBlock(id))
                    sender.sendMessage("Removed block $id from $targetName")
                else sender.sendWarning("$targetName don't have block $id")
            }

            type.startsWith("player") -> {
                if (data.offlineData.removePlayer(id))
                    sender.sendMessage("Removed player $id from $targetName")
                else sender.sendWarning("$targetName don't have player $id")
            }

            else -> sender.sendFailed("Invalid type: $type")
        }
        return 1
    }

    private fun suggestIds(
        ctx: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val targetPlayer = try {
            ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        } catch (e: Exception) {
            null
        } ?: return builder.buildFuture()

        val data = OManager.playerMorph[targetPlayer] ?: return builder.buildFuture()
        val type = StringArgumentType.getString(ctx, "type").lowercase()
        val input = builder.remaining

        val list = when {
            type.startsWith("entity") -> data.offlineData.entities
            type.startsWith("player") -> data.offlineData.players.map { it.name }
            type.startsWith("block") -> data.offlineData.blocks
            else -> emptyList()
        }
        list.filter { it.startsWith(input, true) }.forEach { builder.suggest(it) }
        return builder.buildFuture()
    }
}
