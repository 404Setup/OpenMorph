## 2025-10-26 - [Minecraft Plugin Entity Teleport Optimization]
**Learning:** In Minecraft Bukkit plugins, `Player.location` returns a new `Location` object every time. Assigning this to a field for comparison allows avoiding unnecessary async teleports when the entity hasn't moved, saving significant processing on idle ticks.
**Action:** When implementing entity-following logic, always cache the last target location and compare before issuing a teleport command, as teleport packets are expensive even if async.

## 2025-10-26 - [PlayerMoveEvent Efficiency]
**Learning:** `PlayerMoveEvent` provides the target location (`to`), but simply calling `player.location` inside the handler ignores this data and creates a new object. Additionally, running location sync in `tick()` is redundant if `onMove` handles it reliably (using `MONITOR` priority).
**Action:** When handling movement, always use `event.to`. Use `EventPriority.MONITOR` to ensure you're acting on the final, successful move. Remove redundant polling in `tick()` loops.
