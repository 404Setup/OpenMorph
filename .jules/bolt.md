## 2024-05-23 - Redundant Location Syncing Pattern
**Learning:** The codebase updates entity location both on `PlayerMoveEvent` and every tick in `tick()`. This causes redundant `teleportAsync` calls, spamming packets even when idle.
**Action:** Always check for redundant update loops when optimizing entity synchronization logic. Use cached state to prevent duplicate operations.
