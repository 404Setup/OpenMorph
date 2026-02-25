package one.pkg.om.commands.sub

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.ParsedArgument
import com.mojang.brigadier.context.StringRange
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import one.pkg.om.data.OnlineMorphData
import one.pkg.om.data.SaveMorphData
import one.pkg.om.manager.OManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*

class DropCommandTest {

    private lateinit var dropCommand: DropCommand
    private lateinit var targetPlayer: Player
    private lateinit var commandContext: CommandContext<CommandSourceStack>

    @BeforeEach
    fun setUp() {
        dropCommand = DropCommand()
        OManager.playerMorph.clear()

        // Setup Target Player
        val targetUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        targetPlayer = createMockPlayer(targetUuid, "TargetPlayer")

        // Setup OManager data
        val saveMorphData = SaveMorphData(
            player = targetUuid,
            blocks = mutableListOf(),
            entities = mutableListOf("ZOMBIE"),
            players = mutableListOf()
        )
        try {
            val field = SaveMorphData::class.java.getDeclaredField("playerEntity")
            field.isAccessible = true
            field.set(saveMorphData, targetPlayer)
        } catch (e: Exception) {
            // Ignore
        }

        val onlineMorphData = OnlineMorphData(null, saveMorphData)
        OManager.playerMorph[targetPlayer] = onlineMorphData
    }

    @AfterEach
    fun tearDown() {
        OManager.playerMorph.clear()
    }

    @Test
    fun `test execute denies spoofed name (different UUID)`() {
        // Sender has same name but different UUID
        val senderUuid = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val senderPlayer = createMockPlayer(senderUuid, "TargetPlayer") // Spoofed name

        commandContext = createMockCommandContext(senderPlayer, targetPlayer)

        val result = invokeExecute(commandContext)

        // Expect failure (0) because UUIDs mismatch
        assertEquals(0, result, "Command should fail when UUIDs do not match")
    }

    @Test
    fun `test execute allows legitimate owner (matching UUID)`() {
        // Sender is target (same UUID)
        val senderPlayer = targetPlayer // Same object, same UUID

        commandContext = createMockCommandContext(senderPlayer, targetPlayer)

        val result = invokeExecute(commandContext)

        // Expect success (1)
        assertEquals(1, result, "Command should succeed when UUIDs match")
    }

    @Test
    fun `test execute allows admin regardless of UUID`() {
        // Sender is admin, different UUID
        val senderUuid = UUID.fromString("00000000-0000-0000-0000-000000000003")
        val senderPlayer = createMockPlayer(senderUuid, "AdminPlayer", isAdmin = true)

        commandContext = createMockCommandContext(senderPlayer, targetPlayer)

        val result = invokeExecute(commandContext)

        // Expect success (1)
        assertEquals(1, result, "Command should succeed for admin")
    }

    @Test
    fun `test execute falls back to name check for non-player sender`() {
        // Sender is console (not Player)
        val sender = createMockConsole("TargetPlayer", isAdmin = false) // Console usually op but forcing not op for test

        commandContext = createMockCommandContext(sender, targetPlayer)

        val result = invokeExecute(commandContext)

        // Expect success (1) because names match and sender is not player
        assertEquals(1, result, "Command should succeed for non-player with matching name")

        // Test mismatch name
        val senderMismatch = createMockConsole("Console", isAdmin = false)
        commandContext = createMockCommandContext(senderMismatch, targetPlayer)
        val resultMismatch = invokeExecute(commandContext)
        assertEquals(0, resultMismatch, "Command should fail for non-player with mismatching name")
    }

    private fun invokeExecute(ctx: CommandContext<CommandSourceStack>): Int {
        val method = DropCommand::class.java.getDeclaredMethod("execute", CommandContext::class.java)
        method.isAccessible = true
        return method.invoke(dropCommand, ctx) as Int
    }

