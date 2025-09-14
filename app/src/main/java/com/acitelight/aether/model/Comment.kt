package com.acitelight.aether.model

import kotlinx.serialization.Serializable


@Serializable
data class Comment(
    val content: String,
    val username: String,
    val time: String
)
