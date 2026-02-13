package one.pkg.om.utils

import org.bukkit.Material
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RestrictedBlocksTest {

    @Test
    fun `test dangerous blocks are restricted`() {
        val dangerousBlocks = listOf(
            Material.TNT,
            Material.LAVA,
            Material.WATER,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.DISPENSER,
            Material.DROPPER,
            Material.OBSERVER,
            Material.PISTON,
            Material.STICKY_PISTON,
            Material.HOPPER
        )

        val missing = dangerousBlocks.filter { !RestrictedBlocks.isRestricted(it) }

        assertTrue(missing.isEmpty(), "The following dangerous blocks are not restricted: $missing")
    }

    @Test
    fun `test existing restricted blocks are still restricted`() {
        assertTrue(RestrictedBlocks.isRestricted(Material.BEDROCK))
        assertTrue(RestrictedBlocks.isRestricted(Material.COMMAND_BLOCK))
    }
}
