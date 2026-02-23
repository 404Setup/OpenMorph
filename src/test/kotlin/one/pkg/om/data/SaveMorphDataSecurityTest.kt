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

    @Test
    fun testLimitsInputLength() {
        val uuid = UUID.randomUUID()
        val data = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())

        // Test Block limit (64)
        val longBlock = "A".repeat(65)
        assertFalse(data.addBlock(longBlock), "Should reject block name > 64 chars")

        val validBlock = "A".repeat(64)
        assertTrue(data.addBlock(validBlock), "Should accept block name <= 64 chars")

        // Test Entity limit (64)
        val longEntity = "A".repeat(65)
        assertFalse(data.addEntity(longEntity), "Should reject entity type > 64 chars")

        val validEntity = "A".repeat(64)
        assertTrue(data.addEntity(validEntity), "Should accept entity type <= 64 chars")

        // Test Player Name limit (16)
        val longPlayerName = "A".repeat(17)
        assertFalse(data.addPlayer(SavePlayerData(UUID.randomUUID(), longPlayerName, "")), "Should reject player name > 16 chars")

        val validPlayerName = "A".repeat(16)
        assertTrue(data.addPlayer(SavePlayerData(UUID.randomUUID(), validPlayerName, "")), "Should accept player name <= 16 chars")
    }

    @Test
    fun testActiveMorphLengthLimit() {
        val uuid = UUID.randomUUID()
        val data = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())

        val longName = "A".repeat(1000)
        data.setActiveMorph("PLAYER", longName, null, null)

        // Expectation: Should NOT be set due to length limit
        // Currently this will fail (it will be set)
        assertEquals(null, data.activeMorphName, "Should reject active morph name > 16 chars for PLAYER")

        val validName = "ValidPlayer"
        data.setActiveMorph("PLAYER", validName, null, null)
        assertEquals(validName, data.activeMorphName, "Should accept valid active morph name")
    }
}
