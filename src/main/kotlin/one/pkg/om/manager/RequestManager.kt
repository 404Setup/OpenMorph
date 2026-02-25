/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.manager

import one.pkg.om.data.SavePlayerData
import one.pkg.om.utils.runTaskLater
import one.pkg.om.utils.sendFailed
import one.pkg.om.utils.sendSuccess
import one.pkg.om.utils.sendWarning
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object RequestManager {
    private val requests = ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Long>>()
    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private const val TIMEOUT_SECONDS = 30L
    private const val REQUEST_COOLDOWN_MS = 5000L

    fun cleanup(player: Player) {
        requests.remove(player.uniqueId)
        cooldowns.remove(player.uniqueId)
    }

    fun sendRequest(sender: Player, target: Player) {
        val senderId = sender.uniqueId
        val targetId = target.uniqueId

        val lastRequest = cooldowns[senderId] ?: 0L
        val now = System.currentTimeMillis()
        if (now - lastRequest < REQUEST_COOLDOWN_MS) {
            val remaining = (REQUEST_COOLDOWN_MS - (now - lastRequest)) / 1000.0
            sender.sendWarning("Please wait %.1f seconds before sending another request.".format(remaining))
            return
        }

        val targetRequests = requests.computeIfAbsent(targetId) { ConcurrentHashMap() }
        if (targetRequests.containsKey(senderId)) {
            sender.sendSuccess("You have already sent a request to this player. Please wait.")
            return
        }

        val senderData = OManager.playerMorph[sender]
        if (senderData != null && senderData.offlineData.hasPlayer(targetId)) {
            sender.sendWarning("You already have this player in your morph list.")
            return
        }

        targetRequests[senderId] = System.currentTimeMillis() + (TIMEOUT_SECONDS * 1000)
        cooldowns[senderId] = now

        sender.sendSuccess("Request sent to ${target.name}. Valid for 30 seconds.")
        target.sendSuccess("${sender.name} wants to add you to their morph list.")
        target.sendSuccess("Type '/om request yes ${sender.name}' to accept.")
        target.sendSuccess("Type '/om request no ${sender.name}' to deny.")

        runTaskLater(TIMEOUT_SECONDS * 20) {
            val currentRequests = requests[targetId] ?: return@runTaskLater
            if (currentRequests.remove(senderId) != null) {
                val s = Bukkit.getPlayer(senderId)
                val t = Bukkit.getPlayer(targetId)
                s?.sendWarning("Request to ${t?.name ?: "player"} expired.")
                t?.sendWarning("Request from ${s?.name ?: "player"} expired.")
            }
            if (currentRequests.isEmpty()) {
                requests.remove(targetId, currentRequests)
            }
        }
    }

    fun acceptRequest(receiver: Player, senderName: String) {
        val sender = Bukkit.getPlayer(senderName)
        if (sender == null) {
            receiver.sendWarning("Player $senderName is not online.")
            return
        }

        val receiverId = receiver.uniqueId
        val senderId = sender.uniqueId

        val currentRequests = requests[receiverId]
        if (currentRequests == null || !currentRequests.containsKey(senderId)) {
            receiver.sendWarning("No pending request from $senderName.")
            return
        }

        currentRequests.remove(senderId)
        if (currentRequests.isEmpty()) {
            requests.remove(receiverId, currentRequests)
        }

        val senderData = OManager.playerMorph[sender]
        if (senderData != null) {
            val profile = receiver.playerProfile
            val textures = profile.properties.find { it.name == "textures" }
            val skinVal = textures?.value
            val skinSig = textures?.signature

            // Security Fix: Include signature to ensure skin integrity and client-side verification
            val skin = if (skinVal != null) "$skinVal;${skinSig ?: ""}" else ""

            if (senderData.offlineData.addPlayer(SavePlayerData(receiverId, receiver.name, skin))) {
                sender.sendSuccess("${receiver.name} accepted your request.")
                receiver.sendSuccess("You accepted ${sender.name}'s request.")
            } else {
                sender.sendWarning("Could not add ${receiver.name} because your morph list is full.")
                receiver.sendWarning("Could not add to ${sender.name}'s list because it is full.")
            }
        }
    }

    fun denyRequest(receiver: Player, senderName: String) {
        val receiverId = receiver.uniqueId

        val currentRequests = requests[receiverId]
        if (currentRequests.isNullOrEmpty()) {
            receiver.sendWarning("No pending requests.")
            return
        }

        val senderId = currentRequests.keys.find { Bukkit.getPlayer(it)?.name.equals(senderName, ignoreCase = true) }

        if (senderId == null) {
            receiver.sendWarning("No pending request from $senderName.")
            return
        }

        currentRequests.remove(senderId)
        if (currentRequests.isEmpty()) {
            requests.remove(receiverId, currentRequests)
        }
        val sender = Bukkit.getPlayer(senderId)

        sender?.sendFailed("${receiver.name} denied your request.")
        receiver.sendFailed("You denied ${sender?.name ?: "player"}'s request.")
    }
}
