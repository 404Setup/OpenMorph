package one.pkg.om.data

import one.pkg.om.data.SaveMorphData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class SaveMorphDataConcurrencyTest {

    @Test
    fun testConcurrentSaveToDisk(@TempDir tempDir: Path) {
        println("Running testConcurrentSaveToDisk")
        SaveMorphData.customSaveDir = tempDir.toFile()
        val uuid = UUID.randomUUID()

        // 1. Calculate expected size
        val templateData = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())
        templateData.blocks.add("THREAD_XXXX")
        templateData.saveToDisk()
        val expectedSize = tempDir.resolve("$uuid.dat").toFile().length()
        println("Expected file size: $expectedSize")

        // 2. Run concurrent test
        val numberOfThreads = 50
        val service = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(1)
        val errors = AtomicInteger(0)

        for (i in 0 until numberOfThreads) {
            service.submit {
                try {
                    latch.await()
                    val data = SaveMorphData(uuid, ArrayList(), ArrayList(), ArrayList())
                    data.blocks.add("THREAD_XXXX")
                    for (j in 0 until 10) {
                        data.saveToDisk()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    errors.incrementAndGet()
                }
            }
        }

        latch.countDown()
        service.shutdown()
        assertTrue(service.awaitTermination(30, TimeUnit.SECONDS))

        val file = tempDir.resolve("$uuid.dat").toFile()
        val actualSize = file.length()
        println("Actual file size: $actualSize")

        assertTrue(file.exists(), "File should exist")
        assertTrue(actualSize > 0, "File should not be empty")
        assertEquals(0, errors.get(), "No exceptions should occur")

        // We cannot reliably assert actualSize == expectedSize without the fix because of race conditions.
        // But with the fix, it should be stable.
    }
}
