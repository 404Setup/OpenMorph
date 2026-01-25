/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.OManager
import one.pkg.om.data.SavePlayerData
import one.pkg.om.utils.sendSuccess
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class EntityDeath : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityDeath(e: EntityDeathEvent) {
        val killer = e.entity.killer ?: return
        val victim = e.entity

        val data = OManager.playerMorph[killer] ?: return

        if (victim is Player) {
            if (!data.offlineData.hasPlayer(victim.uniqueId)) {
                val profile = victim.playerProfile
                val textureProperty = profile.properties.firstOrNull { it.name == "textures" }
                val skinValue = textureProperty?.value ?: ""

                val playerData = SavePlayerData(victim.uniqueId, victim.name, skinValue)

                data.offlineData.addPlayer(playerData)
                killer.sendSuccess("You unlocked morph for player: ${victim.name}")
            }
        } else {
            val typeName = victim.type.name
            if (!data.offlineData.hasEntity(typeName)) {
                data.offlineData.addEntity(typeName)
                killer.sendSuccess("You unlocked morph for entity: $typeName")
            }
        }
    }
}
