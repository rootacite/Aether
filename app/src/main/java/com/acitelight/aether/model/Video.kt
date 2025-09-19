package com.acitelight.aether.model

import com.acitelight.aether.service.ApiClient
import kotlinx.serialization.Serializable
import java.security.KeyPair


@Serializable
class Video(
    val isLocal: Boolean,
    val localBase: String,
    val klass: String,
    val id: String,
    val token: String,
    val video: VideoResponse
) {
    fun getCover(): String {
        return if (isLocal)
            "$localBase/videos/$klass/$id/cover.jpg"
        else
            "${ApiClient.getBase()}api/video/$klass/$id/cover?token=$token"
    }

    fun getVideo(): String {
        return if (isLocal)
            "$localBase/videos/$klass/$id/video.mp4"
        else
            "${ApiClient.getBase()}api/video/$klass/$id/av?token=$token"
    }

    fun getSubtitle(): String {
        return if (isLocal)
            "$localBase/videos/$klass/$id/subtitle.ass"
        else
            "${ApiClient.getBase()}api/video/$klass/$id/subtitle?token=$token"
    }

    fun getGallery(): List<KeyImage> {
        return if (isLocal)
            video.gallery.map {
                KeyImage(
                    name = it,
                    url = "$localBase/videos/$klass/$id/gallery/$it",
                    key = "$klass/$id/gallery/$it"
                )
            } else video.gallery.map {
            KeyImage(
                name = it,
                url = "${ApiClient.getBase()}api/video/$klass/$id/gallery/$it?token=$token",
                key = "$klass/$id/gallery/$it"
            )
        }
    }

    fun toLocal(localBase: String): Video
    {
        return Video(
            isLocal = true,
            localBase = localBase,
            klass = klass,
            id = id,
            token = "",
            video = video
        )
    }
}