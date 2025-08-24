package com.acitelight.aether.model

data class Comic(
    val comic_name: String,
    val page_count: Int,
    val bookmarks: List<BookMark>,
    val pages: List<String>
)