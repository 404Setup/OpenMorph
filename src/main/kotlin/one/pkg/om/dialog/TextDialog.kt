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
class TextDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val base = DialogBase.builder(Component.text("Text Input"))
            .inputs(listOf(
                DialogInput.text("text_input", Component.text("Enter text")).build()
            ))
            .build()
        builder.base(base)

        val submit = ActionButton.builder(Component.text("Submit"))
            .action(DialogAction.commandTemplate("say {text_input}"))
            .build()

        val close = ActionButton.builder(Component.text("Close"))
            .build()

        builder.type(DialogType.confirmation(submit, close))
    }

    override val key = "text"

}
