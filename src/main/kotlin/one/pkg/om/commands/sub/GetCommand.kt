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
import one.pkg.om.commands.SubCommand
import one.pkg.om.entities.MorphBlock
import one.pkg.om.entities.MorphPlayer
import one.pkg.om.manager.OManager
import one.pkg.om.utils.op
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.entity.Player

class GetCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("get")
                .requires { it.sender.op() }
                .executes { ctx -> execute(ctx) }
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendWarning("This command is only for players.")
            return 0
        }
        val range = 10.0
        val rayTraceEntity = sender.world.rayTraceEntities(sender.eyeLocation, sender.eyeLocation.direction, range) {
            it != sender
        }

        if (rayTraceEntity != null && rayTraceEntity.hitEntity != null) {
            val hit = rayTraceEntity.hitEntity!!
            val owner = OManager.entityToPlayerMap[hit.uniqueId]
            if (owner != null) {
                sender.sendSuccess("Entity is a morph of player: ${owner.name}")
                return 1
            } else if (hit is Player) {
                val data = OManager.playerMorph[hit]
                if (data?.current is MorphPlayer) {
                    sender.sendSuccess("Player ${hit.name} is morphed as ${(data.current as MorphPlayer).targetName}")
                    return 1
                } else if (data?.current is MorphBlock) {
                    sender.sendSuccess("Player ${hit.name} is morphed as Block (You shouldn't see this player?)")
                    return 1
                }
                sender.sendWarning("Player ${hit.name} is not morphed.")
                return 1
            } else {
                sender.sendSuccess("This entity is real.")
                return 1
            }
        }

        val rayTraceBlock = sender.world.rayTraceBlocks(sender.eyeLocation, sender.eyeLocation.direction, range)
        if (rayTraceBlock != null && rayTraceBlock.hitBlock != null) {
            val block = rayTraceBlock.hitBlock!!
            val morphedP = OManager.playerMorph.entries.find {
                val m = it.value.current
                m is MorphBlock && m.isAt(block.location)
            }?.key

            if (morphedP != null) {
                sender.sendSuccess("Block is a morph of player: ${morphedP.name}")
                return 1
            } else {
                sender.sendSuccess("This block is real.")
                return 1
            }
        }

        sender.sendWarning("Nothing found.")
        return 0
    }
}