    private fun createMockPlayer(uuid: UUID, name: String, isAdmin: Boolean = false): Player {
        val handler = MockHandler()
        handler.on("getUniqueId") { uuid }
        handler.on("getName") { name }
        handler.on("hashCode") { uuid.hashCode() }
        handler.on("equals") { args ->
            val other = args?.get(0)
            if (other is Player) other.uniqueId == uuid else false
        }
        // op() extension checks "404morph.admin" permission or isOp
        handler.on("hasPermission") { args ->
            val perm = args?.get(0) as String
            if (perm == "404morph.admin") isAdmin else false
        }
        handler.on("isOp") { isAdmin }
        handler.on("sendMessage") { null }
        handler.on("spigot") {
             val spigotHandler = MockHandler()
             spigotHandler.on("sendMessage") { null }
             Proxy.newProxyInstance(
                Player.Spigot::class.java.classLoader,
                arrayOf(Player.Spigot::class.java),
                spigotHandler
             )
        }

        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
            handler
        ) as Player
    }

    private fun createMockConsole(name: String, isAdmin: Boolean): CommandSender {
        val handler = MockHandler()
        handler.on("getName") { name }
        handler.on("hasPermission") { args ->
            val perm = args?.get(0) as String
            if (perm == "404morph.admin") isAdmin else false
        }
        handler.on("isOp") { isAdmin }
        handler.on("sendMessage") { null }
        handler.on("spigot") {
             val spigotHandler = MockHandler()
             spigotHandler.on("sendMessage") { null }
             Proxy.newProxyInstance(
                CommandSender.Spigot::class.java.classLoader,
                arrayOf(CommandSender.Spigot::class.java),
                spigotHandler
             )
        }

        return Proxy.newProxyInstance(
            CommandSender::class.java.classLoader,
            arrayOf(CommandSender::class.java),
            handler
        ) as CommandSender
    }

    private fun createMockCommandContext(sender: CommandSender, target: Player): CommandContext<CommandSourceStack> {
        // Mock Source
        val sourceHandler = MockHandler()
        sourceHandler.on("getSender") { sender }

        val sourceStack = Proxy.newProxyInstance(
            CommandSourceStack::class.java.classLoader,
            arrayOf(CommandSourceStack::class.java),
            sourceHandler
        ) as CommandSourceStack

        // Create Mock Resolver
        val resolverHandler = MockHandler()
        resolverHandler.on("resolve") { listOf(target) }
        val resolver = Proxy.newProxyInstance(
             PlayerSelectorArgumentResolver::class.java.classLoader,
             arrayOf(PlayerSelectorArgumentResolver::class.java),
             resolverHandler
        )

        val arguments = HashMap<String, ParsedArgument<CommandSourceStack, *>>()
        arguments["player"] = ParsedArgument(0, 0, resolver)
        arguments["type"] = ParsedArgument(0, 0, "entity")
        arguments["id"] = ParsedArgument(0, 0, "zombie")

        return CommandContext(
            sourceStack,
            "",
            arguments,
            null,
            null,
            emptyList(),
            StringRange.at(0),
            null,
            null,
            false
        )
    }

    class MockHandler : InvocationHandler {
        val methods = mutableMapOf<String, (Array<Any?>?) -> Any?>()

        override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
            if (methods.containsKey(method.name)) {
                return methods[method.name]?.invoke(args)
            }
            if (method.name == "toString") return "MockObject"
            if (method.name == "hashCode") return System.identityHashCode(proxy)
            if (method.name == "equals") return proxy === args?.get(0)

            // Return defaults for primitives to avoid NPE
            if (method.returnType == Boolean::class.java || method.returnType == Boolean::class.javaPrimitiveType) return false
            if (method.returnType == Int::class.java || method.returnType == Int::class.javaPrimitiveType) return 0
            if (method.returnType == Long::class.java || method.returnType == Long::class.javaPrimitiveType) return 0L
            if (method.returnType == Double::class.java || method.returnType == Double::class.javaPrimitiveType) return 0.0
            if (method.returnType == String::class.java) return ""

            return null
        }

        fun on(methodName: String, handler: (Array<Any?>?) -> Any?) {
            methods[methodName] = handler
        }
    }
}
