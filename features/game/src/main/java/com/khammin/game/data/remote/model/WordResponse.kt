package com.khammin.game.data.remote.model

data class WordResponse(
    val data: List<WordItem>,
    val meta: Meta? = null,
)

data class Meta(
    val pagination: Pagination? = null,
)

data class Pagination(
    val page: Int = 1,
    val pageSize: Int = 25,
    val pageCount: Int = 1,
    val total: Int = 0,
)

data class WordItem(
    val id: Int,
    val documentId: String? = null,
    val text: String? = null,
    val language: String? = null,
    val length: Int? = null,
    val meaning: String? = null,           // Strapi v4 flat format
    val attributes: WordAttributes? = null, // Strapi v3 nested format (fallback)
)

data class WordAttributes(
    val text: String? = null,
    val language: String? = null,
    val length: Int? = null,
    val meaning: String? = null
)

data class WordData(
    val text: String,
    val meaning: String? = null
)

fun WordItem.resolvedText(): String {
    val top = text?.trim()
    if (!top.isNullOrEmpty()) return top
    return attributes?.text?.trim().orEmpty()
}

/** Returns the meaning from the Strapi v4 flat field first, then the v3 attributes fallback. */
fun WordItem.resolvedMeaning(): String? {
    val top = meaning?.trim()
    if (!top.isNullOrEmpty()) return top
    return attributes?.meaning?.trim()?.takeIf { it.isNotEmpty() }
}