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
class LockDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val inputs = listOf(
            DialogInput.singleOption("type", Component.text("Type"), IDialog.typeOptions).build(),
            DialogInput.text("id", Component.text("Target ID/Name")).build()
        )

        val lockIdButton = ActionButton.builder(Component.text("Lock by ID/Name"))
            .action(DialogAction.commandTemplate("om lock {type} {id}"))
            .build()

        val lockHeldBlockButton = ActionButton.builder(Component.text("Lock Held Block"))
            .action(DialogAction.commandTemplate("om lock block"))
            .build()

        val base = DialogBase.builder(Component.text("Lock Morph"))
            .inputs(inputs)
            .build()

        builder.base(base)
        builder.type(DialogType.multiAction(listOf(lockIdButton, lockHeldBlockButton)).build())
    }

    override val key = "lock"

}
