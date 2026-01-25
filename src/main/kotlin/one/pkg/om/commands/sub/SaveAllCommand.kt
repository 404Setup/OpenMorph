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
import one.pkg.om.manager.OManager
import one.pkg.om.utils.sendSuccess

class SaveAllCommand : SubCommand {
    override fun register(root: LiteralArgumentBuilder<CommandSourceStack>) {
        root.then(
            Commands.literal("saveall")
                .requires { it.sender.hasPermission("404morph.admin") || it.sender.isOp }
                .executes { ctx -> execute(ctx) }
        )
    }

    private fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        OManager.saveAll()
        ctx.source.sender.sendSuccess("Saved all morph data.")
        return 1
    }
}
