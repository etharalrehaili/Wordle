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
    val attributes: WordAttributes? = null,
)

data class WordAttributes(
    val text: String? = null,
    val language: String? = null,
    val length: Int? = null,
)

fun WordItem.resolvedText(): String {
    val top = text?.trim()
    if (!top.isNullOrEmpty()) return top
    return attributes?.text?.trim().orEmpty()
}