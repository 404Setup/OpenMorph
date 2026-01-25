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
import one.pkg.om.data.SavePlayerData
import one.pkg.om.utils.op
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import java.util.concurrent.CompletableFuture

class AppendCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("append")
                .requires { it.sender.op() }
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
        val targetPlayer =
            ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()

        if (targetPlayer == null) {
            sender.sendFailed("Player not found.")
            return 0
        }

        val targetName = targetPlayer.name
        val data = OManager.playerMorph[targetPlayer] ?: return 0

        val type = StringArgumentType.getString(ctx, "type").lowercase()
        val id = StringArgumentType.getString(ctx, "id")

        when (type) {
            "entity" -> {
                if (data.offlineData.hasEntity(id)) {
                    sender.sendFailed("$targetName already has entity $id")
                    return 0
                }
                data.offlineData.addEntity(id)
                sender.sendSuccess("Added entity $id to $targetName")
            }

            "block" -> {
                if (data.offlineData.hasBlock(id)) {
                    sender.sendFailed("$targetName already has block $id")
                    return 0
                }
                data.offlineData.addBlock(id)
                sender.sendSuccess("Added block $id to $targetName")
            }

            "player" -> {
                if (id == targetName) {
                    sender.sendFailed("You cannot append the same thing to themselves.")
                    return 0
                }
                if (id.equals("all", ignoreCase = true)) {
                    Bukkit.getOnlinePlayers().forEach { p ->
                        if (!data.offlineData.hasPlayer(p.uniqueId)) {
                            val profile = p.playerProfile
                            val textures = profile.properties.firstOrNull { it.name == "textures" }
                            val skinData = if (textures != null) "${textures.value};${textures.signature ?: ""}" else ""
                            data.offlineData.addPlayer(SavePlayerData(p.uniqueId, p.name, skinData))
                        }
                    }
                    sender.sendSuccess("Added all online players to $targetName")
                } else {
                    val p = Bukkit.getPlayer(id)
                    if (p != null) {
                        if (data.offlineData.hasPlayer(p.uniqueId)) {
                            sender.sendFailed("$targetName already has player ${p.name}")
                            return 0
                        }

                        val profile = p.playerProfile
                        val textures = profile.properties.firstOrNull { it.name == "textures" }
                        val skinData = if (textures != null) "${textures.value};${textures.signature ?: ""}" else ""

                        data.offlineData.addPlayer(SavePlayerData(p.uniqueId, p.name, skinData))
                        sender.sendSuccess("Added player ${p.name} to $targetName")
                    } else {
                        val offlineP = Bukkit.getOfflinePlayer(id)
                        if (data.offlineData.hasPlayer(offlineP.uniqueId)) {
                            sender.sendFailed("$targetName already has player $id")
                            return 0
                        }
                        data.offlineData.addPlayer(SavePlayerData(offlineP.uniqueId, id, ""))
                        sender.sendFailed("Added player $id (Offline/Unknown) to $targetName")
                    }
                }
            }
        }
        return 1
    }

    private fun suggestIds(
        ctx: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val targetPlayer = try {
            ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        } catch (_: Exception) {
            null
        } ?: return builder.buildFuture()

        val data = OManager.playerMorph[targetPlayer]?.offlineData ?: return builder.buildFuture()
        val type = StringArgumentType.getString(ctx, "type").lowercase()
        val input = builder.remaining

        if (type == "player") {
            val allPlayers = Bukkit.getOnlinePlayers().map { it.name } + "all"
            allPlayers.filter { name ->
                val isOwned = data.hasPlayer(name)
                !isOwned && name.startsWith(input, true)
            }.forEach { builder.suggest(it) }
            return builder.buildFuture()
        }
        if (type == "entity") {
            val allEntities = EntityType.entries.map { it.name } + "all"
            allEntities.filter { name ->
                val isOwned = data.hasEntity(name)
                !isOwned && name.startsWith(input, true)
            }.forEach { builder.suggest(it) }
            return builder.buildFuture()
        }
        if (type == "block") {
            val allBlocks = LockCommand.ALL_BLOCKS

            if (input.startsWith("block")) {
                val pageStr = input.drop(5)
                val page = pageStr.toIntOrNull()
                if (page != null) {
                    val start = (page - 1) * 20
                    if (start < allBlocks.size) {
                        val end = (start + 20).coerceAtMost(allBlocks.size)
                        allBlocks.subList(start, end).filter { name ->
                            !data.hasBlock(name)
                        }.forEach { builder.suggest(it) }
                        return builder.buildFuture()
                    }
                }
            }

            val pages = (1..(allBlocks.size / 20 + 1)).map { "block$it" }
            pages.filter { it.startsWith(input, true) }.forEach { builder.suggest(it) }

            if (input.isNotEmpty() && !input.startsWith("block")) {
                allBlocks.filter { name ->
                    !data.hasBlock(name) && name.startsWith(input, true)
                }.take(50).forEach { builder.suggest(it) }
            }

            return builder.buildFuture()
        }
        return builder.buildFuture()
    }
}
