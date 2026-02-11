package one.pkg.om.manager

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals

class BanManagerTest {

    private lateinit var tempFile: File

    @BeforeEach
    fun setup() {
        tempFile = File.createTempFile("locked_morphs_test", ".txt")
        BanManager.customFile = tempFile
        // Clear state
        // Since we can't easily clear the private set, we rely on load() from an empty file
        BanManager.load()
    }

    @Test
    fun `test lock normal input`() {
        BanManager.lock("entity", "zombie")

        val content = tempFile.readLines()
        assertTrue(content.contains("entity:zombie"))
        assertTrue(BanManager.isLocked("entity", "zombie"))
    }

    @Test
    fun `test lock with newline injection vulnerability`() {
        // Attempt to inject a new entry via newline
        val type = "entity"
        // If we inject "skeleton" as a new locked type
        val id = "zombie\nentity:skeleton"

        try {
            BanManager.lock(type, id)
        } catch (e: IllegalArgumentException) {
            // If the fix is applied, it might throw an exception, which is good.
            return
        }

        // If not fixed, the file will contain a newline
        val content = tempFile.readLines()

        // If vulnerable, "entity:skeleton" will be a separate line
        assertFalse(content.contains("entity:skeleton"), "Vulnerability exploit successful: injected entry found as a separate line")

        // Also check if it's considered locked
        // We need to reload to simulate plugin restart or just check if it was persisted
        BanManager.load()
        assertFalse(BanManager.isLocked("entity", "skeleton"), "Injected entry is active after reload")
    }
}
