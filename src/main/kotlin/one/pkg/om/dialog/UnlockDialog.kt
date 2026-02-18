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
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component

@Suppress("UnstableApiUsage")
class UnlockDialog  : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val typeOptions = listOf(
            SingleOptionDialogInput.OptionEntry.create("entity", Component.text("Entity"), true),
            SingleOptionDialogInput.OptionEntry.create("block", Component.text("Block"), false),
            SingleOptionDialogInput.OptionEntry.create("player", Component.text("Player"), false)
        )

        val inputs = listOf(
            DialogInput.singleOption("type", Component.text("Type"), typeOptions).build(),
            DialogInput.text("id", Component.text("Target ID/Name")).build()
        )

        val action = ActionButton.create(
            Component.text("Unlock"),
            Component.text("Unlock the selected morph"),
            100,
            DialogAction.commandTemplate("om unlock {type} {id}")
        )

        val base = DialogBase.builder(Component.text("Unlock Morph"))
            .inputs(inputs)
            .build()

        builder.base(base)
        builder.type(DialogType.multiAction(listOf(action)).build())
    }

    override val key = "unlock"

}
