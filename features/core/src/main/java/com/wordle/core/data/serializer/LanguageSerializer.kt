package com.wordle.core.data.serializer

import com.wordle.core.domain.model.LanguageModel
import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object LanguageSerializer : Serializer<LanguageModel> {

    override val defaultValue: LanguageModel
        get() = LanguageModel(
            id          = -1,
            displayName = "",
            code        = ""
        )

    override suspend fun readFrom(input: InputStream): LanguageModel =
        try {
            Json.decodeFromString(
                LanguageModel.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            defaultValue
        }

    override suspend fun writeTo(t: LanguageModel, output: OutputStream) {
        output.write(
            Json.encodeToString(LanguageModel.serializer(), t).encodeToByteArray()
        )
    }
}