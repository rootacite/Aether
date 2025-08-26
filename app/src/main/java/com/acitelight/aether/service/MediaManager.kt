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
        try
        {
            val j = ApiClient.api!!.getVideoClasses(token)
            return j.toList()
        }catch(e: Exception)
        {
            return listOf()
        }
    }

    suspend fun listVideos(klass: String, callback: (Video) -> Unit)
    {
        val j = ApiClient.api!!.queryVideoClasses(klass, token)
        for(it in j)
        {
            try {
                callback(queryVideo(klass, it)!!)
            }catch (e: Exception)
            {

            }
        }
    }

    suspend fun queryVideo(klass: String, id: String): Video?
    {
        try {
            val j = ApiClient.api!!.queryVideo(klass, id, token)
            return Video(klass = klass, id = id, token=token, j)
        }catch (e: Exception)
        {
            return null
        }
    }

    suspend fun listComics() : List<String>
    {
        // TODO: try
        return ApiClient.api!!.getComicCollections()
    }

    suspend fun queryComicInfo(c: String) : Comic
    {
        // TODO: try
        return ApiClient.api!!.queryComicInfo(c)
    }
}