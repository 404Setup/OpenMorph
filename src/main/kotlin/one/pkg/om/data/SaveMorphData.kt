/*
 * Copyright (C) 2026  404Setup.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 */

package one.pkg.om.data

import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.decodeFromSource
import com.github.avrokotlin.avro4k.encodeToSink
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import one.pkg.om.OmMain
import one.pkg.om.manager.OManager
import one.pkg.om.utils.ClassScanner
import one.pkg.om.utils.div
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.util.*

@Serializable
data class SaveMorphData(
    @Contextual
    val player: UUID,
    val blocks: MutableList<String>,
    val entities: MutableList<String>,
    val players: MutableList<SavePlayerData>,
    var activeMorphType: String? = null,
    var activeMorphName: String? = null,
    var activeMorphSkin: String? = null,
    var activeMorphSignature: String? = null,
    @Contextual
    var activeMorphEntityUuid: UUID? = null,
    var solidifiedBlockParams: String? = null,
    var forcedKeyGameMode: String? = null,
    var originalMaxHealth: Double? = null
) {
    @Transient
    private var isDirty: Boolean = false

    @Transient
    private lateinit var playerEntity: Player

    fun player() = playerEntity

    fun updatePlayer(p: Player) {
        playerEntity = p
    }

    fun markDirty() {
        isDirty = true
    }

    fun setActiveMorph(type: String, name: String, skin: String? = null, signature: String? = null) {
        this.activeMorphType = type
        this.activeMorphName = name
        this.activeMorphSkin = skin
        this.activeMorphSignature = signature
        markDirty()
    }

    fun setActiveMorphEntity(uuid: UUID) {
        this.activeMorphEntityUuid = uuid
        markDirty()
        OManager.savePlayer(playerEntity, false)
    }

    fun clearActiveMorphEntity(sync: Boolean = false) {
        if (this.activeMorphEntityUuid != null) {
            this.activeMorphEntityUuid = null
            markDirty()
            OManager.savePlayer(playerEntity, sync)
        }
    }

    fun setSolidifiedBlock(world: String, x: Int, y: Int, z: Int, gameMode: String) {
        this.solidifiedBlockParams = "$world;$x;$y;$z"
        this.forcedKeyGameMode = gameMode
        markDirty()
        OManager.savePlayer(playerEntity, false)
    }

    fun clearSolidifiedBlock() {
        if (this.solidifiedBlockParams != null || this.forcedKeyGameMode != null) {
            this.solidifiedBlockParams = null
            this.forcedKeyGameMode = null
            markDirty()
            OManager.savePlayer(playerEntity, true)
        }
    }

    fun clearActiveMorph() {
        if (activeMorphType != null) {
            this.activeMorphType = null
            this.activeMorphName = null
            this.activeMorphSkin = null
            this.activeMorphSignature = null
            this.originalMaxHealth = null
            markDirty()
        }
    }

    fun hasBlock(material: String): Boolean {
        if (blocks.contains("all")) return true
        return blocks.contains(material.uppercase())
    }

    fun addBlock(material: String): Boolean {
        val mat = material.uppercase()
        if (mat == "all") {
            blocks.clear()
            blocks.add("all")
            markDirty()
            return true
        } else if (blocks.contains("all")) return false

        if (!blocks.contains(mat)) {
            blocks.add(mat)
            markDirty()
            return true
        }
        return false
    }

    fun removeBlock(material: String): Boolean {
        val mat = material.uppercase()
        if (mat == "ALL") {
            if (blocks.isNotEmpty()) {
                blocks.clear()
                markDirty()
                return true
            }
        } else if (blocks.remove(mat)) {
            markDirty()
            return true
        }
        return false
    }

    fun hasEntity(type: String): Boolean {
        if (entities.contains("ALL")) return true
        return entities.contains(type.uppercase())
    }

    fun addEntity(type: String) {
        val et = type.lowercase()
        if (et == "all") {
            entities.clear()
            entities.add("all")
            markDirty()
            return
        }
        if (entities.contains("all")) return

        if (!entities.contains(et)) {
            entities.add(et)
            markDirty()
        }
    }

    fun removeEntity(type: String): Boolean {
        val et = type.uppercase()
        if (et == "all") {
            if (entities.isNotEmpty()) {
                entities.clear()
                markDirty()
                return true
            }
        } else if (entities.remove(et)) {
            markDirty()
            return true
        }
        return false
    }

    fun hasPlayer(uuid: UUID): Boolean {
        return players.any { it.uuid == uuid } || players.any { it.name == "all" }
    }

    fun hasPlayer(name: String): Boolean {
        return players.any { it.name.equals(name, ignoreCase = true) } || players.any { it.name == "all" }
    }

    fun addPlayer(data: SavePlayerData) {
        players.removeIf { it.name.equals(data.name, ignoreCase = true) || it.uuid == data.uuid }
        players.add(data)
        markDirty()
    }

    fun removePlayer(name: String): Boolean {
        if (name.equals("all", ignoreCase = true)) {
            if (players.isNotEmpty()) {
                players.clear()
                markDirty()
                return true
            }
        } else if (players.removeIf { it.name.equals(name, ignoreCase = true) }) {
            markDirty()
            return true
        }
        return false
    }

    fun getSnapshotAndClearDirty(): SaveMorphData? {
        if (!isDirty) return null
        val snapshot = this.copy(
            blocks = ArrayList(blocks),
            entities = ArrayList(entities),
            players = ArrayList(players.map { it.copy() })
        )
        isDirty = false
        return snapshot
    }

    fun saveToDisk() {
        val file = (OmMain.getInstance().playerSaveDir / "$player.dat")
        if (file.exists() && file.length() > 0) {
            file.delete()
            file.createNewFile()
        }
        file.outputStream().asSink().buffered().use {
            Avro.encodeToSink(this, it)
        }
    }

    fun saveMe() {
        if (!isDirty) return
        saveToDisk()
        isDirty = false
    }

    companion object {
        fun create(player: Player) =
            create(player.uniqueId)

        fun create(player: UUID): SaveMorphData {
            val file = (OmMain.getInstance().playerSaveDir / "$player.dat")
            if (!file.exists()) {
                file.createNewFile()
                return empty(player).apply { markDirty(); saveMe() }
            }

            try {
                file.inputStream().asSource().buffered().use {
                    return Avro.decodeFromSource<SaveMorphData>(it).apply {
                        playerEntity = Bukkit.getPlayer(player) ?: throw IllegalArgumentException("Player not found")
                    }
                }
            } catch (e: Exception) {
                val migrators = ClassScanner.scanClasses("one.pkg.om.data")
                    .filter { it.isAnnotationPresent(OMData::class.java) && DataMigrator::class.java.isAssignableFrom(it) }
                    .map { it.getDeclaredConstructor().newInstance() as DataMigrator }
                    .sortedByDescending { it.javaClass.getAnnotation(OMData::class.java).version }

                val errors = mutableListOf<String>()
                errors.add("Current=${e.message}")

                for (migrator in migrators) {
                    try {
                        file.inputStream().asSource().buffered().use {
                            val data = migrator.migrate(it)
                            OmMain.getInstance().logger.info("Migrating data for $player from ${migrator.version} to Current...")
                            return data.apply {
                                playerEntity =
                                    Bukkit.getPlayer(player) ?: throw IllegalArgumentException("Player not found")
                                markDirty()
                                saveMe()
                            }
                        }
                    } catch (migrationEx: Exception) {
                        errors.add("${migrator.version}=${migrationEx.message}")
                    }
                }

                OmMain.getInstance().logger.severe("Failed to load data for $player. Errors: ${errors.joinToString(", ")}")
                val backup = File(file.parent, "${file.name}.bak")
                file.copyTo(backup, overwrite = true)
                OmMain.getInstance().logger.info("Corrupted data backed up to ${backup.name}. Creating new data.")

                file.delete()
                file.createNewFile()
                return empty(player).apply { markDirty(); saveMe() }
            }
        }

        fun empty(player: UUID): SaveMorphData =
            empty(Bukkit.getPlayer(player) ?: throw IllegalArgumentException("Player not found"))

        fun empty(player: Player): SaveMorphData {
            return SaveMorphData(player.uniqueId, ArrayList(), ArrayList(), ArrayList()).apply {
                playerEntity = player
            }
        }
    }
}

@Serializable
data class SavePlayerData(
    @Contextual
    val uuid: UUID,
    val name: String,
    var skin: String,
    var lastUpdate: Long = System.currentTimeMillis()
)
