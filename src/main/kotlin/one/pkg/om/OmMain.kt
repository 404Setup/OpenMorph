/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om

import OMetrics
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import one.pkg.om.commands.OmCommand
import one.pkg.om.manager.BanManager
import one.pkg.om.manager.OManager
import one.pkg.om.utils.ClassScanner
import one.pkg.om.utils.div
import one.pkg.om.utils.runGlobalTaskTimer
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin


class OmMain : JavaPlugin() {
    init {
        instance = this
    }

    private var metrics: OMetrics? = null

    val playerSaveDir = (dataFolder / "saves").apply {
        mkdirs()
    }

    fun JavaPlugin.registerEvents(listener: Listener) {
        logger.info("Registering event listener: ${listener.javaClass.simpleName}")
        server.pluginManager.registerEvents(listener, this)
    }

    fun JavaPlugin.registerEvents(vararg listener: Listener) {
        listener.forEach { registerEvents(it) }
    }

    override fun onEnable() {
        println("Plugin has been enabled")

        metrics = OMetrics(this, 29038)

        ClassScanner.scanClasses("one.pkg.om.listener")
            .filter { Listener::class.java.isAssignableFrom(it) }
            .forEach {
                try {
                    val listener = it.getDeclaredConstructor().newInstance() as Listener
                    registerEvents(listener)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            commands.register(
                OmCommand().register().build(),
                "OpenMorph main command",
                listOf("openAsMorph")
            )
        }

        BanManager.load()

        runGlobalTaskTimer(12000L, 12000L) {
            OManager.saveAll()
        }
    }

    override fun onDisable() {
        logger.info("Plugin has been disabled")

        metrics?.shutdown()
        metrics = null

        OManager.playerMorph.values.forEach {
            try {
                it.current?.stop(stopServer = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        OManager.saveAllSync()
        OManager.playerMorph.clear()
    }

    companion object {
        private lateinit var instance: OmMain

        fun getInstance(): OmMain {
            return instance
        }
    }
}
