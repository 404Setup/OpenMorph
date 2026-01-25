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
import one.pkg.om.manager.BanManager
import one.pkg.om.manager.OManager
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.Material
import org.bukkit.entity.Player

class ReadCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(Commands.literal("read").executes { ctx -> execute(ctx) })
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendFailed("This command is only for players.")
            return 0
        }
        val item = sender.inventory.itemInMainHand
        if (!item.type.isBlock || item.type == Material.AIR) {
            sender.sendWarning("You must hold a block in your main hand.")
            return 0
        }

        val data = OManager.playerMorph[sender] ?: return 0
        val blockName = item.type.name

        if (!sender.isOp && BanManager.isLocked("block", blockName)) {
            sender.sendFailed("This block morph is locked.")
            return 0
        }

        if (data.offlineData.hasBlock(blockName)) {
            sender.sendWarning("You already unlocked this block.")
        } else {
            item.amount -= 1
            data.offlineData.addBlock(blockName)
            sender.sendSuccess("Unlocked morph for block: $blockName")
        }
        return 1
    }
}
