package com.acitelight.aether.model

data class ComicResponse(
    val comic_name: String,
    val page_count: Int,
    val bookmarks: List<BookMark>,
    val list: List<String>,
    val author: String
)