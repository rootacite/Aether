package com.acitelight.aether.model

import kotlinx.serialization.Serializable

@Serializable
data class ComicResponse(
    val comic_name: String,
    val page_count: Int,
    val bookmarks: List<BookMark>,
    val list: List<String>,
    val tags: List<String>,
    val author: String,
    val cover: String? = null
)