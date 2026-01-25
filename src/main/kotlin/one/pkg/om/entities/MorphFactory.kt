/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

object MorphFactory {
    fun create(player: Player, type: EntityType): MorphEntity {
        return when (type) {
            EntityType.ALLAY -> MorphAllay(player)
            EntityType.BAT -> MorphBat(player)
            EntityType.BEE -> MorphBee(player)
            EntityType.COW -> MorphCow(player)
            EntityType.CHICKEN -> MorphChicken(player)
            EntityType.WITHER_SKELETON -> MorphWitherSkeleton(player)
            EntityType.ZOMBIE -> MorphZombie(player)
            EntityType.ZOMBIE_VILLAGER -> MorphZombieVillager(player)
            EntityType.ZOMBIFIED_PIGLIN -> MorphZombifiedPiglin(player)
            EntityType.PIGLIN -> MorphPiglin(player)
            EntityType.PIGLIN_BRUTE -> MorphPiglinBrute(player)
            EntityType.FROG -> MorphFrog(player)
            EntityType.VILLAGER -> MorphVillager(player)
            EntityType.VEX -> MorphVex(player)
            EntityType.IRON_GOLEM -> MorphIronGolem(player)
            EntityType.PIG -> MorphPig(player)
            EntityType.SKELETON -> MorphSkeleton(player)
            EntityType.BOGGED -> MorphBoggedSkeleton(player)
            EntityType.PARCHED -> MorphParchedSkeleton(player)
            EntityType.STRAY -> MorphStraySkeleton(player)
            EntityType.SHEEP -> MorphSheep(player)
            EntityType.GHAST -> MorphGhast(player)
            else -> MorphEntity(player, type)
        }
    }
}
