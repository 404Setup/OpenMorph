/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.data

import org.bukkit.entity.EntityType

object MorphIgnored {
    val ignored: MutableSet<EntityType> = mutableSetOf(
        EntityType.BLOCK_DISPLAY,
        EntityType.ITEM_DISPLAY,
        EntityType.TEXT_DISPLAY,
        EntityType.MINECART,
        EntityType.TNT_MINECART,
        EntityType.CHEST_MINECART,
        EntityType.HOPPER_MINECART,
        EntityType.FURNACE_MINECART,
        EntityType.COMMAND_BLOCK_MINECART,
        EntityType.SPAWNER_MINECART,
        EntityType.FISHING_BOBBER,
        EntityType.TNT,
        EntityType.SPLASH_POTION,
        EntityType.FIREBALL,
        EntityType.SMALL_FIREBALL,
        EntityType.SNOWBALL,
        EntityType.FIREWORK_ROCKET
    )
}