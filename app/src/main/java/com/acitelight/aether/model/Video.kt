package com.acitelight.aether.model

import com.acitelight.aether.service.ApiClient
import java.security.KeyPair

class Video constructor(
    val klass: String,
    val id: String,
    val token: String,
    val video: VideoResponse
    ){
    fun getCover(): String
    {
        return "${ApiClient.base}api/video/$klass/$id/cover?token=$token"
    }

    fun getVideo(): String
    {
        return "${ApiClient.base}api/video/$klass/$id/av?token=$token"
    }

    fun getGallery(): List<KeyImage>
    {
        return video.gallery.map{
            KeyImage(url = "${ApiClient.base}api/video/$klass/$id/gallery/$it?token=$token", key = "$klass/$id/gallery/$it")
        }
    }

}