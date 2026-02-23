package one.pkg.om.entities

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TickLogicTest {

    @Test
    fun testTickLogicPeriod5() {
        // Simulating the tick logic with period 5
        val period = 5
        var tickCounter = 123 // Random entity ID
        var hostilityChecks = 0
        var syncChecks = 0

        // Hostility divisor = 40 / 5 = 8
        val hostilityDivisor = 40 / period
        // Sync divisor = 5 / 5 = 1
        val syncDivisor = 5 / period

        // Simulate 400 ticks (real time) = 80 executions
        val executions = 80

        for (i in 0 until executions) {
            val currentTick = tickCounter++

            // Hostility check
            if (currentTick % hostilityDivisor == 0) {
                hostilityChecks++
            }

            // Sync check
            if (currentTick % syncDivisor == 0) {
                syncChecks++
            }
        }

        // Expected hostility checks: 400 ticks / 40 ticks = 10 checks.
        // But due to offset (123 % 8 = 3), it might be 9 or 10.
        // 123 % 8 = 3. First hit at currentTick = 128 (exec 5).
        //Execs: 0..79 -> IDs: 123..202
        // Hits at 128, 136, 144, 152, 160, 168, 176, 184, 192, 200.
        // Total 10 hits.
        println("Hostility Checks (P=5): $hostilityChecks")
        assertTrue(hostilityChecks >= 9 && hostilityChecks <= 11, "Hostility checks should be ~10, got $hostilityChecks")

        // Expected sync checks: 400 ticks / 5 ticks = 80 checks (every execution).
        // Since syncDivisor = 1, it checks every execution.
        println("Sync Checks (P=5): $syncChecks")
        assertTrue(syncChecks == executions, "Sync checks should match executions, got $syncChecks")
    }

    @Test
    fun testTickLogicPeriod1() {
        // Simulating the tick logic with period 1
        val period = 1
        var tickCounter = 123 // Random entity ID
        var hostilityChecks = 0
        var syncChecks = 0

        // Hostility divisor = 40 / 1 = 40
        val hostilityDivisor = 40 / period
        // Sync divisor = 5 / 1 = 5
        val syncDivisor = 5 / period

        // Simulate 400 ticks (real time) = 400 executions
        val executions = 400

        for (i in 0 until executions) {
            val currentTick = tickCounter++

            // Hostility check
            if (currentTick % hostilityDivisor == 0) {
                hostilityChecks++
            }

            // Sync check (simulating passiveSkills empty logic)
            // If passiveSkills empty, check if % 5 == 0.
            if (currentTick % syncDivisor == 0) {
                syncChecks++
            }
        }

        // Expected hostility checks: 400 / 40 = 10.
        // Offset 123 % 40 = 3. First hit at 160 (exec 37).
        // Hits at 160, 200, 240, 280, 320, 360, 400, 440, 480, 520.
        // Wait, 123..522.
        // Hits at 160, 200, 240, 280, 320, 360, 400, 440, 480, 520.
        // 10 hits.
        println("Hostility Checks (P=1): $hostilityChecks")
        assertTrue(hostilityChecks >= 9 && hostilityChecks <= 11, "Hostility checks should be ~10, got $hostilityChecks")

        // Expected sync checks: 400 / 5 = 80.
        println("Sync Checks (P=1): $syncChecks")
        assertTrue(syncChecks >= 79 && syncChecks <= 81, "Sync checks should be ~80, got $syncChecks")
    }
}
