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
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent

@Suppress("UnstableApiUsage")
class RunDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val base = DialogBase.builder(Component.text("Run Skill"))
            .build()
        builder.base(base)

        val buttons = (1..9).map { id ->
            ActionButton.create(
                Component.text("Skill $id"),
                null,
                1,
                DialogAction.staticAction(ClickEvent.runCommand("/om run $id"))
            )
        }

        val closeButton = ActionButton.create(
            Component.text("Close"),
            null,
            1,
            null
        )

        builder.type(DialogType.multiAction(buttons, closeButton, 3))
    }

    override val key = "run"

}
