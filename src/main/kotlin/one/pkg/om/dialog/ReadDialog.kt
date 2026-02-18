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
class ReadDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val readButton = ActionButton.builder(Component.text("Unlock Block"))
            .action(DialogAction.commandTemplate("/om read"))
            .build()

        builder.base(DialogBase.builder(Component.text("Read Morph"))
            .body(listOf(DialogBody.plainMessage(Component.text("Hold a block in your main hand and click the button below to unlock its morph."))))
            .build())

        builder.type(DialogType.multiAction(listOf(readButton)).build())
    }

    override val key = "read"

}
