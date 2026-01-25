/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.event.RegistryComposeEvent
import io.papermc.paper.registry.keys.DialogKeys
import net.kyori.adventure.key.Key

@Suppress("UnstableApiUsage")
interface IDialog {
    val key: String

    fun create(builder: DialogRegistryEntry.Builder)

    fun register(event: RegistryComposeEvent<Dialog, DialogRegistryEntry.Builder>) {
        event.registry().register(DialogKeys.create(Key.key("404morph:$key"))) { builder ->
            this.create(builder)
        }
    }
}