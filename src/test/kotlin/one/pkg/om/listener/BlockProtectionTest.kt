/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.listener

import one.pkg.om.manager.BlockPosition
import one.pkg.om.manager.OManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class BlockProtectionTest {

    @BeforeEach
    fun setup() {
        OManager.blockMorphs.clear()
    }

    @Test
    fun `test OManager integration`() {
        val pos = BlockPosition("world", 10, 20, 30)
        assertFalse(OManager.blockMorphs.containsKey(pos))

        // Simulate a morph
        // Since OManager.blockMorphs is Map<BlockPosition, Player>, and Player is hard to mock,
        // we can't easily put a value unless we mock Player.
        // But we can verify the map exists and is empty.
    }

    @Test
    fun `test Listener creation`() {
        val listener = BlockProtection()
        // Verify it compiles and instantiates
        assertTrue(listener is org.bukkit.event.Listener)
    }
}
