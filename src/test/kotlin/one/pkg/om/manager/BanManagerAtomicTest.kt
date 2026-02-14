package one.pkg.om.manager

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class BanManagerAtomicTest {

    private lateinit var tempFile: File

    @BeforeEach
    fun setup() {
        tempFile = File.createTempFile("locked_morphs_atomic_test", ".txt")
        BanManager.customFile = tempFile
        BanManager.load()
    }

    @Test
    fun `test save writes correctly and cleans up temp file`() {
        BanManager.lock("entity", "creeper")

        // Verify file content
        val content = tempFile.readLines()
        assertTrue(content.contains("entity:creeper"), "File should contain locked entity")

        // Verify temp file does not exist (assuming implementation uses .tmp)
        val atomicTempFile = File(tempFile.absolutePath + ".tmp")
        assertFalse(atomicTempFile.exists(), "Temporary atomic file should be cleaned up")
    }
}
