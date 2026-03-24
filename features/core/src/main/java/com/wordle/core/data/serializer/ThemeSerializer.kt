package com.wordle.core.data.serializer

import androidx.datastore.core.Serializer
import com.wordle.core.domain.model.DARK_MODEL
import com.wordle.core.domain.model.ThemeModel
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object ThemeSerializer : Serializer<ThemeModel> {

    override val defaultValue: ThemeModel get() = DARK_MODEL

    override suspend fun readFrom(input: InputStream): ThemeModel =
        try {
            Json.decodeFromString(
                ThemeModel.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            defaultValue
        }

    override suspend fun writeTo(t: ThemeModel, output: OutputStream) {
        output.write(
            Json.encodeToString(ThemeModel.serializer(), t).encodeToByteArray()
        )
    }
}