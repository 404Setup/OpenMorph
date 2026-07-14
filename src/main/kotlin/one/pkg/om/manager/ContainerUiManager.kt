/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import one.pkg.om.data.MorphIgnored
import one.pkg.om.utils.op
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class GuiHolder : InventoryHolder {
    private lateinit var inventory: Inventory
    val actions = HashMap<Int, (Player) -> Unit>()

    override fun getInventory(): Inventory {
        return inventory
    }

    fun setInventory(inv: Inventory) {
        this.inventory = inv
    }
}

object ContainerUiManager {

    private fun String.miniMessage(): Component {
        return MiniMessage.miniMessage().deserialize(this)
    }

    private fun createItem(material: Material, name: String, lore: List<String> = emptyList()): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.displayName(name.miniMessage())
        meta.lore(lore.map { it.miniMessage() })
        item.itemMeta = meta
        return item
    }

    private fun createPlayerHead(playerName: String, name: String, lore: List<String> = emptyList()): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as? SkullMeta ?: return item
        meta.displayName(name.miniMessage())
        meta.lore(lore.map { it.miniMessage() })
        meta.owningPlayer = Bukkit.getOfflinePlayer(playerName)
        item.itemMeta = meta
        return item
    }

    fun <T> openPaginatedList(
        player: Player,
        title: String,
        list: List<T>,
        page: Int,
        itemCreator: (T) -> ItemStack,
        actionCreator: (T) -> ((Player) -> Unit),
        onBack: (Player) -> Unit
    ) {
        val size = 54
        val holder = GuiHolder()
        val inventory = Bukkit.createInventory(holder, size, title.miniMessage())
        holder.setInventory(inventory)

        val itemsPerPage = 45
        val totalPages = maxOf(1, (list.size + itemsPerPage - 1) / itemsPerPage)
        val currentPage = minOf(totalPages, maxOf(1, page))

        val startIndex = (currentPage - 1) * itemsPerPage
        val endIndex = minOf(list.size, startIndex + itemsPerPage)

        for (i in startIndex until endIndex) {
            val element = list[i]
            val slot = i - startIndex
            inventory.setItem(slot, itemCreator(element))
            holder.actions[slot] = actionCreator(element)
        }

        for (slot in (endIndex - startIndex) until 45) {
            inventory.setItem(slot, createItem(Material.AIR, ""))
        }

        if (currentPage > 1) {
            inventory.setItem(
                45,
                createItem(
                    Material.ARROW,
                    "<yellow>Previous Page",
                    listOf("<gray>Page ${currentPage - 1} / $totalPages")
                )
            )
            holder.actions[45] =
                { openPaginatedList(it, title, list, currentPage - 1, itemCreator, actionCreator, onBack) }
        } else {
            inventory.setItem(45, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(49, createItem(Material.BARRIER, "<red>Back", listOf("<gray>Return to previous menu")))
        holder.actions[49] = onBack

        if (currentPage < totalPages) {
            inventory.setItem(
                53,
                createItem(Material.ARROW, "<yellow>Next Page", listOf("<gray>Page ${currentPage + 1} / $totalPages"))
            )
            holder.actions[53] =
                { openPaginatedList(it, title, list, currentPage + 1, itemCreator, actionCreator, onBack) }
        } else {
            inventory.setItem(53, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        for (slot in 46..48) {
            inventory.setItem(slot, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }
        for (slot in 50..52) {
            inventory.setItem(slot, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        player.openInventory(inventory)
    }

    fun openMenu(player: Player, key: String) {
        when (key.lowercase()) {
            "main" -> openMainMenu(player)
            "morph" -> openMorphTypeSelectionMenu(player)
            "unmorph" -> openUnmorphConfirmMenu(player)
            "read" -> {
                player.performCommand("om read")
                player.closeInventory()
            }

            "info" -> {
                player.performCommand("om info")
                player.closeInventory()
            }

            "get" -> {
                player.performCommand("om get")
                player.closeInventory()
            }

            "lock" -> openLockTypeSelectionMenu(player)
            "unlock" -> openUnlockTypeSelectionMenu(player)
            "append" -> openAppendPlayerSelectionMenu(player)
            "drop" -> openDropTypeSelectionMenu(player)
        }
    }

    fun openMainMenu(player: Player) {
        val size = 27
        val holder = GuiHolder()
        val inventory = Bukkit.createInventory(holder, size, "<aqua><b>OpenMorph Menu".miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(
            10,
            createItem(Material.COMPASS, "<yellow><b>Morph Menu", listOf("<gray>Choose a morph category to morph into"))
        )
        holder.actions[10] = { openMorphTypeSelectionMenu(it) }

        inventory.setItem(
            11,
            createItem(Material.BARRIER, "<red><b>Unmorph", listOf("<gray>Restore your original human form"))
        )
        holder.actions[11] = { openUnmorphConfirmMenu(it) }

        inventory.setItem(
            12,
            createItem(
                Material.BOOK,
                "<gold><b>Unlock Held Block (Read)",
                listOf("<gray>Hold a block and click to unlock its morph")
            )
        )
        holder.actions[12] = {
            it.performCommand("om read")
            it.closeInventory()
        }

        inventory.setItem(
            13,
            createItem(Material.PAPER, "<green><b>My Info", listOf("<gray>View your active morph statistics"))
        )
        holder.actions[13] = {
            it.performCommand("om info")
            it.closeInventory()
        }

        inventory.setItem(
            14,
            createItem(
                Material.SPYGLASS,
                "<aqua><b>Inspect Looking At (Get)",
                listOf("<gray>Get entity/block details you look at")
            )
        )
        holder.actions[14] = {
            it.performCommand("om get")
            it.closeInventory()
        }

        if (player.op()) {
            inventory.setItem(
                15,
                createItem(
                    Material.IRON_DOOR,
                    "<dark_red><b>Lock Morphs (Admin)",
                    listOf("<gray>Restrict certain morphs for players")
                )
            )
            holder.actions[15] = { openLockTypeSelectionMenu(it) }

            inventory.setItem(
                16,
                createItem(
                    Material.OAK_DOOR,
                    "<green><b>Unlock Morphs (Admin)",
                    listOf("<gray>Lift lock restrictions for morphs")
                )
            )
            holder.actions[16] = { openUnlockTypeSelectionMenu(it) }

            inventory.setItem(
                19,
                createItem(
                    Material.ANVIL,
                    "<light_purple><b>Append Morph to Player (Admin)",
                    listOf("<gray>Unlock a morph for a player")
                )
            )
            holder.actions[19] = { openAppendPlayerSelectionMenu(it) }

            inventory.setItem(
                20,
                createItem(
                    Material.LAVA_BUCKET,
                    "<dark_purple><b>Drop Player Morphs (Admin)",
                    listOf("<gray>Remove an unlocked morph from a player")
                )
            )
            holder.actions[20] = { openDropTypeSelectionMenu(it) }
        }

        player.openInventory(inventory)
    }

    fun openMorphTypeSelectionMenu(player: Player) {
        val size = 27
        val holder = GuiHolder()
        val inventory = Bukkit.createInventory(holder, size, "<aqua>Morph Category".miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(
            11,
            createItem(Material.ZOMBIE_SPAWN_EGG, "<yellow>Entity Morphs", listOf("<gray>Morph into unlocked entities"))
        )
        holder.actions[11] = { openMorphEntityList(it, 1) }

        inventory.setItem(
            13,
            createItem(Material.GRASS_BLOCK, "<green>Block Morphs", listOf("<gray>Morph into unlocked blocks"))
        )
        holder.actions[13] = { openMorphBlockList(it, 1) }

        inventory.setItem(
            15,
            createItem(Material.PLAYER_HEAD, "<aqua>Player Morphs", listOf("<gray>Morph into unlocked players"))
        )
        holder.actions[15] = { openMorphPlayerList(it, 1) }

        inventory.setItem(22, createItem(Material.BARRIER, "<red>Back", listOf("<gray>Back to main menu")))
        holder.actions[22] = { openMainMenu(it) }

        player.openInventory(inventory)
    }

    fun openUnmorphConfirmMenu(player: Player) {
        val size = 27
        val holder = GuiHolder()
        val title = if (player.op()) "<red>Unmorph Selection" else "<red>Confirm Unmorph"
        val inventory = Bukkit.createInventory(holder, size, title.miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        val yesItem = createItem(
            Material.RED_STAINED_GLASS_PANE,
            "<red><b>YES, Unmorph Self",
            listOf("<gray>Click to unmorph yourself")
        )
        inventory.setItem(11, yesItem)
        holder.actions[11] = {
            it.closeInventory()
            it.performCommand("om unmorph")
        }

        val noItem = createItem(
            Material.GREEN_STAINED_GLASS_PANE,
            "<green><b>NO, Cancel",
            listOf("<gray>Keep your current morph")
        )
        inventory.setItem(15, noItem)
        holder.actions[15] = { openMainMenu(it) }

        if (player.op()) {
            inventory.setItem(
                4,
                createItem(
                    Material.ANVIL,
                    "<light_purple>Unmorph Another Player",
                    listOf("<gray>Force unmorph an online player")
                )
            )
            holder.actions[4] = { openUnmorphOtherPlayerSelect(it) }
        }

        inventory.setItem(22, createItem(Material.BARRIER, "<red>Back", listOf("<gray>Back to main menu")))
        holder.actions[22] = { openMainMenu(it) }

        player.openInventory(inventory)
    }

    private fun openUnmorphOtherPlayerSelect(player: Player) {
        val onlinePlayers = Bukkit.getOnlinePlayers().toList()
        openPaginatedList(
            player = player,
            title = "<red>Select Player to Unmorph",
            list = onlinePlayers,
            page = 1,
            itemCreator = { p ->
                createPlayerHead(p.name, "<yellow>${p.name}", listOf("<gray>Click to force unmorph this player"))
            },
            actionCreator = { p ->
                { player ->
                    player.closeInventory()
                    player.performCommand("om unmorph ${p.name}")
                }
            },
            onBack = { openUnmorphConfirmMenu(it) }
        )
    }

    fun openMorphEntityList(player: Player, page: Int) {
        val data = OManager.playerMorph[player] ?: return
        val isOp = player.op()
        val list = if (isOp) {
            EntityType.entries.filter { !MorphIgnored.ignored.contains(it) }.map { it.name }
        } else {
            data.offlineData.entities
        }

        openPaginatedList(
            player = player,
            title = "<aqua>Morph Entity List",
            list = list,
            page = page,
            itemCreator = { entityName ->
                val matName = "${entityName.uppercase()}_SPAWN_EGG"
                val mat = Material.matchMaterial(matName) ?: Material.EGG
                createItem(mat, "<yellow>$entityName", listOf("<gray>Click to morph into this entity"))
            },
            actionCreator = { entityName ->
                { p ->
                    if (p.op()) {
                        openAdminActionMenu(p, "entity", entityName)
                    } else {
                        p.closeInventory()
                        p.performCommand("om morph entity $entityName")
                    }
                }
            },
            onBack = { openMorphTypeSelectionMenu(it) }
        )
    }

    fun openMorphBlockList(player: Player, page: Int) {
        val data = OManager.playerMorph[player] ?: return
        val isOp = player.op()
        val list = if (isOp) {
            Material.entries.filter { it.isBlock && it != Material.AIR }.map { it.name }
        } else {
            data.offlineData.blocks
        }

        openPaginatedList(
            player = player,
            title = "<green>Morph Block List",
            list = list,
            page = page,
            itemCreator = { blockName ->
                val mat = Material.getMaterial(blockName.uppercase()) ?: Material.STONE
                createItem(mat, "<yellow>$blockName", listOf("<gray>Click to morph into this block"))
            },
            actionCreator = { blockName ->
                { p ->
                    if (p.op()) {
                        openAdminActionMenu(p, "block", blockName)
                    } else {
                        p.closeInventory()
                        p.performCommand("om morph block $blockName")
                    }
                }
            },
            onBack = { openMorphTypeSelectionMenu(it) }
        )
    }

    fun openMorphPlayerList(player: Player, page: Int) {
        val data = OManager.playerMorph[player] ?: return
        val isOp = player.op()
        val list = if (isOp) {
            Bukkit.getOnlinePlayers().map { it.name }
        } else {
            data.offlineData.players.map { it.name }
        }

        openPaginatedList(
            player = player,
            title = "<aqua>Morph Player List",
            list = list,
            page = page,
            itemCreator = { targetName ->
                createPlayerHead(targetName, "<yellow>$targetName", listOf("<gray>Click to morph into this player"))
            },
            actionCreator = { targetName ->
                { p ->
                    if (p.op()) {
                        openAdminActionMenu(p, "player", targetName)
                    } else {
                        p.closeInventory()
                        p.performCommand("om morph player $targetName")
                    }
                }
            },
            onBack = { openMorphTypeSelectionMenu(it) }
        )
    }

    fun openAdminActionMenu(player: Player, type: String, id: String) {
        val size = 27
        val holder = GuiHolder()
        val inventory = Bukkit.createInventory(holder, size, "<red>Admin: Select Target".miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(
            11,
            createItem(Material.COMPASS, "<yellow>Morph Self", listOf("<gray>Morph yourself into $id"))
        )
        holder.actions[11] = {
            it.closeInventory()
            it.performCommand("om morph $type $id")
        }

        inventory.setItem(
            13,
            createItem(
                Material.PLAYER_HEAD,
                "<aqua>Morph Other Player",
                listOf("<gray>Select another player to morph into $id")
            )
        )
        holder.actions[13] = { openOnlinePlayerSelectorForMorph(it, type, id) }

        inventory.setItem(15, createItem(Material.BARRIER, "<red>Cancel", listOf("<gray>Go back to list")))
        holder.actions[15] = {
            when (type.lowercase()) {
                "entity" -> openMorphEntityList(it, 1)
                "block" -> openMorphBlockList(it, 1)
                "player" -> openMorphPlayerList(it, 1)
            }
        }

        player.openInventory(inventory)
    }

    fun openOnlinePlayerSelectorForMorph(player: Player, type: String, id: String) {
        val onlinePlayers = Bukkit.getOnlinePlayers().toList()
        openPaginatedList(
            player = player,
            title = "<aqua>Select Target Player",
            list = onlinePlayers,
            page = 1,
            itemCreator = { p ->
                createPlayerHead(p.name, "<yellow>${p.name}", listOf("<gray>Click to morph this player"))
            },
            actionCreator = { p ->
                { player ->
                    player.closeInventory()
                    player.performCommand("om morph $type $id ${p.name}")
                }
            },
            onBack = { openAdminActionMenu(it, type, id) }
        )
    }

    fun openLockTypeSelectionMenu(player: Player) {
        val size = 27
        val holder = GuiHolder()
        val inventory = Bukkit.createInventory(holder, size, "<dark_red>Lock Category".miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(
            11,
            createItem(Material.ZOMBIE_SPAWN_EGG, "<yellow>Lock Entity", listOf("<gray>Lock/Unlock entity morphs"))
        )
        holder.actions[11] = { openLockEntityList(it, 1) }

        inventory.setItem(
            13,
            createItem(Material.GRASS_BLOCK, "<green>Lock Block", listOf("<gray>Lock/Unlock block morphs"))
        )
        holder.actions[13] = { openLockBlockList(it, 1) }

        inventory.setItem(
            15,
            createItem(Material.PLAYER_HEAD, "<aqua>Lock Player", listOf("<gray>Lock/Unlock player morphs"))
        )
        holder.actions[15] = { openLockPlayerList(it, 1) }

        inventory.setItem(22, createItem(Material.BARRIER, "<red>Back", listOf("<gray>Back to main menu")))
        holder.actions[22] = { openMainMenu(it) }

        player.openInventory(inventory)
    }

    fun openUnlockTypeSelectionMenu(player: Player) {
        openLockTypeSelectionMenu(player)
    }

    fun openLockEntityList(player: Player, page: Int) {
        val locked = BanManager.getLockedIds("entity")
        val list = EntityType.entries.map { it.name }

        openPaginatedList(
            player = player,
            title = "<dark_red>Lock Entity Morphs",
            list = list,
            page = page,
            itemCreator = { entityName ->
                val isLocked = locked.contains(entityName)
                val status = if (isLocked) "<red>[LOCKED]" else "<green>[UNLOCKED]"
                val matName = "${entityName.uppercase()}_SPAWN_EGG"
                val mat = Material.matchMaterial(matName) ?: Material.EGG
                createItem(mat, "$status <yellow>$entityName", listOf("<gray>Click to toggle lock status"))
            },
            actionCreator = { entityName ->
                { p ->
                    val isLocked = BanManager.isLocked("entity", entityName)
                    if (isLocked) {
                        p.performCommand("om unlock entity $entityName")
                    } else {
                        p.performCommand("om lock entity $entityName")
                    }
                    openLockEntityList(p, page)
                }
            },
            onBack = { openLockTypeSelectionMenu(it) }
        )
    }

    fun openLockBlockList(player: Player, page: Int) {
        val locked = BanManager.getLockedIds("block")
        val list = Material.entries.filter { it.isBlock && it != Material.AIR }.map { it.name }

        openPaginatedList(
            player = player,
            title = "<dark_red>Lock Block Morphs",
            list = list,
            page = page,
            itemCreator = { blockName ->
                val isLocked = locked.contains(blockName)
                val status = if (isLocked) "<red>[LOCKED]" else "<green>[UNLOCKED]"
                val mat = Material.getMaterial(blockName.uppercase()) ?: Material.STONE
                createItem(mat, "$status <yellow>$blockName", listOf("<gray>Click to toggle lock status"))
            },
            actionCreator = { blockName ->
                { p ->
                    val isLocked = BanManager.isLocked("block", blockName)
                    if (isLocked) {
                        p.performCommand("om unlock block $blockName")
                    } else {
                        p.performCommand("om lock block $blockName")
                    }
                    openLockBlockList(p, page)
                }
            },
            onBack = { openLockTypeSelectionMenu(it) }
        )
    }

    fun openLockPlayerList(player: Player, page: Int) {
        val locked = BanManager.getLockedIds("player")
        val list = Bukkit.getOnlinePlayers().map { it.name }

        openPaginatedList(
            player = player,
            title = "<dark_red>Lock Player Morphs",
            list = list,
            page = page,
            itemCreator = { targetName ->
                val isLocked = locked.contains(targetName)
                val status = if (isLocked) "<red>[LOCKED]" else "<green>[UNLOCKED]"
                createPlayerHead(targetName, "$status <yellow>$targetName", listOf("<gray>Click to toggle lock status"))
            },
            actionCreator = { targetName ->
                { p ->
                    val isLocked = BanManager.isLocked("player", targetName)
                    if (isLocked) {
                        p.performCommand("om unlock player $targetName")
                    } else {
                        p.performCommand("om lock player $targetName")
                    }
                    openLockPlayerList(p, page)
                }
            },
            onBack = { openLockTypeSelectionMenu(it) }
        )
    }

    fun openAppendPlayerSelectionMenu(player: Player) {
        val onlinePlayers = Bukkit.getOnlinePlayers().toList()
        openPaginatedList(
            player = player,
            title = "<light_purple>Append: Select Target",
            list = onlinePlayers,
            page = 1,
            itemCreator = { p ->
                createPlayerHead(
                    p.name,
                    "<yellow>${p.name}",
                    listOf("<gray>Click to manage morphs to append to this player")
                )
            },
            actionCreator = { p ->
                { openAppendCategoryMenu(it, p.name) }
            },
            onBack = { openMainMenu(it) }
        )
    }

    fun openAppendCategoryMenu(player: Player, targetPlayerName: String) {
        val size = 27
        val holder = GuiHolder()
        val inventory =
            Bukkit.createInventory(holder, size, "<light_purple>Append Category to $targetPlayerName".miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(
            11,
            createItem(
                Material.ZOMBIE_SPAWN_EGG,
                "<yellow>Append Entity",
                listOf("<gray>Unlock an entity morph for $targetPlayerName")
            )
        )
        holder.actions[11] = { openAppendEntityList(it, targetPlayerName, 1) }

        inventory.setItem(
            13,
            createItem(
                Material.GRASS_BLOCK,
                "<green>Append Block",
                listOf("<gray>Unlock a block morph for $targetPlayerName")
            )
        )
        holder.actions[13] = { openAppendBlockList(it, targetPlayerName, 1) }

        inventory.setItem(
            15,
            createItem(
                Material.PLAYER_HEAD,
                "<aqua>Append Player",
                listOf("<gray>Unlock a player morph for $targetPlayerName")
            )
        )
        holder.actions[15] = { openAppendPlayerList(it, targetPlayerName, 1) }

        inventory.setItem(22, createItem(Material.BARRIER, "<red>Back", listOf("<gray>Back to player list")))
        holder.actions[22] = { openAppendPlayerSelectionMenu(it) }

        player.openInventory(inventory)
    }

    fun openAppendEntityList(player: Player, targetPlayerName: String, page: Int) {
        val list = EntityType.entries.filter { !MorphIgnored.ignored.contains(it) }.map { it.name }
        openPaginatedList(
            player = player,
            title = "<light_purple>Append Entity to $targetPlayerName",
            list = list,
            page = page,
            itemCreator = { entityName ->
                val matName = "${entityName.uppercase()}_SPAWN_EGG"
                val mat = Material.matchMaterial(matName) ?: Material.EGG
                createItem(mat, "<yellow>$entityName", listOf("<gray>Click to append to $targetPlayerName"))
            },
            actionCreator = { entityName ->
                { p ->
                    p.closeInventory()
                    p.performCommand("om append $targetPlayerName entity $entityName")
                }
            },
            onBack = { openAppendCategoryMenu(it, targetPlayerName) }
        )
    }

    fun openAppendBlockList(player: Player, targetPlayerName: String, page: Int) {
        val list = Material.entries.filter { it.isBlock && it != Material.AIR }.map { it.name }
        openPaginatedList(
            player = player,
            title = "<light_purple>Append Block to $targetPlayerName",
            list = list,
            page = page,
            itemCreator = { blockName ->
                val mat = Material.getMaterial(blockName.uppercase()) ?: Material.STONE
                createItem(mat, "<yellow>$blockName", listOf("<gray>Click to append to $targetPlayerName"))
            },
            actionCreator = { blockName ->
                { p ->
                    p.closeInventory()
                    p.performCommand("om append $targetPlayerName block $blockName")
                }
            },
            onBack = { openAppendCategoryMenu(it, targetPlayerName) }
        )
    }

    fun openAppendPlayerList(player: Player, targetPlayerName: String, page: Int) {
        val list = Bukkit.getOnlinePlayers().map { it.name }
        openPaginatedList(
            player = player,
            title = "<light_purple>Append Player to $targetPlayerName",
            list = list,
            page = page,
            itemCreator = { playerName ->
                createPlayerHead(
                    playerName,
                    "<yellow>$playerName",
                    listOf("<gray>Click to append to $targetPlayerName")
                )
            },
            actionCreator = { playerName ->
                { p ->
                    p.closeInventory()
                    p.performCommand("om append $targetPlayerName player $playerName")
                }
            },
            onBack = { openAppendCategoryMenu(it, targetPlayerName) }
        )
    }

    fun openDropTypeSelectionMenu(player: Player) {
        val onlinePlayers = Bukkit.getOnlinePlayers().toList()
        openPaginatedList(
            player = player,
            title = "<dark_purple>Drop: Select Target",
            list = onlinePlayers,
            page = 1,
            itemCreator = { p ->
                createPlayerHead(p.name, "<yellow>${p.name}", listOf("<gray>Click to drop morphs from this player"))
            },
            actionCreator = { p ->
                { openDropCategoryMenu(it, p.name) }
            },
            onBack = { openMainMenu(it) }
        )
    }

    fun openDropCategoryMenu(player: Player, targetPlayerName: String) {
        val size = 27
        val holder = GuiHolder()
        val inventory =
            Bukkit.createInventory(holder, size, "<dark_purple>Drop Category from $targetPlayerName".miniMessage())
        holder.setInventory(inventory)

        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        inventory.setItem(
            11,
            createItem(
                Material.ZOMBIE_SPAWN_EGG,
                "<yellow>Drop Entity",
                listOf("<gray>Remove an entity morph from $targetPlayerName")
            )
        )
        holder.actions[11] = { openDropEntityList(it, targetPlayerName, 1) }

        inventory.setItem(
            13,
            createItem(
                Material.GRASS_BLOCK,
                "<green>Drop Block",
                listOf("<gray>Remove a block morph from $targetPlayerName")
            )
        )
        holder.actions[13] = { openDropBlockList(it, targetPlayerName, 1) }

        inventory.setItem(
            15,
            createItem(
                Material.PLAYER_HEAD,
                "<aqua>Drop Player",
                listOf("<gray>Remove a player morph from $targetPlayerName")
            )
        )
        holder.actions[15] = { openDropPlayerList(it, targetPlayerName, 1) }

        inventory.setItem(22, createItem(Material.BARRIER, "<red>Back", listOf("<gray>Back to player list")))
        holder.actions[22] = { openDropTypeSelectionMenu(it) }

        player.openInventory(inventory)
    }

    fun openDropEntityList(player: Player, targetPlayerName: String, page: Int) {
        val target = Bukkit.getPlayer(targetPlayerName)
        val data = target?.let { OManager.playerMorph[it] }
        val list = data?.offlineData?.entities ?: emptyList()

        openPaginatedList(
            player = player,
            title = "<dark_purple>Drop Entity from $targetPlayerName",
            list = list,
            page = page,
            itemCreator = { entityName ->
                val matName = "${entityName.uppercase()}_SPAWN_EGG"
                val mat = Material.matchMaterial(matName) ?: Material.EGG
                createItem(mat, "<yellow>$entityName", listOf("<gray>Click to drop from $targetPlayerName"))
            },
            actionCreator = { entityName ->
                { p ->
                    p.closeInventory()
                    p.performCommand("om drop $targetPlayerName entity $entityName")
                }
            },
            onBack = { openDropCategoryMenu(it, targetPlayerName) }
        )
    }

    fun openDropBlockList(player: Player, targetPlayerName: String, page: Int) {
        val target = Bukkit.getPlayer(targetPlayerName)
        val data = target?.let { OManager.playerMorph[it] }
        val list = data?.offlineData?.blocks ?: emptyList()

        openPaginatedList(
            player = player,
            title = "<dark_purple>Drop Block from $targetPlayerName",
            list = list,
            page = page,
            itemCreator = { blockName ->
                val mat = Material.getMaterial(blockName.uppercase()) ?: Material.STONE
                createItem(mat, "<yellow>$blockName", listOf("<gray>Click to drop from $targetPlayerName"))
            },
            actionCreator = { blockName ->
                { p ->
                    p.closeInventory()
                    p.performCommand("om drop $targetPlayerName block $blockName")
                }
            },
            onBack = { openDropCategoryMenu(it, targetPlayerName) }
        )
    }

    fun openDropPlayerList(player: Player, targetPlayerName: String, page: Int) {
        val target = Bukkit.getPlayer(targetPlayerName)
        val data = target?.let { OManager.playerMorph[it] }
        val list = data?.offlineData?.players?.map { it.name } ?: emptyList()

        openPaginatedList(
            player = player,
            title = "<dark_purple>Drop Player from $targetPlayerName",
            list = list,
            page = page,
            itemCreator = { playerName ->
                createPlayerHead(
                    playerName,
                    "<yellow>$playerName",
                    listOf("<gray>Click to drop from $targetPlayerName")
                )
            },
            actionCreator = { playerName ->
                { p ->
                    p.closeInventory()
                    p.performCommand("om drop $targetPlayerName player $playerName")
                }
            },
            onBack = { openDropCategoryMenu(it, targetPlayerName) }
        )
    }
}
