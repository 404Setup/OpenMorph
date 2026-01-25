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
import one.pkg.om.entities.MorphBlock
import one.pkg.om.entities.MorphFactory
import one.pkg.om.entities.MorphPlayer
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Enemy
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class MorphCommand : SubCommand {

    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("morph")
                .then(
                    Commands.argument("type", StringArgumentType.word())
                        .suggests { _, builder ->
                            listOf("entity", "block", "player").filter { it.startsWith(builder.remaining, true) }
                                .forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .then(
                            Commands.argument("target", StringArgumentType.string())
                                .suggests(this::suggestTarget)
                                .executes { ctx -> execute(ctx, false) }
                                .then(
                                    Commands.argument("player", ArgumentTypes.player())
                                        .executes { ctx -> execute(ctx, true) }
                                )
                        )
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>, hasPlayerArg: Boolean): Int {
        val sender = ctx.source.sender
        val type = ctx.getArgument("type", String::class.java).lowercase()
        val targetVal = ctx.getArgument("target", String::class.java)
        val isOp = sender.op()

        val targetPlayer = if (hasPlayerArg && isOp) {
            ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        } else {
            sender as? Player
        }

        if (targetPlayer == null) {
            val msg =
                if (hasPlayerArg && isOp) "Player not found." else "Console must specify a target player."
            sender.sendFailed(msg)
            return 0
        }

        val data = OManager.playerMorph[targetPlayer] ?: return 0

        if (!isOp) {
            if (one.pkg.om.manager.BanManager.isLocked(type, targetVal)) {
                sender.sendFailed("This morph is locked.")
                return 0
            }

            val allowed = when {
                type.startsWith("entity") -> data.offlineData.hasEntity(targetVal)
                type.startsWith("block") -> data.offlineData.hasBlock(targetVal)
                type.startsWith("player") -> data.offlineData.hasPlayer(targetVal)
                else -> false
            }
            if (!allowed) {
                sender.sendFailed("You do not have permission to morph into this $type.")
                return 0
            }
        }

        if (data.current == null) {
            data.offlineData.originalMaxHealth = targetPlayer.getAttribute(Attribute.MAX_HEALTH)?.value
            data.offlineData.markDirty()
        }

        data.current?.stop()

        when {
            type.startsWith("entity") -> {
                val et = runCatching { EntityType.valueOf(targetVal.uppercase()) }.getOrNull()
                if (et != null) {
                    if (targetPlayer.world.difficulty == Difficulty.PEACEFUL) {
                        val clazz = et.entityClass
                        if (clazz != null && Enemy::class.java.isAssignableFrom(clazz)) {
                            sender.sendWarning("Cannot morph into enemy in Peaceful mode.")
                            return 0
                        }
                    }

                    val morph = MorphFactory.create(targetPlayer, et)
                    data.current = morph
                    morph.start()
                    data.offlineData.setActiveMorph("ENTITY", et.name)
                    sender.sendSuccess("Morphed ${targetPlayer.name} to entity $et")
                } else {
                    sender.sendFailed("Invalid entity type")
                }
            }

            type.startsWith("block") -> {
                val mat = runCatching { Material.valueOf(targetVal.uppercase()) }.getOrNull()
                if (mat != null && mat.isBlock) {
                    val morph = MorphBlock(targetPlayer, mat)
                    data.current = morph
                    morph.start()
                    data.offlineData.setActiveMorph("BLOCK", mat.name)
                    sender.sendSuccess("Morphed ${targetPlayer.name} to block $mat")
                } else {
                    sender.sendFailed("Invalid block material")
                }
            }

            type.startsWith("player") -> {
                val savedP = data.offlineData.players.find { it.name.equals(targetVal, ignoreCase = true) }
                val (skinVal, skinSig) = savedP?.skin?.let {
                    val parts = it.split(";", limit = 2)
                    parts[0] to parts.getOrNull(1)
                } ?: (null to null)

                val morph = MorphPlayer(targetPlayer, targetVal, skinVal, skinSig)
                data.current = morph
                morph.start()
                data.offlineData.setActiveMorph("PLAYER", targetVal, skinVal, skinSig)
                sender.sendSuccess("Morphed ${targetPlayer.name} to player $targetVal")
            }

            else -> sender.sendFailed("Invalid morph type")
        }
        return 1
    }

    private fun suggestTarget(
        ctx: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val type = ctx.getArgument("type", String::class.java).lowercase()
        val input = builder.remaining
        val sender = ctx.source.sender
        val isOp = sender.op()

        if (type.startsWith("block") && sender is Player) {
            val data = OManager.playerMorph[sender] ?: return builder.buildFuture()
            data.offlineData.blocks.filter { it.startsWith(input, true) }.forEach { builder.suggest(it) }
            return builder.buildFuture()
        }

        val list = if (sender is Player && !isOp) {
            val data = OManager.playerMorph[sender]
            if (data == null) {
                emptyList()
            } else {
                when {
                    type.startsWith("entity") -> if (data.offlineData.entities.contains("all")) EntityType.entries.map { it.name } else data.offlineData.entities
                    type.startsWith("player") -> data.offlineData.players.map { it.name }
                    else -> emptyList()
                }
            }
        } else {
            when {
                type.startsWith("entity") -> EntityType.entries.map { it.name }
                type.startsWith("block") -> Material.entries.filter { it.isBlock }.map { it.name }
                type.startsWith("player") -> Bukkit.getOnlinePlayers().map { it.name }
                else -> emptyList()
            }
        }

        list.filter { it.startsWith(input, true) }.forEach { builder.suggest(it) }
        return builder.buildFuture()
    }
}
