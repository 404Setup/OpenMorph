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

class MorphDialog : IDialog {
    override fun create(builder: DialogRegistryEntry.Builder) {
        val entityButton = ActionButton.builder(Component.text("Entity"))
            .action(DialogAction.commandTemplate("/morph entity "))
            .build()

        val blockButton = ActionButton.builder(Component.text("Block"))
            .action(DialogAction.commandTemplate("/morph block "))
            .build()

        val playerButton = ActionButton.builder(Component.text("Player"))
            .action(DialogAction.commandTemplate("/morph player "))
            .build()

        builder.base(DialogBase.builder(Component.text("Morph Menu"))
            .body(listOf(DialogBody.plainMessage(Component.text("Choose a category to morph into:"))))
            .build())

        builder.type(DialogType.multiAction(listOf(entityButton, blockButton, playerButton)).build())
    }

    override val key = "morph"

}
