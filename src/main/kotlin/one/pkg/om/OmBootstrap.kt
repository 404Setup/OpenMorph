/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

@file:Suppress("ALL")

package one.pkg.om

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.registry.event.RegistryEvents
import one.pkg.om.dialog.*
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class OmBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(
            RegistryEvents.DIALOG.compose()
                .newHandler { event ->
                    val dialogs = listOf(
                        AppendDialog(),
                        DropDialog(),
                        GetDialog(),
                        InfoDialog(),
                        LockDialog(),
                        MainDialog(),
                        MorphDialog(),
                        ReadDialog(),
                        UnMorphDialog(),
                        UnlockDialog()
                    )
                    dialogs.forEach { it.register(event) }
                }
        )
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return OmMain()
    }
}