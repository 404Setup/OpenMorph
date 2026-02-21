package one.pkg.om.listener

import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.entities.MorphBlock
import one.pkg.om.manager.BlockPosition
import one.pkg.om.manager.OManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy
import java.util.UUID

class BlockBreakTest {

    private lateinit var player: Player
    private lateinit var world: World
    private lateinit var block: Block
    private lateinit var morphBlock: MorphBlock
    private var damageCalled = false
    private var damageAmount = 0.0

    @BeforeEach
    fun setUp() {
        // Mock Player
        val playerUuid = UUID.randomUUID()
        player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getUniqueId" -> playerUuid
                "hashCode" -> playerUuid.hashCode()
                "equals" -> args != null && args.isNotEmpty() && args[0] === proxy
                "toString" -> "MockPlayer($playerUuid)"
                "getEntityId" -> 123
                "getWorld" -> world
                "getLocation" -> Location(world, 10.0, 20.0, 30.0)
                "damage" -> {
                    damageCalled = true
                    if (args != null && args.isNotEmpty() && args[0] is Double) {
                        damageAmount = args[0] as Double
                    }
                    null
                }
                "getAttribute" -> null // For MorphEntity constructor
                else -> null
            }
        } as Player

        // Mock World
        val worldUuid = UUID.randomUUID()
        world = Proxy.newProxyInstance(
            World::class.java.classLoader,
            arrayOf(World::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getUID" -> worldUuid
                "getName" -> "world"
                "hashCode" -> worldUuid.hashCode()
                "equals" -> args != null && args.isNotEmpty() && args[0] === world
                "toString" -> "MockWorld($worldUuid)"
                else -> null
            }
        } as World

        // Mock Block
        block = Proxy.newProxyInstance(
            Block::class.java.classLoader,
            arrayOf(Block::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getWorld" -> world
                "getX" -> 10
                "getY" -> 20
                "getZ" -> 30
                "getLocation" -> Location(world, 10.0, 20.0, 30.0)
                "getType" -> Material.STONE
                else -> null
            }
        } as Block

        // Setup MorphBlock
        // We instantiate it directly. We don't call start(), so side effects like spawnDisplay are avoided.
        morphBlock = MorphBlock(player, Material.STONE)

        // Set solidifiedLocation via reflection
        val solidifiedLocationField = MorphBlock::class.java.getDeclaredField("solidifiedLocation")
        solidifiedLocationField.isAccessible = true
        solidifiedLocationField.set(morphBlock, Location(world, 10.0, 20.0, 30.0))

        // Set isSolidified
        val isSolidifiedField = MorphBlock::class.java.getDeclaredField("isSolidified")
        isSolidifiedField.isAccessible = true
        isSolidifiedField.set(morphBlock, true)

        // Setup OManager
        val saveMorphData = SaveMorphData(
            playerUuid,
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )
        val onlineMorphData = OnlineMorphData(morphBlock, saveMorphData)

        OManager.playerMorph[player] = onlineMorphData

        // Populate blockMorphs for future compatibility (optimized version)
        OManager.blockMorphs[BlockPosition("world", 10, 20, 30)] = player
    }

    @AfterEach
    fun tearDown() {
        OManager.playerMorph.clear()
        OManager.blockMorphs.clear()
    }

    @Test
    fun `onBreak should damage player if morphed as block at location`() {
        // Arrange
        val event = BlockBreakEvent(block, player) // Player here is breaker

        val listener = BlockBreak()

        // Act
        listener.onBreak(event)

        // Assert
        assertTrue(damageCalled, "Player should be damaged")
        assertEquals(5.0, damageAmount, "Damage amount should be 5.0")
        assertFalse(event.isDropItems, "Block should not drop items")
    }

    @Test
    fun `onBreak should not damage player if location does not match`() {
        // Arrange: Block at different location
        val otherBlock = Proxy.newProxyInstance(
            Block::class.java.classLoader,
            arrayOf(Block::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getWorld" -> world
                "getX" -> 11 // Different X
                "getY" -> 20
                "getZ" -> 30
                "getLocation" -> Location(world, 11.0, 20.0, 30.0)
                "getType" -> Material.STONE
                else -> null
            }
        } as Block

        val event = BlockBreakEvent(otherBlock, player)
        val listener = BlockBreak()

        // Reset flags
        damageCalled = false

        // Act
        listener.onBreak(event)

        // Assert
        assertFalse(damageCalled, "Player should not be damaged for different block")
        assertTrue(event.isDropItems, "Block should drop items (default true)")
    }
}
