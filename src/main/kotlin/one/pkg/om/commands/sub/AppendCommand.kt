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
import one.pkg.om.data.MorphIgnored
import one.pkg.om.data.SavePlayerData
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
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
        val id = StringArgumentType.getString(ctx, "id").uppercase()

        when (type) {
            "entity" -> {
                if (data.offlineData.hasEntity(id)) {
                    sender.sendFailed("$targetName already has entity $id")
                    return 0
                }
                val entityType = EntityType.fromName(id)
                if (entityType == null) {
                    sender.sendFailed("$id is not a valid entity.")
                    return 0
                }
                if (MorphIgnored.ignored.contains(entityType)) {
                    sender.sendFailed("$id is ignored by the plugin.")
                    return 0
                }
                if (data.offlineData.addEntity(id)) {
                    sender.sendSuccess("Added entity $id to $targetName")
                } else {
                    sender.sendFailed("Could not add entity $id (Limit reached?)")
                }
            }

            "block" -> {
                if (data.offlineData.hasBlock(id)) {
                    sender.sendFailed("$targetName already has block $id")
                    return 0
                }
                val block: Material?
                if (sender is Player && id == "hand") {
                    val item = sender.inventory.itemInMainHand
                    if (!item.type.isBlock || item.type == Material.AIR) {
                        sender.sendWarning("You must hold a block in your main hand.")
                        return 0
                    }
                    block = item.type
                } else {
                    block = Material.getMaterial(id)
                    if (block == null) {
                        sender.sendFailed("$id is not a valid block.")
                        return 0
                    }
                }
                if (data.offlineData.addBlock(block.name)) {
                    sender.sendSuccess("Added block $id to $targetName")
                } else {
                    sender.sendFailed("Could not add block $id (Limit reached?)")
                }
            }

            "player" -> {
                if (id == targetName) {
                    sender.sendFailed("You cannot append the same thing to themselves.")
                    return 0
                }
                if (id.equals("all", ignoreCase = true)) {
                    var count = 0
                    Bukkit.getOnlinePlayers().forEach { p ->
                        if (!data.offlineData.hasPlayer(p.uniqueId)) {
                            val profile = p.playerProfile
                            val textures = profile.properties.firstOrNull { it.name == "textures" }
                            val skinData = if (textures != null) "${textures.value};${textures.signature ?: ""}" else ""
                            if (data.offlineData.addPlayer(SavePlayerData(p.uniqueId, p.name, skinData))) {
                                count++
                            }
                        }
                    }
                    sender.sendSuccess("Added $count online players to $targetName")
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

                        if (data.offlineData.addPlayer(SavePlayerData(p.uniqueId, p.name, skinData))) {
                            sender.sendSuccess("Added player ${p.name} to $targetName")
                        } else {
                            sender.sendFailed("Could not add player ${p.name} (Limit reached?)")
                        }
                    } else {
                        val offlineP = Bukkit.getOfflinePlayer(id)
                        if (data.offlineData.hasPlayer(offlineP.uniqueId)) {
                            sender.sendFailed("$targetName already has player $id")
                            return 0
                        }
                        if (data.offlineData.addPlayer(SavePlayerData(offlineP.uniqueId, id, ""))) {
                            sender.sendFailed("Added player $id (Offline/Unknown) to $targetName")
                        } else {
                            sender.sendFailed("Could not add player $id (Limit reached?)")
                        }
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
            val allEntities = EntityType.entries.filter { !MorphIgnored.ignored.contains(it) }.map { it.name } + "all"
            allEntities.filter { name ->
                val isOwned = data.hasEntity(name)
                !isOwned && name.startsWith(input, true)
            }.forEach { builder.suggest(it) }
            return builder.buildFuture()
        }
        return builder.buildFuture()
    }
}
