## 2025-02-18 - Logic Bomb in Entity Behavior
**Vulnerability:** Found a hardcoded "instant kill" logic in `MorphGhast` that set victim health to 0 when hit by a fireball. This allowed players morphed as Ghasts to one-shot any entity, including players and bosses, bypassing all protections.
**Learning:** Developers sometimes leave debug or "god mode" features in production code, or implement overpowered mechanics for fun without considering security implications in a multiplayer context.
**Prevention:** Review all entity interaction listeners for hardcoded damage values or health manipulation. Ensure damage is calculated using standard API methods (`entity.damage()`) rather than setting health directly.
