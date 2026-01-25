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
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import one.pkg.om.commands.SubCommand
import one.pkg.om.manager.BanManager
import one.pkg.om.manager.RequestManager
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendWarning
import org.bukkit.entity.Player

class RequestCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("request")
                .then(
                    Commands.literal("send")
                        .then(
                            Commands.argument("player", ArgumentTypes.player())
                                .executes { ctx -> executeSend(ctx) })
                )
                .then(
                    Commands.literal("yes")
                        .then(
                            Commands.argument("target", StringArgumentType.word())
                                .executes { ctx -> executeYes(ctx) })
                )
                .then(
                    Commands.literal("no")
                        .then(
                            Commands.argument("target", StringArgumentType.word())
                                .executes { ctx -> executeNo(ctx) })
                )
        )
    }

    private fun executeSend(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendFailed("This command is for players only.")
            return 0
        }
        val target =
            ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        if (target == null) {
            sender.sendWarning("Player is not online.")
            return 0
        }
        if (BanManager.isLocked("player", target.name)) {
            sender.sendFailed("This player morph is locked.")
            return 0
        }
        if (target == sender) {
            sender.sendFailed("You cannot send a request to yourself.")
            return 0
        }
        RequestManager.sendRequest(sender, target)
        return 1
    }

    private fun executeYes(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendFailed("This command is for players only.")
            return 0
        }
        val targetName = StringArgumentType.getString(ctx, "target")
        RequestManager.acceptRequest(sender, targetName)
        return 1
    }

    private fun executeNo(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendFailed("This command is for players only.")
            return 0
        }
        val targetName = StringArgumentType.getString(ctx, "target")
        RequestManager.denyRequest(sender, targetName)
        return 1
    }
}
