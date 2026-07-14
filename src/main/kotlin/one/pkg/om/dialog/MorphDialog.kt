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
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component

@Suppress("UnstableApiUsage")
class MorphDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val inputs = listOf(
            DialogInput.singleOption("type", Component.text("Type"), IDialog.typeOptions).build(),
            DialogInput.text("target", Component.text("Target (ID/Name)")).build(),
            DialogInput.text("player", Component.text("Target Player (Optional, Admin only)")).build()
        )

        val morphSelfButton = ActionButton.builder(Component.text("Morph Self"))
            .action(DialogAction.commandTemplate("om morph {type} {target}"))
            .build()

        val morphOtherButton = ActionButton.builder(Component.text("Morph Other"))
            .action(DialogAction.commandTemplate("om morph {type} {target} {player}"))
            .build()

        builder.base(DialogBase.builder(Component.text("Morph Menu"))
            .inputs(inputs)
            .build())

        builder.type(DialogType.multiAction(listOf(morphSelfButton, morphOtherButton)).build())
    }

    override val key = "morph"

}
