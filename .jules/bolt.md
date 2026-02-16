## 2025-10-26 - [Minecraft Plugin Entity Teleport Optimization]
**Learning:** In Minecraft Bukkit plugins, `Player.location` returns a new `Location` object every time. Assigning this to a field for comparison allows avoiding unnecessary async teleports when the entity hasn't moved, saving significant processing on idle ticks.
**Action:** When implementing entity-following logic, always cache the last target location and compare before issuing a teleport command, as teleport packets are expensive even if async.

## 2025-10-26 - [PlayerMoveEvent Efficiency]
**Learning:** `PlayerMoveEvent` provides the target location (`to`), but simply calling `player.location` inside the handler ignores this data and creates a new object. Additionally, running location sync in `tick()` is redundant if `onMove` handles it reliably (using `MONITOR` priority).
**Action:** When handling movement, always use `event.to`. Use `EventPriority.MONITOR` to ensure you're acting on the final, successful move. Remove redundant polling in `tick()` loops.

## 2025-10-27 - [Bukkit Entity Filtering]
**Learning:** `player.getNearbyEntities(x, y, z)` iterates over all entities in the chunk area, creating Java wrappers for each (including items, projectiles, etc.), which can be slow in dense areas. More importantly, iterating with a generic `count < limit` over the *entire* collection means the limit can be exhausted by non-target entities, causing logic failures (e.g., ignoring mobs in favor of dropped items).
**Action:** Use `world.getNearbyEntities(BoundingBox, Predicate)` to filter entities engine-side. This avoids wrapper overhead for irrelevant entities and ensures iteration limits apply only to the desired target type.

## 2024-05-21 - [Optimizing getNearbyEntities with limit]
**Learning:** Limiting `getNearbyEntities` collection size via the predicate significantly reduces memory allocation and iteration overhead when many entities are present.
**Action:** Use a counter in the predicate to stop collecting entities once a limit is reached, instead of collecting all and filtering later.

## 2026-02-16 - [Kotlin Lambda Allocation in Hot Loops]
**Learning:** In Kotlin, defining a lambda that captures local variables inside a frequently executed method (like `tick()`) forces a new object allocation on every execution. Even if executed every few ticks, this adds up across many entities. Additionally, iterating over an empty `MutableList` using `forEach` allocates an iterator.
**Action:** Extract logic into private methods instead of using local lambdas for tasks. Guard `forEach` loops on potentially empty lists with `isNotEmpty()` to avoid iterator allocation.
