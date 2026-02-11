package one.pkg.om

import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.system.measureNanoTime

class BenchmarkTest {

    data class TestBlockPos(val world: String, val x: Int, val y: Int, val z: Int)
    data class TestPlayer(val name: String)
    data class TestMorphData(val current: Any?)
    data class TestMorphBlock(val pos: TestBlockPos) {
        fun isAt(loc: TestBlockPos): Boolean {
            return pos == loc
        }
    }

    @Test
    fun benchmarkLookup() {
        val playerCount = 2000
        val players = mutableListOf<TestPlayer>()
        val playerMorphs = LinkedHashMap<TestPlayer, TestMorphData>()
        val blockMorphs = ConcurrentHashMap<TestBlockPos, TestPlayer>()

        val worldName = "world"
        val positions = mutableListOf<TestBlockPos>()

        // Setup
        for (i in 0 until playerCount) {
            val p = TestPlayer("Player$i")
            players.add(p)
            // 50% chance to be a morph block
            if (i % 2 == 0) {
                val pos = TestBlockPos(worldName, Random.nextInt(1000), Random.nextInt(256), Random.nextInt(1000))
                positions.add(pos)
                val morph = TestMorphBlock(pos)
                playerMorphs[p] = TestMorphData(morph)
                blockMorphs[pos] = p
            } else {
                playerMorphs[p] = TestMorphData(null)
            }
        }

        // Test cases (lookup existing positions)
        val lookupCount = 100000

        // Warmup
        for (i in 0 until 1000) {
            val loc = positions[i % positions.size]
            for ((p, data) in playerMorphs) {
                val current = data.current
                if (current is TestMorphBlock && current.isAt(loc)) break
            }
            blockMorphs[loc]
        }

        // Baseline: Loop
        val baselineTime = measureNanoTime {
            for (i in 0 until lookupCount) {
                // Pick a random position to look up to avoid JIT optimization on a single value
                val loc = positions[i % positions.size]
                var found: TestPlayer? = null
                for ((p, data) in playerMorphs) {
                    val current = data.current
                    if (current is TestMorphBlock) {
                        if (current.isAt(loc)) {
                            found = p
                            break // In real code it returns immediately
                        }
                    }
                }
            }
        }

        // Optimized: Map
        val optimizedTime = measureNanoTime {
            for (i in 0 until lookupCount) {
                val loc = positions[i % positions.size]
                val found = blockMorphs[loc]
            }
        }

        println("Benchmark Results (for $lookupCount lookups among $playerCount players):")
        println("Baseline (Loop): ${baselineTime / 1_000_000.0} ms")
        println("Optimized (Map): ${optimizedTime / 1_000_000.0} ms")

        val speedup = baselineTime.toDouble() / optimizedTime.toDouble()
        println("Speedup: ${String.format("%.2f", speedup)}x")
    }
}
