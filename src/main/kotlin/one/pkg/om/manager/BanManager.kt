/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.manager

import one.pkg.om.OmMain
import one.pkg.om.utils.div
import java.util.concurrent.ConcurrentHashMap

object BanManager {
    private val file by lazy { OmMain.getInstance().dataFolder / "locked_morphs.txt" }
    private val lockedMorphs = ConcurrentHashMap.newKeySet<String>()

    fun load() {
        if (!file.exists()) return
        try {
            lockedMorphs.clear()
            lockedMorphs.addAll(file.readLines().filter { it.isNotBlank() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            file.writeText(lockedMorphs.joinToString("\n"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLocked(type: String, id: String): Boolean {
        return lockedMorphs.contains("${type.lowercase()}:${id.lowercase()}")
    }

    fun lock(type: String, id: String) {
        lockedMorphs.add("${type.lowercase()}:${id.lowercase()}")
        save()
    }

    fun unlock(type: String, id: String) {
        lockedMorphs.remove("${type.lowercase()}:${id.lowercase()}")
        save()
    }

    fun getLockedIds(type: String): List<String> {
        val prefix = type.lowercase() + ":"
        return lockedMorphs.filter { it.startsWith(prefix) }
            .map { it.substring(prefix.length) }
    }
}
