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
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap

object BanManager {
    internal var customFile: File? = null

    private val defaultFile by lazy { OmMain.getInstance().dataFolder / "locked_morphs.txt" }

    private val file: File
        get() = customFile ?: defaultFile

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
            // Security: Use atomic write to prevent data corruption/loss on crash
            val tempFile = File(file.absolutePath + ".tmp")
            tempFile.writeText(lockedMorphs.joinToString("\n"))

            Files.move(
                tempFile.toPath(),
                file.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLocked(type: String, id: String): Boolean {
        return lockedMorphs.contains("${type.lowercase()}:${id.lowercase()}")
    }

    fun lock(type: String, id: String) {
        validateInput(type, id)
        lockedMorphs.add("${type.lowercase()}:${id.lowercase()}")
        save()
    }

    fun unlock(type: String, id: String) {
        validateInput(type, id)
        lockedMorphs.remove("${type.lowercase()}:${id.lowercase()}")
        save()
    }

    private fun validateInput(type: String, id: String) {
        if (type.contains("\n") || id.contains("\n") || type.contains("\r") || id.contains("\r")) {
            throw IllegalArgumentException("Type and ID cannot contain newline characters")
        }
    }

    fun getLockedIds(type: String): List<String> {
        val prefix = type.lowercase() + ":"
        return lockedMorphs.filter { it.startsWith(prefix) }
            .map { it.substring(prefix.length) }
    }
}
