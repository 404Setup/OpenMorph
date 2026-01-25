/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import io.papermc.paper.event.player.PlayerArmSwingEvent
import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.OManager
import org.bukkit.entity.Damageable
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityAttack : Listener {
    @EventHandler
    fun onSwingHand(e: PlayerArmSwingEvent) {
        val player = e.player
        val morphData = OManager.playerMorph[player]
        val disguise = morphData?.current as? MorphEntity
        disguise?.swingHand()
    }

    @EventHandler
    fun onEntityAttack(e: EntityDamageByEntityEvent) {
        var attacker: Player? = null
        if (e.damager is Player) {
            attacker = e.damager as Player
        } else if (e.damager is Projectile && (e.damager as Projectile).shooter is Player) {
            attacker = (e.damager as Projectile).shooter as Player
        }

        if (attacker != null) {
            val morphData = OManager.playerMorph[attacker]

            val disguise = morphData?.current as? MorphEntity
            if (disguise?.disguisedEntity != null && disguise.disguisedEntity == e.entity) {
                e.isCancelled = true
                val trace = attacker.world.rayTraceEntities(
                    attacker.eyeLocation,
                    attacker.eyeLocation.direction,
                    4.0,
                    0.5
                ) { it != attacker && it != disguise.disguisedEntity }

                if (trace != null && trace.hitEntity != null) {
                    val target = trace.hitEntity
                    if (target is Damageable) {
                        target.damage(e.damage, attacker)
                    }
                }
                return
            }

            morphData?.onAttack(e)
        }
    }
}