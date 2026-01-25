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
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.DialogKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import one.pkg.om.utils.asComponent
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class OmBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        // TEST
        /*context.lifecycleManager.registerEventHandler(
            RegistryEvents.DIALOG.compose()
                .newHandler { event ->
                    event.registry().register(DialogKeys.create(Key.key("papermc:custom_dialog"))) { builder ->
                        builder
                            .base(DialogBase.builder("Title".asComponent()).build())
                            .type(DialogType.notice())
                    }
                }
        )

        DialogAction.customClick(
            { view, audience ->
                val levels = view.getFloat("level")?.toInt() ?: 0
                val exp = view.getFloat("experience") ?: 0.00F

                if (audience is Player) {
                    audience.sendRichMessage(
                        "You selected <color:#ccfffd><level> levels</color> and <color:#ccfffd><exp>% exp</color> to the next level!",
                        Placeholder.component("level", Component.text(levels)),
                        Placeholder.component("exp", Component.text(exp))
                    )

                    audience.level = levels;
                    audience.exp = exp / 100
                }
            },
            ClickCallback.Options.builder()
                .uses(1) // Set the number of uses for this callback. Defaults to 1
                .lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
                .build()
        )*/
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return OmMain()
    }
}