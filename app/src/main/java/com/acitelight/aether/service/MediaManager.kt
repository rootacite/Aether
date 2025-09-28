package com.acitelight.aether.service

import android.content.Context
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.tonyodev.fetch2.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaManager @Inject constructor(
    val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    private val apiClient: ApiClient
)
{
    var token: String = "null"

    suspend fun listVideoKlasses(): List<String>
    {
        try
        {
            val j = apiClient.api!!.getVideoClasses(token)
            return j.toList()
        }catch(_: Exception)
        {
            return listOf()
        }
    }

    suspend fun queryVideoKlasses(klass: String): List<String>
    {
        try
        {
            val j = apiClient.api!!.queryVideoClasses(klass, token)
            return j.toList()
        }catch(_: Exception)
        {
            return listOf()
        }
    }

    suspend fun queryVideo(klass: String, id: String, model: VideoDownloadItemState): Video?
    {
        if(model.status == Status.COMPLETED)
        {
            val jsonString = File(
                context.getExternalFilesDir(null),
                "videos/$klass/$id/summary.json"
            ).readText()
            return Json.decodeFromString<Video>(jsonString).toLocal(context.getExternalFilesDir(null)?.path!!)
        }

        try {
            val j = apiClient.api!!.queryVideo(klass, id, token)
            return Video(klass = klass, id = id, token=token, isLocal = false, localBase = "", video = j)
        }catch (_: Exception)
        {
            return null
        }
    }

    suspend fun queryVideo(klass: String, id: String): Video?
    {
        val downloaded = fetchManager.getAllDownloadsAsync().filter {
            it.extras.getString("id", "") == id &&
            it.extras.getString("class", "") == klass
        }

        if(downloaded.any{ it.status == Status.COMPLETED }
            && downloaded.all{ it.status == Status.COMPLETED || it.extras.getString("type", "") == "subtitle" })
        {
            val jsonString = File(
                context.getExternalFilesDir(null),
                "videos/$klass/$id/summary.json"
            ).readText()
            return Json.decodeFromString<Video>(jsonString).toLocal(context.getExternalFilesDir(null)?.path!!)
        }

        try {
            val j = apiClient.api!!.queryVideo(klass, id, token)
            return Video(klass = klass, id = id, token=token, isLocal = false, localBase = "", video = j)
        }catch (_: Exception)
        {
            return null
        }
    }

    suspend fun queryVideoBulk(klass: String, id: List<String>): List<Video>? {
        return try {
            val downloads = fetchManager.getAllDownloadsAsync()

            val localIds = mutableSetOf<String>()
            val remoteIds = mutableListOf<String>()

            for (videoId in id) {
                val o = downloads.filter {
                    it.extras.getString("id", "") == videoId &&
                            it.extras.getString("class", "") == klass
                }

                if (o.any{ it.status == Status.COMPLETED }
                    && o.all{ it.status == Status.COMPLETED || it.extras.getString("type", "") == "subtitle" })
                {
                    localIds.add(videoId)
                } else {
                    remoteIds.add(videoId)
                }
            }

            val localVideos = localIds.mapNotNull { videoId ->
                val localFile = File(
                    context.getExternalFilesDir(null),
                    "videos/$klass/$videoId/summary.json"
                )
                if (localFile.exists()) {
                    try {
                        val jsonString = localFile.readText()
                        Json.decodeFromString<Video>(jsonString).toLocal(
                            context.getExternalFilesDir(null)?.path ?: ""
                        )
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

            val remoteVideos = if (remoteIds.isNotEmpty()) {
                val j = apiClient.api!!.queryVideoBulk(klass, remoteIds, token)
                j.zip(remoteIds).map {
                    Video(
                        klass = klass,
                        id = it.second,
                        token = token,
                        isLocal = false,
                        localBase = "",
                        video = it.first
                    )
                }
            } else {
                emptyList()
            }

            localVideos + remoteVideos
        } catch (_: Exception) {
            null
        }
    }

    suspend fun listComics() : List<String>
    {
        try{
            val j = apiClient.api!!.getComics(token)
            return j
        }catch (_: Exception)
        {
            return listOf()
        }
    }

    suspend fun queryComicInfoSingle(id: String) : Comic?
    {
        try{
            val j = apiClient.api!!.queryComicInfo(id, token)
            return Comic(id = id, comic = j, token = token)
        }catch (_: Exception)
        {
            return null
        }
    }

    suspend fun queryComicInfoBulk(id: List<String>) : List<Comic>?
    {
        try{
            val j = apiClient.api!!.queryComicInfoBulk(id, token)
            return j.zip(id).map { Comic(id = it.second, comic = it.first, token = token) }
        }catch (_: Exception)
        {
            return null
        }
    }

    suspend fun postBookmark(id: String, bookMark: BookMark): Boolean
    {
        try{
            apiClient.api!!.postBookmark(id, token, bookMark)
            return true
        }catch (_: Exception)
        {
            return false
        }
    }
}