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
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component

@Suppress("UnstableApiUsage")
class MainDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val morphButton = ActionButton.builder(Component.text("Morph"))
            .action(DialogAction.commandTemplate("om morph"))
            .build()

        val unmorphButton = ActionButton.builder(Component.text("Unmorph"))
            .action(DialogAction.commandTemplate("om unmorph"))
            .build()

        val readButton = ActionButton.builder(Component.text("Read"))
            .action(DialogAction.commandTemplate("om read"))
            .build()

        val infoButton = ActionButton.builder(Component.text("Info"))
            .action(DialogAction.commandTemplate("om info"))
            .build()

        val lockButton = ActionButton.builder(Component.text("Lock"))
            .action(DialogAction.commandTemplate("om lock"))
            .build()

        val unlockButton = ActionButton.builder(Component.text("Unlock"))
            .action(DialogAction.commandTemplate("om unlock"))
            .build()

        val runButton = ActionButton.builder(Component.text("Run"))
            .action(DialogAction.commandTemplate("om run"))
            .build()

        builder.base(DialogBase.builder(Component.text("OpenMorph Menu"))
            .body(listOf(DialogBody.plainMessage(Component.text("Select an action:"))))
            .build())

        builder.type(DialogType.multiAction(listOf(
            morphButton,
            unmorphButton,
            readButton,
            infoButton,
            lockButton,
            unlockButton,
            runButton
        )).build())
    }

    override val key = "main"

}
