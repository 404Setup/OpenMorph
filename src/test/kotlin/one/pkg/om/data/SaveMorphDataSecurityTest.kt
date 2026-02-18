package one.pkg.om.data

import one.pkg.om.data.SaveMorphData
import one.pkg.om.data.SavePlayerData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.ArrayList
import java.util.UUID

class SaveMorphDataSecurityTest {

    @Test
    fun testEntityCasingConsistency() {
        val uuid = UUID.randomUUID()
        val data = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())

        // Add entity with lowercase name
        data.addEntity("zombie")

        // Should return true for uppercase check (standard EntityType.name)
        assertTrue(data.hasEntity("ZOMBIE"), "hasEntity should return true for 'ZOMBIE' after adding 'zombie'")

        // Should also return true for lowercase check
        assertTrue(data.hasEntity("zombie"), "hasEntity should return true for 'zombie' after adding 'zombie'")

        // Ensure stored data is uppercase (consistent with blocks)
        assertTrue(data.entities.contains("ZOMBIE"), "Stored entity data should be uppercase")
    }

    @Test
    fun testSkinDataLengthLimit() {
        val uuid = UUID.randomUUID()
        val data = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())

        val longSkin = "A".repeat(20001) // Slightly over 20k limit
        val result = data.addPlayer(SavePlayerData(UUID.randomUUID(), "LongPlayer", longSkin))

        assertFalse(result, "Should reject skin data > 20,000 characters")
        assertEquals(0, data.players.size, "Player should not be added")

        val validSkin = "A".repeat(19000)
        val validResult = data.addPlayer(SavePlayerData(UUID.randomUUID(), "ShortPlayer", validSkin))

        assertTrue(validResult, "Should accept valid skin data length")
        assertEquals(1, data.players.size, "Player should be added")
    }
}
