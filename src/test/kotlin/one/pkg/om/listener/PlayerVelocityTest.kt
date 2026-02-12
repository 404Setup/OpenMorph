/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.entities.MorphEntity
import one.pkg.om.manager.OManager
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerVelocityEvent
import org.bukkit.util.Vector
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.UUID

class PlayerVelocityTest {

    @AfterEach
    fun cleanup() {
        OManager.playerMorph.clear()
    }

    private fun createMockPlayer(): Player {
        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
            object : InvocationHandler {
                override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
                     if (method.name == "getUniqueId") return UUID.randomUUID()
                     if (method.name == "getName") return "TestPlayer"
                     if (method.name == "getEntityId") return 1
                     if (method.name == "hashCode") return 12345
                     if (method.name == "equals") return proxy === args?.get(0)
                     return null
                }
            }
        ) as Player
    }

    private class TestMorphEntity(player: Player, override val hasKnockback: Boolean) : MorphEntity(player, EntityType.ZOMBIE) {
        override fun start() {}
        override fun stop(clearData: Boolean, stopServer: Boolean) {}
    }

    private fun createMockSaveData(player: Player): SaveMorphData {
        val data = SaveMorphData(player.uniqueId, mutableListOf(), mutableListOf(), mutableListOf())
        data.updatePlayer(player)
        return data
    }

    @Test
    fun `test knockback prevented when hasKnockback is false`() {
        val player = createMockPlayer()
        val morph = TestMorphEntity(player, false)
        val saveData = createMockSaveData(player)
        val onlineData = OnlineMorphData(morph, saveData)

        OManager.playerMorph[player] = onlineData

        val event = PlayerVelocityEvent(player, Vector(1.0, 1.0, 1.0))
        val listener = PlayerVelocity()

        listener.onVelocity(event)

        assertTrue(event.isCancelled, "Event should be cancelled when hasKnockback is false")
    }

    @Test
    fun `test knockback allowed when hasKnockback is true`() {
        val player = createMockPlayer()
        val morph = TestMorphEntity(player, true)
        val saveData = createMockSaveData(player)
        val onlineData = OnlineMorphData(morph, saveData)

        OManager.playerMorph[player] = onlineData

        val event = PlayerVelocityEvent(player, Vector(1, 1, 1))
        val listener = PlayerVelocity()

        listener.onVelocity(event)

        assertFalse(event.isCancelled, "Event should not be cancelled when hasKnockback is true")
    }

    @Test
    fun `test knockback allowed when not morphed`() {
        val player = createMockPlayer()
        // No morph data in OManager

        val event = PlayerVelocityEvent(player, Vector(1, 1, 1))
        val listener = PlayerVelocity()

        listener.onVelocity(event)

        assertFalse(event.isCancelled, "Event should not be cancelled when player is not morphed")
    }
}
