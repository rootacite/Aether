package com.acitelight.aether.service

import android.content.Context
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicResponse
import com.acitelight.aether.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaManager @Inject constructor(

)
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

    suspend fun queryVideoKlasses(klass: String): List<String>
    {
        try
        {
            val j = ApiClient.api!!.queryVideoClasses(klass, token)
            return j.toList()
        }catch(e: Exception)
        {
            return listOf()
        }
    }

    suspend fun queryVideo(klass: String, id: String): Video?
    {
        try {
            val j = ApiClient.api!!.queryVideo(klass, id, token)
            return Video(klass = klass, id = id, token=token, isLocal = false, video = j)
        }catch (e: Exception)
        {
            return null
        }
    }

    suspend fun queryVideoBulk(klass: String, id: List<String>): List<Video>?
    {
        try {
            val j = ApiClient.api!!.queryVideoBulk(klass, id, token)
            return j.zip(id).map {Video(klass = klass, id = it.second, token=token, isLocal = false, video = it.first)}
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

    suspend fun queryComicInfoSingle(id: String) : Comic?
    {
        try{
            val j = ApiClient.api!!.queryComicInfo(id, token)
            return Comic(id = id, comic = j, token = token)
        }catch (e: Exception)
        {
            return null
        }
    }

    suspend fun queryComicInfoBulk(id: List<String>) : List<Comic>?
    {
        try{
            val j = ApiClient.api!!.queryComicInfoBulk(id, token)
            return j.zip(id).map { Comic(id = it.second, comic = it.first, token = token) }
        }catch (e: Exception)
        {
            return null
        }
    }

    suspend fun postBookmark(id: String, bookMark: BookMark): Boolean
    {
        try{
            val j = ApiClient.api!!.postBookmark(id, token, bookMark)
            return true
        }catch (e: Exception)
        {
            return false
        }
    }
}