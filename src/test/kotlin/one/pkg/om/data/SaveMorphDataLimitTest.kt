package one.pkg.om.data

import one.pkg.om.data.SaveMorphData
import one.pkg.om.data.SavePlayerData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.ArrayList
import java.util.UUID

class SaveMorphDataLimitTest {

    @Test
    fun testMorphLimits() {
        val uuid = UUID.randomUUID()
        val data = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())

        // Test Blocks Limit
        for (i in 0 until 100) {
            assertTrue(data.addBlock("BLOCK_$i"), "Should add block within limit")
        }
        // Try adding 101st block
        assertFalse(data.addBlock("BLOCK_OVERFLOW"), "Should fail to add block when limit reached")

        assertEquals(100, data.blocks.size, "Blocks size should be capped at 100")

        // Test Entities Limit
        for (i in 0 until 100) {
            assertTrue(data.addEntity("ENTITY_$i"), "Should add entity within limit")
        }
        assertFalse(data.addEntity("ENTITY_OVERFLOW"), "Should fail to add entity when limit reached")

        assertEquals(100, data.entities.size, "Entities size should be capped at 100")

        // Test Players Limit
        for (i in 0 until 100) {
            assertTrue(data.addPlayer(SavePlayerData(UUID.randomUUID(), "PLAYER_$i", "")), "Should add player within limit")
        }
        assertFalse(data.addPlayer(SavePlayerData(UUID.randomUUID(), "PLAYER_OVERFLOW", "")), "Should fail to add player when limit reached")

        assertEquals(100, data.players.size, "Players size should be capped at 100")
    }
}
