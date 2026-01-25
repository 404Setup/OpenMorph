/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.entities

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.entity.Player

import one.pkg.om.OmMain

class MorphPlayer(player: Player, val targetName: String, val skinValue: String?, val skinSignature: String?) : MorphEntities(player) {
    private var originalProfile: PlayerProfile? = null

    override fun start() {
        originalProfile = player.playerProfile
        
        val newProfile = Bukkit.createProfile(targetName)
        if (skinValue != null && skinSignature != null) {
            newProfile.setProperty(ProfileProperty("textures", skinValue, skinSignature))
        }
        
        player.playerProfile = newProfile
        refreshPlayer()
    }

    override fun stop(clearData: Boolean, stopServer: Boolean) {
        if (originalProfile != null) {
            player.playerProfile = originalProfile!!
        }
        refreshPlayer()
    }
    
    private fun refreshPlayer() {
        Bukkit.getOnlinePlayers().forEach { 
             if (it != player) {
                 it.hidePlayer(OmMain.getInstance(), player)
                 it.showPlayer(OmMain.getInstance(), player)
             }
        }
    }
}
