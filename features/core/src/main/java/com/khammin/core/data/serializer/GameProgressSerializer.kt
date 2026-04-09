package com.khammin.core.data.serializer

import androidx.datastore.core.Serializer
import com.khammin.core.domain.model.GameProgress
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object GameProgressSerializer : Serializer<GameProgress> {

    override val defaultValue: GameProgress get() = GameProgress()

    override suspend fun readFrom(input: InputStream): GameProgress =
        try {
            Json.decodeFromString(GameProgress.serializer(), input.readBytes().decodeToString())
        } catch (e: Exception) {
            defaultValue
        }

    override suspend fun writeTo(t: GameProgress, output: OutputStream) {
        output.write(Json.encodeToString(GameProgress.serializer(), t).encodeToByteArray())
    }
}
