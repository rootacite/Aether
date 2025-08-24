package com.acitelight.aether.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoQueryIndex(
    val klass: String,
    val id: String
)
