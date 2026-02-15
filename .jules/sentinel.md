## 2026-06-25 - Restricted Blocks Enhancement
**Vulnerability:** Players could morph into blocks like TNT, Lava, or Water and "solidify" them into the world, bypassing protection plugins because `MorphBlock` uses `block.setType()` instead of firing `BlockPlaceEvent`.
**Learning:** Plugins that directly modify the world state without firing Bukkit events (like `BlockPlaceEvent`) bypass all other plugins that listen to these events for protection (e.g., WorldGuard, GriefPrevention). Manual restrictions are required in these cases.
**Prevention:** Always fire relevant Bukkit events when modifying the world in response to player actions, or explicitly check for permissions/regions if events cannot be fired. In this case, we expanded the `RestrictedBlocks` blacklist to cover dangerous materials.

## 2026-06-26 - Unbounded Player Data Collections
**Vulnerability:** Player morph lists (blocks, entities, players) stored in `SaveMorphData` were unbounded `MutableList`s. Malicious actors or unintentional usage could cause these lists to grow indefinitely, leading to memory exhaustion (OOM) or disk space denial of service (DoS) via large JSON/Avro files.
**Learning:** Data classes with mutable collections often lack inherent size constraints. Relying on higher-level logic to prevent abuse is risky if multiple entry points exist (e.g., `AppendCommand` vs `RequestManager`).
**Prevention:** Enforce hard limits (e.g., `MAX_MORPHS`) directly within the data model's modification methods (`addBlock`, `addEntity`, etc.) and return a success/failure status to the caller.
