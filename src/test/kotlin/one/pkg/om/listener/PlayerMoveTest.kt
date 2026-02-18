package one.pkg.om.listener

import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.entities.MorphEntities
import one.pkg.om.manager.OManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy
import java.util.UUID

class PlayerMoveTest {

    private lateinit var player: Player
    private lateinit var world: World
    private lateinit var morphEntities: TestMorphEntities

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
                "hashCode" -> worldUuid.hashCode()
                "equals" -> args != null && args.isNotEmpty() && args[0] === world
                "toString" -> "MockWorld($worldUuid)"
                else -> null
            }
        } as World

        // Mock MorphEntities
        morphEntities = TestMorphEntities(player)
    }

    @AfterEach
    fun tearDown() {
        OManager.playerMorph.clear()
    }

    @Test
    fun `onPlayerMove should not ignore rotation`() {
        // Arrange
        val from = Location(world, 10.0, 20.0, 30.0, 0f, 0f)
        val to = Location(world, 10.0, 20.0, 30.0, 90f, 0f) // Rotated 90 degrees yaw

        val event = PlayerMoveEvent(player, from, to)

        // Dummy SaveMorphData (minimally populated to avoid nulls/crashes if accessed)
        val saveMorphData = SaveMorphData(
            UUID.randomUUID(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )

        val onlineMorphData = OnlineMorphData(morphEntities, saveMorphData)
        OManager.playerMorph[player] = onlineMorphData

        // Act
        val listener = PlayerMove()
        listener.onPlayerMove(event)

        // Assert
        assertTrue(morphEntities.onMoveCalled, "onMove should be called when player rotates")
    }

    class TestMorphEntities(player: Player) : MorphEntities(player) {
        var onMoveCalled = false
        override fun onMove(event: PlayerMoveEvent) {
            onMoveCalled = true
        }
    }
}
