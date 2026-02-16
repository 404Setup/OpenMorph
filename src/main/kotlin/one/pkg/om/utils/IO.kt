/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.utils

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import one.pkg.om.OmMain
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Registry
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

internal var testPluginInstance: Plugin? = null
private fun getPlugin(): Plugin = testPluginInstance ?: OmMain.getInstance()

operator fun File.div(other: String): File = this.resolve(other)

fun runAsync(task: Consumer<ScheduledTask>): ScheduledTask =
    Bukkit.getAsyncScheduler().runNow(getPlugin(), task)

fun runGlobalTaskTimer(initialDelayTicks: Long, periodTicks: Long, task: Consumer<ScheduledTask>) =
    Bukkit.getGlobalRegionScheduler().runAtFixedRate(
        getPlugin(), task, initialDelayTicks, periodTicks
    )

fun runGlobalTask(task: Consumer<ScheduledTask>) =
    Bukkit.getGlobalRegionScheduler().run(getPlugin(), task)

fun runTaskLater(delayTicks: Long, task: Consumer<ScheduledTask>) =
    Bukkit.getGlobalRegionScheduler().runDelayed(getPlugin(), task, delayTicks)

fun Entity.runDelayed(retired: Runnable?, delayTicks: Long, task: Consumer<ScheduledTask>) =
    this.scheduler.runDelayed(getPlugin(), task, retired, delayTicks)

fun Entity.runDelayed(delayTicks: Long, task: Consumer<ScheduledTask>) =
    runDelayed(null, delayTicks, task)

fun Entity.runAs(retired: Runnable?, task: Consumer<ScheduledTask>) =
    this.scheduler.run(getPlugin(), task, retired)

fun Entity.runAs(task: Consumer<ScheduledTask>) =
    this.scheduler.run(getPlugin(), task, null)

fun <T> Array<out T>.localRandom(): T {
    if (isEmpty())
        throw NoSuchElementException("Array is empty.")
    return get(ThreadLocalRandom.current().nextInt(size))
}

fun <T> Collection<T>.localRandom(): T {
    if (isEmpty())
        throw NoSuchElementException("Collection is empty.")
    return elementAt(ThreadLocalRandom.current().nextInt(size))
}

fun <T : Keyed> Registry<T>.localRandom(): T {
    if (this.size() < 1)
        throw NoSuchElementException("Registry is empty.")
    return this.toList().localRandom()
}