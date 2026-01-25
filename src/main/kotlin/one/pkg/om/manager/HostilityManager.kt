/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.manager

import one.pkg.om.OmMain
import org.bukkit.entity.EntityType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

object HostilityManager {
    private val hostilityRules: Map<EntityType, Set<EntityType>> by lazy {
        loadHostilityRules()
    }

    private fun loadHostilityRules(): Map<EntityType, Set<EntityType>> {
        val rules = mutableMapOf<EntityType, Set<EntityType>>()
        val resourcePath = "/data/hostility"

        val resourceUrl = javaClass.classLoader.getResource(resourcePath)
        if (resourceUrl != null) {
            val uri = resourceUrl.toURI()
            val path = if (uri.scheme == "jar") {
                val fs = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
                fs.getPath(resourcePath)
            } else {
                Paths.get(uri)
            }

            Files.walk(path, 1).forEach { filePath ->
                val fileName = filePath.fileName.toString()
                if (fileName.endsWith(".txt")) {
                    val victimName = fileName.removeSuffix(".txt").lowercase()
                    try {
                        val victimType = EntityType.fromName(victimName)
                        if (victimType == null) {
                            OmMain.getInstance().logger.warning {
                                "Invalid entity type in hostility file: $victimName"
                            }
                        } else {
                            val aggressors = mutableSetOf<EntityType>()

                            javaClass.classLoader.getResourceAsStream("$resourcePath/$fileName")?.use { stream ->
                                BufferedReader(InputStreamReader(stream)).use { reader ->
                                    reader.lineSequence().forEach { line ->
                                        val trimmed = line.trim()
                                        if (trimmed.isNotEmpty()) {
                                            try {
                                                val e = EntityType.fromName(trimmed)
                                                if (e == null) {
                                                    OmMain.getInstance().logger.warning {
                                                        "Invalid entity type in hostility file: $trimmed"
                                                    }
                                                } else aggressors.add(e)
                                            } catch (_: IllegalArgumentException) {
                                            }
                                        }
                                    }
                                }
                            }

                            if (aggressors.isNotEmpty()) {
                                rules[victimType] = aggressors
                            }
                        }

                    } catch (_: IllegalArgumentException) {
                    }
                }
            }
        }

        return rules
    }

    fun shouldAttack(aggressor: EntityType, victim: EntityType): Boolean {
        if (aggressor == EntityType.WARDEN) return true

        return hostilityRules[victim]?.contains(aggressor) == true
    }
}
