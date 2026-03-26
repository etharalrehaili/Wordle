package com.khammin.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageModel(
    val id: Int,
    val code: String,
    val displayName: String
)

val ENGLISH_MODEL = LanguageModel(
    id          = -1,
    displayName = Languages.EN.displayName,
    code        = Languages.EN.code
)

val ARABIC_MODEL = LanguageModel(
    id          = 1,
    displayName = Languages.AR.displayName,
    code        = Languages.AR.code
)

fun LanguageModel.isArabic() = code == Languages.AR.code

enum class Languages(val code: String, val displayName: String) {
    AR("ar", "العربية"),
    EN("en", "English")
}