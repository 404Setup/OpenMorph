/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import one.pkg.om.utils.localRandom
import org.bukkit.DyeColor
import org.bukkit.GameMode
import org.bukkit.Registry
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom

class MorphIronGolem(player: Player) : MorphEntity(player, EntityType.IRON_GOLEM) {
    override val hasKnockback: Boolean = false

    override fun start() {
        super.start()
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Int.MAX_VALUE, 0, false, false))
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        super.stop(clearData, stopServer)
        player.removePotionEffect(PotionEffectType.SLOWNESS)
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        super.onAttack(event)

        event.entity.velocity = event.entity.velocity.add(Vector(0.0, 1.0, 0.0))
    }
}

class MorphVillager(player: Player) : MorphEntity(player, EntityType.VILLAGER) {
    override fun onEntitySpawned(entity: Entity) {
        val villager = entity as? Villager ?: return
        villager.villagerType = Registry.VILLAGER_TYPE.toList().localRandom()
        if (ThreadLocalRandom.current().nextInt(1, 10) > 7)
            villager.setBaby()
        villager.profession = Registry.VILLAGER_PROFESSION.toList().localRandom()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphPig(player: Player) : MorphEntity(player, EntityType.PIG) {
    override fun onEntitySpawned(entity: Entity) {
        val pig = entity as? Pig ?: return
        pig.setSaddle(ThreadLocalRandom.current().nextBoolean())
    }
}

class MorphSheep(player: Player) : MorphEntity(player, EntityType.SHEEP) {
    override fun onEntitySpawned(entity: Entity) {
        val sheep = entity as? Sheep ?: return
        sheep.color = DyeColor.entries.toTypedArray().localRandom()
    }
}

class MorphAllay(player: Player) : MorphEntity(player, EntityType.ALLAY) {
    override fun start() {
        super.start()
        player.allowFlight = true
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        super.stop(clearData, stopServer)
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            player.allowFlight = false
            player.isFlying = false
        }
    }
}

class MorphBat(player: Player) : MorphEntity(player, EntityType.BAT) {
    override fun onEntitySpawned(entity: Entity) {
        val bat = entity as? Bat ?: return
        bat.isAwake = true
    }

    override fun start() {
        super.start()
        player.allowFlight = true
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        super.stop(clearData, stopServer)
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            player.allowFlight = false
            player.isFlying = false
        }
    }
}

class MorphGhast(player: Player) : MorphEntity(player, EntityType.GHAST) {
    override fun start() {
        super.start()
        player.allowFlight = true

        skills[1] = { p ->
            p.launchProjectile(LargeFireball::class.java).apply {
                yield = 1.0f
            }
        }
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        super.stop(clearData, stopServer)
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            player.allowFlight = false
            player.isFlying = false
        }
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager is LargeFireball) {
            val entity = event.entity
            if (entity is LivingEntity) {
                entity.health = 0.0
            } else {
                if (entity is Damageable) {
                    entity.damage(9999.0)
                }
            }
        }
    }
}
