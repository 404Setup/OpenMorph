package one.pkg.om.data

import one.pkg.om.utils.ClassScanner
import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime

class ScanBenchmarkTest {

    @Test
    fun benchmarkScanClasses() {
        val context = OMData::class.java

        // Warmup
        ClassScanner.scanClasses("one.pkg.om.data", context)

        val iterations = 100

        // Baseline: Scanning every time
        val baselineTime = measureNanoTime {
            for (i in 0 until iterations) {
                val migrators = ClassScanner.scanClasses("one.pkg.om.data", context)
                    .filter { it.isAnnotationPresent(OMData::class.java) && DataMigrator::class.java.isAssignableFrom(it) }
                    .map { it.getDeclaredConstructor().newInstance() as DataMigrator }
                    .sortedByDescending { it.javaClass.getAnnotation(OMData::class.java).version }
            }
        }

        // Optimized: Cached (simulating the fix in SaveMorphData)
        val cachedMigrators by lazy {
             ClassScanner.scanClasses("one.pkg.om.data", context)
                .filter { it.isAnnotationPresent(OMData::class.java) && DataMigrator::class.java.isAssignableFrom(it) }
                .map { it.getDeclaredConstructor().newInstance() as DataMigrator }
                .sortedByDescending { it.javaClass.getAnnotation(OMData::class.java).version }
        }

        // Force initialization
        cachedMigrators.size

        val optimizedTime = measureNanoTime {
            for (i in 0 until iterations) {
                val migrators = cachedMigrators
            }
        }

        println("Benchmark Results (for $iterations iterations):")
        println("Baseline (Scan): ${baselineTime / 1_000_000.0} ms")
        println("Optimized (Cached): ${optimizedTime / 1_000_000.0} ms")

        if (optimizedTime > 0) {
            val speedup = baselineTime.toDouble() / optimizedTime.toDouble()
            println("Speedup: ${String.format("%.2f", speedup)}x")
        } else {
             println("Speedup: Infinite (Optimized took 0ms or < resolution)")
        }
    }
}
