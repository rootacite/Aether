package com.acitelight.aether.model

import kotlinx.serialization.Serializable

@Serializable
data class BookMark(
    val name: String,
    val page: String
)