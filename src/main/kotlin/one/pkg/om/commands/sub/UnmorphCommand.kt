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
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import org.bukkit.entity.Player

class UnmorphCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("unmorph")
                .executes { ctx -> execute(ctx, false) }
                .then(
                    Commands.argument("player", ArgumentTypes.player())
                        .executes { ctx -> execute(ctx, true) }
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>, hasPlayerArg: Boolean): Int {
        val sender = ctx.source.sender
        var targetPlayer = sender as? Player

        if (hasPlayerArg && sender.op()) {
            targetPlayer =
                ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        }

        if (targetPlayer == null) {
            sender.sendFailed("Target player not found or not specified.")
            return 0
        }

        val data = OManager.playerMorph[targetPlayer]
        if (data == null || data.current == null) {
            sender.sendFailed("You are not currently morphed.")
            return 0
        }
        data.current?.stop()
        data.current = null
        data.offlineData.clearActiveMorph()
        sender.sendSuccess("Unmorphed ${targetPlayer.name}")
        return 1
    }
}
