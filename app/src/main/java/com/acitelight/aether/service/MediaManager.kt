package com.acitelight.aether.service

import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicResponse
import com.acitelight.aether.model.Video


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

    suspend fun listVideos(klass: String, filter: List<String>, callback: (Video) -> Unit)
    {
        val j = ApiClient.api!!.queryVideoClasses(klass, token)
        for(it in j)
        {
            if(filter.contains(it))
                continue
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
        try{
            val j = ApiClient.api!!.getComics(token)
            return j
        }catch (e: Exception)
        {
            return listOf()
        }
    }

    suspend fun queryComicInfo(id: String) : Comic?
    {
        try{
            val j = ApiClient.api!!.queryComicInfo(id, token)
            return Comic(id = id, comic = j, token = token)
        }catch (e: Exception)
        {
            return null
        }
    }
}