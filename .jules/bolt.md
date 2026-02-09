## 2025-10-26 - [Minecraft Plugin Entity Teleport Optimization]
**Learning:** In Minecraft Bukkit plugins, `Player.location` returns a new `Location` object every time. Assigning this to a field for comparison allows avoiding unnecessary async teleports when the entity hasn't moved, saving significant processing on idle ticks.
**Action:** When implementing entity-following logic, always cache the last target location and compare before issuing a teleport command, as teleport packets are expensive even if async.
