package one.pkg.om.listener

import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.entities.MorphEntities
import one.pkg.om.manager.OManager
import one.pkg.om.utils.testPluginInstance
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*

class PlayerWorldChangeTest {

    private lateinit var listener: PlayerWorldChange
    private lateinit var mockPlugin: Plugin

    class MockPlayer(val name: String, var world: World) : InvocationHandler {
        val uuid = UUID.randomUUID()
        val hiddenEntities = mutableListOf<Player>()

        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
            return when (method.name) {
                "getName" -> name
                "getUniqueId" -> uuid
                "getWorld" -> world
                "hideEntity" -> {
                    hiddenEntities.add(args!![1] as Player)
                    null
                }
                "hashCode" -> uuid.hashCode()
                "equals" -> {
                    if (args == null || args[0] !is Player) return false
                    val other = args[0] as Player
                    other.uniqueId == uuid
                }
                "toString" -> "Player($name)"
                else -> null
            }
        }

        fun create(): Player {
             return Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java), this) as Player
        }

        fun verifyHidden(target: Player) {
            val targetUuid = target.uniqueId
            val found = hiddenEntities.any { it.uniqueId == targetUuid }
            if (!found) {
                throw AssertionError("Player $name should have hidden entity ${target.name}")
            }
        }

        fun verifyNotHidden(target: Player) {
             val targetUuid = target.uniqueId
             val found = hiddenEntities.any { it.uniqueId == targetUuid }
             if (found) {
                 throw AssertionError("Player $name should NOT have hidden entity ${target.name}")
             }
        }
    }

    class MockWorld(val name: String) : InvocationHandler {
        val players = mutableListOf<Player>()

        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
             return when (method.name) {
                "getName" -> name
                "getPlayers" -> players
                "hashCode" -> name.hashCode()
                "equals" -> {
                    if (args == null || args[0] !is World) return false
                    val other = args[0] as World
                    other.name == name
                }
                "toString" -> "World($name)"
                else -> null
             }
        }

        fun create(): World {
             return Proxy.newProxyInstance(World::class.java.classLoader, arrayOf(World::class.java), this) as World
        }
    }

    @BeforeEach
    fun setUp() {
        mockPlugin = Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, _, _ -> null } as Plugin

        testPluginInstance = mockPlugin
        listener = PlayerWorldChange()
    }

    @AfterEach
    fun tearDown() {
        OManager.playerMorph.clear()
        testPluginInstance = null
    }

    @Test
    fun `test onWorldChange hides existing morphs from entering player`() {
        val worldHandler = MockWorld("WorldA")
        val world = worldHandler.create()

        val enteringHandler = MockPlayer("EnteringPlayer", world)
        val enteringPlayer = enteringHandler.create()

        val event = PlayerChangedWorldEvent(enteringPlayer, world)

        val morphHandler = MockPlayer("MorphedPlayer", world)
        val morphedPlayer = morphHandler.create()

        val dummyMorph = try {
            object : MorphEntities(morphedPlayer) {}
        } catch (e: Exception) {
            throw RuntimeException("Failed to create MorphEntities: ${e.message}", e)
        }
        val uid = try {
             morphedPlayer.uniqueId
        } catch (e: Exception) {
             throw RuntimeException("Failed to get UUID: ${e.message}", e)
        }
        if (uid == null) throw RuntimeException("UUID is null")

        val saveData = try {
             SaveMorphData(uid, ArrayList(), ArrayList(), ArrayList())
        } catch (e: Exception) {
             throw RuntimeException("Failed to create SaveMorphData", e)
        }
        saveData.updatePlayer(morphedPlayer)

        val morphData = try {
             OnlineMorphData(dummyMorph, saveData)
        } catch (e: Exception) {
             throw RuntimeException("Failed to create OnlineMorphData", e)
        }

        try {
             OManager.playerMorph[morphedPlayer] = morphData
        } catch (e: Exception) {
             throw RuntimeException("Failed to put in OManager: key=$morphedPlayer, value=$morphData", e)
        }

        listener.onWorldChange(event)

        enteringHandler.verifyHidden(morphedPlayer)
    }

    @Test
    fun `test onWorldChange hides entering morph from world players`() {
        val worldHandler = MockWorld("WorldA")
        val world = worldHandler.create()

        val enteringHandler = MockPlayer("MorphPlayer", world)
        val enteringPlayer = enteringHandler.create()

        val event = PlayerChangedWorldEvent(enteringPlayer, world)

        val dummyMorph = object : MorphEntities(enteringPlayer) {}
        val saveData = SaveMorphData(enteringPlayer.uniqueId, ArrayList(), ArrayList(), ArrayList())
        saveData.updatePlayer(enteringPlayer)
        val morphData = OnlineMorphData(dummyMorph, saveData)

        OManager.playerMorph[enteringPlayer] = morphData

        val p1Handler = MockPlayer("P1", world)
        val p1 = p1Handler.create()

        val p2Handler = MockPlayer("P2", world)
        val p2 = p2Handler.create()

        worldHandler.players.add(p1)
        worldHandler.players.add(p2)
        worldHandler.players.add(enteringPlayer)

        listener.onWorldChange(event)

        p1Handler.verifyHidden(enteringPlayer)
        p2Handler.verifyHidden(enteringPlayer)
        enteringHandler.verifyNotHidden(enteringPlayer)
    }
}
