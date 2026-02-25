package one.pkg.om.manager

import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.utils.testPluginInstance
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import java.util.function.Consumer

class RequestManagerLeakTest {

    private lateinit var mockServer: Server
    private lateinit var mockPlugin: Plugin
    private lateinit var mockScheduler: GlobalRegionScheduler
    private lateinit var tempDir: File
    private lateinit var sender: Player
    private lateinit var receiver: Player
    private lateinit var scheduledTasks: MutableList<Consumer<ScheduledTask>>

    @BeforeEach
    fun setup() {
        tempDir = File.createTempFile("om_test_leak", "")
        tempDir.delete()
        tempDir.mkdirs()
        SaveMorphData.customSaveDir = tempDir

        scheduledTasks = mutableListOf()

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
                // Store task for manual execution
                if (args.size >= 3 && args[1] is Consumer<*>) {
                    @Suppress("UNCHECKED_CAST")
                    scheduledTasks.add(args[1] as Consumer<ScheduledTask>)
                }
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
        receiver = createMockPlayer("Receiver", receiverUuid)

        // Mock Server
        mockServer = Proxy.newProxyInstance(
            Server::class.java.classLoader,
            arrayOf(Server::class.java)
        ) { _, method, args ->
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
                    if (arg is UUID) {
                        val uuid = arg
                        if (uuid == senderUuid) return@newProxyInstance sender
                        if (uuid == receiverUuid) return@newProxyInstance receiver
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

        // Clear RequestManager state
        val requestsField = RequestManager::class.java.getDeclaredField("requests")
        requestsField.isAccessible = true
        (requestsField.get(RequestManager) as MutableMap<*, *>).clear()

        val cooldownsField = RequestManager::class.java.getDeclaredField("cooldowns")
        cooldownsField.isAccessible = true
        (cooldownsField.get(RequestManager) as MutableMap<*, *>).clear()
    }

    @AfterEach
    fun tearDown() {
        val serverField = Bukkit::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(null, null)
        testPluginInstance = null
        tempDir.deleteRecursively()
    }

    private fun createMockPlayer(name: String, uuid: UUID): Player {
        val mockProfile = Proxy.newProxyInstance(
            PlayerProfile::class.java.classLoader,
            arrayOf(PlayerProfile::class.java)
        ) { _, method, args ->
            if (method.name == "getProperties") return@newProxyInstance LinkedHashSet<ProfileProperty>()
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
                "sendMessage" -> null
                "isOnline" -> true
                "hashCode" -> uuid.hashCode()
                "equals" -> {
                    val other = args[0]
                    return@newProxyInstance other === proxy
                }
                else -> null
            }
        } as Player
    }

    @Test
    fun `test request map leak on deny`() {
        // Send request
        RequestManager.sendRequest(sender, receiver)

        // Verify map has entry for receiver
        val requestsMap = getRequestsMap()
        assertTrue(requestsMap.containsKey(receiver.uniqueId), "Map should contain receiver UUID")

        // Deny request
        RequestManager.denyRequest(receiver, "Sender")

        // Check if map still contains receiver UUID
        // Current behavior: It does. Fixed behavior: It should NOT.

        // This assertion will fail BEFORE the fix, demonstrating the leak.
        assertNull(requestsMap[receiver.uniqueId], "Map should NOT contain receiver UUID after deny (Memory Leak Fix)")
    }

    @Test
    fun `test request map leak on accept`() {
        // Populate OManager for sender
        val senderData = SaveMorphData.empty(sender)
        OManager.playerMorph[sender] = OnlineMorphData(null, senderData)

        RequestManager.sendRequest(sender, receiver)
        val requestsMap = getRequestsMap()
        assertTrue(requestsMap.containsKey(receiver.uniqueId))

        RequestManager.acceptRequest(receiver, "Sender")

        assertNull(requestsMap[receiver.uniqueId], "Map should NOT contain receiver UUID after accept")
    }

    @Test
    fun `test request map leak on timeout`() {
        RequestManager.sendRequest(sender, receiver)
        val requestsMap = getRequestsMap()
        assertTrue(requestsMap.containsKey(receiver.uniqueId))

        // Execute scheduled tasks (timeout)
        val mockTask = Proxy.newProxyInstance(
            ScheduledTask::class.java.classLoader,
            arrayOf(ScheduledTask::class.java)
        ) { _, _, _ -> null } as ScheduledTask
        scheduledTasks.forEach { it.accept(mockTask) }

        assertNull(requestsMap[receiver.uniqueId], "Map should NOT contain receiver UUID after timeout")
    }

    private fun getRequestsMap(): Map<UUID, ConcurrentHashMap<UUID, Long>> {
        val field = RequestManager::class.java.getDeclaredField("requests")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(RequestManager) as Map<UUID, ConcurrentHashMap<UUID, Long>>
    }
}
