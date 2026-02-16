package one.pkg.om.manager

import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.utils.testPluginInstance
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty

class RequestManagerTest {

    private lateinit var mockServer: Server
    private lateinit var mockPlugin: Plugin
    private lateinit var mockScheduler: GlobalRegionScheduler
    private lateinit var tempDir: File
    private lateinit var sender: Player
    private lateinit var receiver: Player

    @BeforeEach
    fun setup() {
        tempDir = File.createTempFile("om_test_save", "")
        tempDir.delete()
        tempDir.mkdirs()
        SaveMorphData.customSaveDir = tempDir

        // Mock Plugin
        mockPlugin = Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, _, _ -> null } as Plugin
        testPluginInstance = mockPlugin

        // Mock Scheduler
        mockScheduler = Proxy.newProxyInstance(
            GlobalRegionScheduler::class.java.classLoader,
            arrayOf(GlobalRegionScheduler::class.java)
        ) { _, method, args ->
            if (method.name == "runDelayed") {
                // Return dummy task
                return@newProxyInstance Proxy.newProxyInstance(
                    ScheduledTask::class.java.classLoader,
                    arrayOf(ScheduledTask::class.java)
                ) { _, _, _ -> null }
            }
            null
        } as GlobalRegionScheduler

        val senderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val receiverUuid = UUID.fromString("00000000-0000-0000-0000-000000000002")
        sender = createMockPlayer("Sender", senderUuid)
        receiver = createMockPlayer("Receiver", receiverUuid, "the_skin_value", "the_skin_signature")

        // Mock Server
        mockServer = Proxy.newProxyInstance(
            Server::class.java.classLoader,
            arrayOf(Server::class.java)
        ) { _, method, args ->
            // println("Server method called: ${method.name}")
            when (method.name) {
                "getGlobalRegionScheduler" -> mockScheduler
                "getPlayer" -> {
                    if (args == null || args.isEmpty()) return@newProxyInstance null
                    val arg = args[0]
                    if (arg is String) {
                        val name = arg
                        if (name == "Sender") return@newProxyInstance sender
                        if (name == "Receiver") return@newProxyInstance receiver
                    }
                    null
                }
                "getLogger" -> java.util.logging.Logger.getLogger("Test")
                else -> null
            }
        } as Server

        if (Bukkit.getServer() == null) {
            val serverField = Bukkit::class.java.getDeclaredField("server")
            serverField.isAccessible = true
            serverField.set(null, mockServer)
        }
    }

    @AfterEach
    fun tearDown() {
        // Reset Bukkit server to avoid polluting other tests
        val serverField = Bukkit::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(null, null)

        // Reset plugin instance
        testPluginInstance = null

        // Clean up temp dir
        tempDir.deleteRecursively()
    }

    private fun createMockPlayer(name: String, uuid: UUID, skinVal: String? = null, skinSig: String? = null): Player {
        val mockProfile = Proxy.newProxyInstance(
            PlayerProfile::class.java.classLoader,
            arrayOf(PlayerProfile::class.java)
        ) { _, method, args ->
            if (method.name == "getProperties") {
                val props = LinkedHashSet<ProfileProperty>()
                if (skinVal != null) {
                    props.add(ProfileProperty("textures", skinVal, skinSig))
                }
                return@newProxyInstance props
            }
             if (method.name == "getId") return@newProxyInstance uuid
             if (method.name == "getName") return@newProxyInstance name
            null
        } as PlayerProfile

        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getName" -> name
                "getUniqueId" -> uuid
                "getPlayerProfile" -> mockProfile
                "sendMessage" -> null // Ignore
                "isOnline" -> true
                "hashCode" -> uuid.hashCode()
                "equals" -> {
                    val other = args[0]
                    // Simple reference check since we reuse instances
                    return@newProxyInstance other === proxy
                }
                else -> null
            }
        } as Player
    }

    @Test
    fun `test acceptRequest preserves skin signature`() {
        // Populate OManager
        val senderData = SaveMorphData.empty(sender)
        OManager.playerMorph[sender] = OnlineMorphData(null, senderData)

        // Send request
        RequestManager.sendRequest(sender, receiver)

        // Accept request
        RequestManager.acceptRequest(receiver, "Sender")

        // Verify data
        val savedPlayer = senderData.players.find { it.name == "Receiver" }
        if (savedPlayer == null) {
            fail<Unit>("Receiver was not added to sender's morph list")
        }

        // Assert failure first (Current behavior)
        // assertEquals("the_skin_value", savedPlayer!!.skin, "Bug reproduction: Skin signature is missing")

        // Assert correct behavior (Goal)
        assertEquals("the_skin_value;the_skin_signature", savedPlayer!!.skin, "Skin signature should be preserved")
    }
}
