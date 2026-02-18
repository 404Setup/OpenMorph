/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.dialog

import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.data.dialog.action.DialogAction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent

@Suppress("UnstableApiUsage")
class InfoDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val helpButton = ActionButton.builder(Component.text("Help"))
            .action(DialogAction.staticAction(ClickEvent.runCommand("/om help")))
            .build()

        val closeButton = ActionButton.builder(Component.text("Close"))
            .build()

        builder.base(DialogBase.builder(Component.text("OpenMorph Info"))
            .body(listOf(
                DialogBody.plainMessage(Component.text("OpenMorph Plugin")),
                DialogBody.plainMessage(Component.text("A powerful morphing tool for your server.")),
                DialogBody.plainMessage(Component.text("Use /om help for a list of commands."))
            ))
            .build())

        builder.type(DialogType.multiAction(listOf(helpButton, closeButton)).build())
    }

    override val key = "info"

}
