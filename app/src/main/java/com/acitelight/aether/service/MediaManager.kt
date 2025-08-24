package com.acitelight.aether.service

import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


object MediaManager
{
    var token: String = "null"

    suspend fun listVideoKlasses(): List<String>
    {
        val j = ApiClient.api.getVideoClasses(token)
        return j.toList()
    }

    suspend fun listVideos(klass: String): List<Video>
    {
        val j = ApiClient.api.queryVideoClasses(klass, token)
        return j.map{
            queryVideo(klass, it)
        }.toList()
    }

    suspend fun queryVideo(klass: String, id: String): Video
    {
        val j = ApiClient.api.queryVideo(klass, id, token)
        return Video(klass = klass, id = id, token=token, j)
    }

    suspend fun listComics() : List<String>
    {
        return ApiClient.api.getComicCollections()
    }

    suspend fun queryComicInfo(c: String) : Comic
    {
        return ApiClient.api.queryComicInfo(c)
    }
}