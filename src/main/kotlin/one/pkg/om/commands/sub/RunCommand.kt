/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.commands.sub

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import one.pkg.om.commands.SubCommand
import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.entity.Player

class RunCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("run")
                .then(
                    Commands.argument("id", IntegerArgumentType.integer())
                        .executes { ctx -> execute(ctx, false) }
                        .then(
                            Commands.argument("player", ArgumentTypes.player())
                                .executes { ctx -> execute(ctx, true) }
                        )
                )
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>, hasPlayerArg: Boolean): Int {
        val sender = ctx.source.sender
        val id = IntegerArgumentType.getInteger(ctx, "id")

        var targetPlayer = sender as? Player
        if (hasPlayerArg && sender.op()) {
            targetPlayer =
                ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).firstOrNull()
        }

        if (targetPlayer == null) return 0

        val data = OManager.playerMorph[targetPlayer]
        if (data?.current is MorphEntity) {
            (data.current as MorphEntity).useSkill(id)
            sender.sendSuccess("Used skill $id for ${targetPlayer.name}")
            return 1
        } else {
            sender.sendWarning("Not morphed as entity or no skills.")
            return 0
        }
    }
}
