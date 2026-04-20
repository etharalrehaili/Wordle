package com.khammin.game.data.remote.model

import com.google.gson.annotations.SerializedName

data class ValidateWordRequest(
    val word: String,
    val language: String,
)

data class ValidateWordResponse(
    @SerializedName("isValid") val isValid: Boolean,
)
