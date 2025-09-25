package com.acitelight.aether.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoResponse(
    val name: String,
    val duration: Long,
    val gallery: List<String>,
    val comment: List<Comment>,
    val star: Boolean,
    val like: Int,
    val author: String,
    val group: String?
)
