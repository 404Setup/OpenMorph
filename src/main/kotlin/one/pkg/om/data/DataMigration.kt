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
import kotlinx.io.Source
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

interface DataMigrator {
    val version: String
    fun migrate(source: Source): SaveMorphData
}


@OMData(version = 5)
class V5Migrator : DataMigrator {
    override val version = "V5"
    override fun migrate(source: Source): SaveMorphData {
        val v5 = Avro.decodeFromSource<SaveMorphDataV5>(source)
        return SaveMorphData(
            player = v5.player,
            blocks = v5.blocks,
            entities = v5.entities,
            players = v5.players,
            activeMorphType = v5.activeMorphType,
            activeMorphName = v5.activeMorphName,
            activeMorphSkin = v5.activeMorphSkin,
            activeMorphSignature = v5.activeMorphSignature,
            activeMorphEntityUuid = v5.activeMorphEntityUuid,
            solidifiedBlockParams = v5.solidifiedBlockParams,
            forcedKeyGameMode = v5.forcedKeyGameMode,
            originalMaxHealth = null
        )
    }
}

@OMData(version = 4)
class V4Migrator : DataMigrator {
    override val version = "V4"
    override fun migrate(source: Source): SaveMorphData {
        val v4 = Avro.decodeFromSource<SaveMorphDataV4>(source)
        return SaveMorphData(
            player = v4.player,
            blocks = v4.blocks,
            entities = v4.entities,
            players = v4.players,
            activeMorphType = v4.activeMorphType,
            activeMorphName = v4.activeMorphName,
            activeMorphSkin = v4.activeMorphSkin,
            activeMorphSignature = v4.activeMorphSignature,
            activeMorphEntityUuid = v4.activeMorphEntityUuid,
            solidifiedBlockParams = v4.solidifiedBlockParams,
            forcedKeyGameMode = null,
            originalMaxHealth = null
        )
    }
}

@OMData(version = 3)
class V3Migrator : DataMigrator {
    override val version = "V3"
    override fun migrate(source: Source): SaveMorphData {
        val v3 = Avro.decodeFromSource<SaveMorphDataV3>(source)
        return SaveMorphData(
            player = v3.player,
            blocks = v3.blocks,
            entities = v3.entities,
            players = v3.players,
            activeMorphType = v3.activeMorphType,
            activeMorphName = v3.activeMorphName,
            activeMorphSkin = v3.activeMorphSkin,
            activeMorphSignature = v3.activeMorphSignature,
            activeMorphEntityUuid = v3.activeMorphEntityUuid,
            solidifiedBlockParams = null,
            forcedKeyGameMode = null,
            originalMaxHealth = null
        )
    }
}

@OMData(version = 2)
class V2Migrator : DataMigrator {
    override val version = "V2"
    override fun migrate(source: Source): SaveMorphData {
        val v2 = Avro.decodeFromSource<SaveMorphDataV2>(source)
        return SaveMorphData(
            player = v2.player,
            blocks = v2.blocks,
            entities = v2.entities,
            players = v2.players,
            activeMorphType = v2.activeMorphType,
            activeMorphName = v2.activeMorphName,
            activeMorphSkin = v2.activeMorphSkin,
            activeMorphSignature = v2.activeMorphSignature,
            activeMorphEntityUuid = null,
            solidifiedBlockParams = null,
            forcedKeyGameMode = null,
            originalMaxHealth = null
        )
    }
}

@OMData(version = 1)
class V1Migrator : DataMigrator {
    override val version = "V1"
    override fun migrate(source: Source): SaveMorphData {
        val v1 = Avro.decodeFromSource<SaveMorphDataV1>(source)
        return SaveMorphData(
            player = v1.player,
            blocks = v1.blocks,
            entities = v1.entities,
            players = v1.players,
            activeMorphType = null,
            activeMorphName = null,
            activeMorphSkin = null,
            activeMorphSignature = null,
            activeMorphEntityUuid = null,
            solidifiedBlockParams = null,
            forcedKeyGameMode = null,
            originalMaxHealth = null
        )
    }
}

@Serializable
internal data class SaveMorphDataV5(
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
    var forcedKeyGameMode: String? = null
)

@Serializable
internal data class SaveMorphDataV4(
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
    var solidifiedBlockParams: String? = null
)

@Serializable
internal data class SaveMorphDataV3(
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
    var activeMorphEntityUuid: UUID? = null
)

@Serializable
internal data class SaveMorphDataV2(
    @Contextual
    val player: UUID,
    val blocks: MutableList<String>,
    val entities: MutableList<String>,
    val players: MutableList<SavePlayerData>,
    var activeMorphType: String? = null,
    var activeMorphName: String? = null,
    var activeMorphSkin: String? = null,
    var activeMorphSignature: String? = null
)

@Serializable
internal data class SaveMorphDataV1(
    @Contextual
    val player: UUID,
    val blocks: MutableList<String>,
    val entities: MutableList<String>,
    val players: MutableList<SavePlayerData>
)
