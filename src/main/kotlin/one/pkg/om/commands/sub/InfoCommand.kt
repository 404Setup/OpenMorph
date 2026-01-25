/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.commands.sub

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import one.pkg.om.commands.SubCommand
import one.pkg.om.entities.MorphBlock
import one.pkg.om.entities.MorphEntity
import one.pkg.om.entities.MorphPlayer
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.entity.Player

class InfoCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("info")
                .executes { ctx -> execute(ctx, false) }
                .then(
                    Commands.argument("player", ArgumentTypes.player())
                        .requires { it.sender.op() }
                        .executes { ctx -> execute(ctx, true) }
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>, hasArg: Boolean): Int {
        val sender = ctx.source.sender
        var targetPlayer = sender as? Player
        if (hasArg && sender.op()) {
            targetPlayer =
                ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        }

        if (targetPlayer == null) {
            sender.sendWarning("Target player not found or usage invalid.")
            return 0
        }

        val data = OManager.playerMorph[targetPlayer] ?: return 0
        val current = data.current
        val isMorphed = current != null
        val morphInfo = if (isMorphed) {
            when (current) {
                is MorphEntity -> "Entity: ${current.entityType}"
                is MorphBlock -> "Block: ${current.material}"
                is MorphPlayer -> "Player: ${current.targetName}"
                else -> "Unknown"
            }
        } else "None"

        sender.sendWarning("--- Info for ${targetPlayer.name} ---")
        sender.sendSuccess("Morphed: $isMorphed ($morphInfo)")
        sender.sendSuccess("Unlocked Entities: ${data.offlineData.entities.size}")
        sender.sendSuccess("Unlocked Blocks: ${data.offlineData.blocks.size}")
        sender.sendSuccess("Unlocked Players: ${data.offlineData.players.size}")
        return 1
    }
}
