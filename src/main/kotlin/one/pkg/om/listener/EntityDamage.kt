/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.OManager
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffectType

class EntityDamage : Listener {
    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (e.entity is Player) {
            val player = e.entity as Player
            OManager.playerMorph[player]?.onDamage(e)
        } else {
            val player = OManager.entityToPlayerMap[e.entity.uniqueId] ?: return
            if (e.cause != EntityDamageEvent.DamageCause.CUSTOM) {
                if (e is EntityDamageByEntityEvent) {
                    var damage = e.damage
                    val damager = e.damager
                    if (damager is Player) {
                        val attr = damager.getAttribute(Attribute.ATTACK_DAMAGE)
                        if (attr != null && attr.value > damage) {
                            damage = attr.value
                        }

                        val isCrit = damager.fallDistance > 0.0F &&
                                !damager.isOnGround &&
                                !damager.isInsideVehicle &&
                                !damager.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                                !Tag.CLIMBABLE.isTagged(damager.location.block.type) &&
                                !damager.isSprinting

                        if (isCrit) {
                            damage *= 1.5
                        }
                    }
                    player.damage(damage, damager)
                } else {
                    player.damage(e.damage)
                }
                e.isCancelled = true
            }
        }
    }
}
