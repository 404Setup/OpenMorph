package one.pkg.om.commands

import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime

class MorphCommandBenchmarkTest {

    // Simulating Bukkit enums for benchmark purposes
    enum class MockMaterial {
        STONE, DIRT, GRASS, WATER, LAVA, BEDROCK, SAND, GRAVEL, OAK_LOG, OAK_PLANKS; // and many more
        val isBlock: Boolean get() = this != WATER && this != LAVA
    }

    enum class MockEntityType {
        ZOMBIE, SKELETON, CREEPER, SPIDER, COW, SHEEP, PIG, HORSE, PLAYER, VILLAGER; // and many more
    }

    @Test
    fun benchmarkTabCompletion() {
        // Expand the mock lists to be larger to simulate actual sizes (e.g. ~1000 items)
        val allMaterials = (0..1000).map { "MATERIAL_$it" }
        val allEntities = (0..100).map { "ENTITY_$it" }

        val isBlockList = allMaterials.map { it to (it.hashCode() % 2 == 0) }

        val iterations = 100000

        // Baseline: map every time
        val baselineTime = measureNanoTime {
            for (i in 0 until iterations) {
                val list = isBlockList.filter { it.second }.map { it.first }
                val entities = allEntities.map { it }
            }
        }

        // Optimized: cache
        val cachedBlocks by lazy { isBlockList.filter { it.second }.map { it.first } }
        val cachedEntities by lazy { allEntities.map { it } }
        val optimizedTime = measureNanoTime {
            for (i in 0 until iterations) {
                val list = cachedBlocks
                val entities = cachedEntities
            }
        }

        println("Benchmark Results (for $iterations iterations):")
        println("Baseline: ${baselineTime / 1_000_000.0} ms")
        println("Optimized: ${optimizedTime / 1_000_000.0} ms")
        val speedup = baselineTime.toDouble() / optimizedTime.toDouble()
        println("Speedup: ${String.format("%.2f", speedup)}x")
    }
}
