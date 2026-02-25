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
import java.nio.file.FileSystem
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.EnumMap
import java.util.EnumSet

object HostilityManager {
    private val hostilityRules: Map<EntityType, Set<EntityType>> by lazy {
        loadHostilityRules()
    }

    private fun loadHostilityRules(): Map<EntityType, Set<EntityType>> {
        // Optimization: Use EnumMap for O(1) lookups and better memory usage compared to HashMap
        val rules = EnumMap<EntityType, Set<EntityType>>(EntityType::class.java)
        val resourcePath = "/data/hostility"

        val resourceUrl = javaClass.classLoader.getResource(resourcePath)
        if (resourceUrl != null) {
            val uri = resourceUrl.toURI()

            var fs: FileSystem? = null
            val path = try {
                if (uri.scheme == "jar") {
                    try {
                        fs = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
                        fs.getPath(resourcePath)
                    } catch (e: FileSystemAlreadyExistsException) {
                        FileSystems.getFileSystem(uri).getPath(resourcePath)
                    }
                } else {
                    Paths.get(uri)
                }
            } catch (e: Exception) {
                OmMain.getInstance().logger.warning { "Failed to load hostility rules path: ${e.message}" }
                return rules
            }

            try {
                // Ensure the directory stream is closed to prevent resource leaks
                Files.walk(path, 1).use { stream ->
                    stream.forEach { filePath ->
                        val fileName = filePath.fileName.toString()
                        if (fileName.endsWith(".txt")) {
                            val victimName = fileName.removeSuffix(".txt").lowercase()
                            try {
                                @Suppress("DEPRECATION")
                                val victimType = EntityType.fromName(victimName)
                                if (victimType == null) {
                                    OmMain.getInstance().logger.warning {
                                        "Invalid entity type in hostility file: $victimName"
                                    }
                                } else {
                                    // Optimization: Use EnumSet for O(1) lookups and compact memory representation
                                    val aggressors = EnumSet.noneOf(EntityType::class.java)

                                    javaClass.classLoader.getResourceAsStream("$resourcePath/$fileName")?.use { stream ->
                                        BufferedReader(InputStreamReader(stream)).use { reader ->
                                            reader.lineSequence().forEach { line ->
                                                val trimmed = line.trim()
                                                if (trimmed.isNotEmpty()) {
                                                    try {
                                                        @Suppress("DEPRECATION")
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
            } catch (e: Exception) {
                OmMain.getInstance().logger.warning { "Failed to walk hostility rules: ${e.message}" }
            } finally {
                try {
                    fs?.close()
                } catch (e: Exception) {
                    // Ignore close errors
                }
            }
        }

        return rules
    }

    fun shouldAttack(aggressor: EntityType, victim: EntityType): Boolean {
        if (aggressor == EntityType.WARDEN) return true

        return hostilityRules[victim]?.contains(aggressor) == true
    }

    fun getAggressors(victim: EntityType): Set<EntityType>? {
        return hostilityRules[victim]
    }
}
