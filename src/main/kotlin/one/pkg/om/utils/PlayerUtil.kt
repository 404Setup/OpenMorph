/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.utils

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import one.pkg.om.manager.OManager
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.Registry
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

fun Player.scheduleResetHealth(delayTicks: Long = 5L, stopServer: Boolean = false) {
    if (stopServer) resetHealth()
    else runDelayed(delayTicks) {
        if (health < 0.1) {
            scheduleResetHealth(5L)
        } else {
            resetHealth()
        }
    }
}

fun Player.resetHealth() {
    val attr = Attribute.MAX_HEALTH
    val originalMaxHealth = OManager.playerMorph[player]?.offlineData?.originalMaxHealth ?: 20.0
    getAttribute(attr)!!.baseValue = originalMaxHealth
    getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 0.0
    health = health.coerceAtMost(originalMaxHealth)
    healthScale = originalMaxHealth
    sendHealthUpdate()
}

fun Location.isIt(other: Location): Boolean {
    return world.isIt(other) && x == other.x && y == other.y && z == other.z && yaw == other.yaw && pitch == other.pitch
}

fun World.isIt(other: World): Boolean {
    return uid == other.uid
}

fun World.isIt(other: Location): Boolean {
    return isIt(other.world)
}

fun <T : Keyed> getRegistry(registryKey: RegistryKey<T>): Registry<T> {
    return RegistryAccess.registryAccess().getRegistry(registryKey)
}