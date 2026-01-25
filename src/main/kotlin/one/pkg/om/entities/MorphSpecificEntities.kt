/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import io.papermc.paper.registry.RegistryKey
import one.pkg.om.utils.getRegistry
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

open class MorphFlyEntity(player: Player, entityType: EntityType) : MorphEntity(player, entityType) {
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

class MorphFrog(player: Player) : MorphEntity(player, EntityType.FROG) {
    override fun onEntitySpawned(entity: Entity) {
        val frog = entity as? Frog ?: return

        frog.variant = getRegistry(RegistryKey.FROG_VARIANT).localRandom()
    }
}

class MorphVillager(player: Player) : MorphEntity(player, EntityType.VILLAGER) {
    override fun onEntitySpawned(entity: Entity) {
        val villager = entity as? Villager ?: return
        villager.villagerType = Registry.VILLAGER_TYPE.localRandom()
        if (ThreadLocalRandom.current().nextInt(1, 10) > 7)
            villager.setBaby()
        villager.profession = Registry.VILLAGER_PROFESSION.localRandom()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphZombie(player: Player) : MorphEntity(player, EntityType.ZOMBIE) {
    override fun onEntitySpawned(entity: Entity) {
        val zombie = entity as? Zombie ?: return

        if (ThreadLocalRandom.current().nextBoolean())
            zombie.setAdult()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphPiglin(player: Player) : MorphEntity(player, EntityType.PIGLIN) {
    override fun onEntitySpawned(entity: Entity) {
        val piglin = entity as? Piglin ?: return

        if (ThreadLocalRandom.current().nextBoolean())
            piglin.setAdult()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphPiglinBrute(player: Player) : MorphEntity(player, EntityType.PIGLIN_BRUTE) {
    override fun onEntitySpawned(entity: Entity) {
        val piglinBrute = entity as? PiglinBrute ?: return

        if (ThreadLocalRandom.current().nextBoolean())
            piglinBrute.setAdult()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphChicken(player: Player) : MorphEntity(player, EntityType.CHICKEN) {
    override fun onEntitySpawned(entity: Entity) {
        val chicken = entity as? Chicken ?: return
        if (ThreadLocalRandom.current().nextBoolean())
            chicken.setAdult()
        chicken.variant = getRegistry(RegistryKey.CHICKEN_VARIANT).localRandom()
        chicken.pose = Pose.STANDING
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphCow(player: Player) : MorphEntity(player, EntityType.COW) {
    override fun onEntitySpawned(entity: Entity) {
        val cow = entity as? Cow ?: return
        if (ThreadLocalRandom.current().nextBoolean())
            cow.setAdult()
        cow.variant = getRegistry(RegistryKey.COW_VARIANT).localRandom()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphZombieVillager(player: Player) : MorphEntity(player, EntityType.ZOMBIE_VILLAGER) {
    override fun onEntitySpawned(entity: Entity) {
        val zombieVillager = entity as? ZombieVillager ?: return
        zombieVillager.villagerType = Registry.VILLAGER_TYPE.localRandom()
        if (ThreadLocalRandom.current().nextInt(1, 10) > 7)
            zombieVillager.setAdult()
        zombieVillager.villagerProfession = Registry.VILLAGER_PROFESSION.localRandom()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphZombifiedPiglin(player: Player) : MorphEntity(player, EntityType.ZOMBIFIED_PIGLIN) {
    override fun onEntitySpawned(entity: Entity) {
        val e = entity as? PigZombie ?: return
        if (ThreadLocalRandom.current().nextBoolean()) e.setAdult()
    }

    override fun canUpdateInventory(): Boolean = true
}

class MorphSkeleton(player: Player) : MorphEntity(player, EntityType.SKELETON) {
    override fun canUpdateInventory(): Boolean = true
}

class MorphBoggedSkeleton(player: Player) : MorphEntity(player, EntityType.BOGGED) {
    override fun canUpdateInventory(): Boolean = true
}

class MorphParchedSkeleton(player: Player) : MorphEntity(player, EntityType.PARCHED) {
    override fun canUpdateInventory(): Boolean = true
}

class MorphStraySkeleton(player: Player) : MorphEntity(player, EntityType.STRAY) {
    override fun canUpdateInventory(): Boolean = true
}

class MorphWitherSkeleton(player: Player) : MorphEntity(player, EntityType.WITHER_SKELETON) {
    override fun canUpdateInventory(): Boolean = true
}

class MorphPig(player: Player) : MorphEntity(player, EntityType.PIG) {
    override fun onEntitySpawned(entity: Entity) {
        val pig = entity as? Pig ?: return
        pig.setSaddle(ThreadLocalRandom.current().nextBoolean())
        pig.variant = getRegistry(RegistryKey.PIG_VARIANT).localRandom()
    }
}

class MorphSheep(player: Player) : MorphEntity(player, EntityType.SHEEP) {
    override fun onEntitySpawned(entity: Entity) {
        val sheep = entity as? Sheep ?: return
        sheep.color = DyeColor.entries.toTypedArray().localRandom()
    }
}

class MorphAllay(player: Player) : MorphFlyEntity(player, EntityType.ALLAY)

class MorphBee(player: Player) : MorphFlyEntity(player, EntityType.BEE)

class MorphVex(player: Player) : MorphFlyEntity(player, EntityType.VEX)

class MorphBat(player: Player) : MorphFlyEntity(player, EntityType.BAT) {
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

class MorphGhast(player: Player) : MorphFlyEntity(player, EntityType.GHAST) {
    override fun start() {
        super.start()

        skills[1] = { p ->
            p.launchProjectile(LargeFireball::class.java).apply {
                yield = 1.0f
            }
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
