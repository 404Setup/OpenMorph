package one.pkg.om.entities

import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy
import java.util.UUID

class HostilityPredicateTest {

    @Test
    fun testPredicateLimitAndReset() {
        // Mock Player
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { _, method, _ ->
            if (method.name == "getEntityId") return@newProxyInstance 123
            if (method.name == "getUniqueId") return@newProxyInstance UUID.randomUUID()
            if (method.name == "toString") return@newProxyInstance "MockPlayer"
            // MorphEntity might access other methods in init, but currently only getEntityId
            // If it accesses attributes, we might need to mock getAttribute
            if (method.name == "getAttribute") return@newProxyInstance null
            null
        } as Player

        // Create MorphEntity
        // This might fail if MorphEntity constructor does more than we think.
        // It calls super(player) -> MorphEntities(player).
        // MorphEntity init:
        // tickCounter = player.entityId
        // hasKnockback = true
        // ...
        val morphEntity = MorphEntity(player, EntityType.ZOMBIE)

        // Access hostilityPredicate
        val predicate = morphEntity.hostilityPredicate

        // Mock Aggressor Mob (WARDEN)
        var targetSetCount = 0
        val warden = Proxy.newProxyInstance(
            Mob::class.java.classLoader,
            arrayOf(Mob::class.java)
        ) { _, method, _ ->
            if (method.name == "getType") return@newProxyInstance EntityType.WARDEN
            if (method.name == "getTarget") return@newProxyInstance null
            if (method.name == "setTarget") {
                targetSetCount++
                return@newProxyInstance null
            }
            if (method.name == "toString") return@newProxyInstance "MockWarden"
            // HostilityManager checks type
            null
        } as Mob

        // Test Limit
        for (i in 1..50) {
            // Predicate now returns false (side-effect optimization) but performs the action
            assertFalse(predicate.test(warden), "Predicate should return false (side-effect optimization) for count $i")
            assertEquals(i, targetSetCount, "Target should have been set $i times")
        }

        // 51st time should return false and NOT set target
        assertFalse(predicate.test(warden), "Predicate should return false after 50 matches")
        assertEquals(50, targetSetCount, "Target should not have been set for 51st call")

        // Reset
        predicate.reset()

        // Should work again
        assertFalse(predicate.test(warden), "Predicate should return false (side-effect) after reset")
        assertEquals(51, targetSetCount, "Target should have been set again after reset")
    }

    @Test
    fun testPredicateFiltersNonMobs() {
        val player = Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { _, method, _ ->
             if (method.name == "getEntityId") return@newProxyInstance 123
             if (method.name == "getUniqueId") return@newProxyInstance UUID.randomUUID()
             null
        } as Player
        val morphEntity = MorphEntity(player, EntityType.ZOMBIE)
        val predicate = morphEntity.hostilityPredicate

        val nonMob = Proxy.newProxyInstance(
            Entity::class.java.classLoader,
            arrayOf(Entity::class.java)
        ) { _, method, _ ->
            if (method.name == "getType") return@newProxyInstance EntityType.ARROW
             null
        } as Entity

        assertFalse(predicate.test(nonMob), "Should return false for non-Mob")
    }
}
