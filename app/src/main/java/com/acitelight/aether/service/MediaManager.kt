package com.acitelight.aether.service

import android.content.Context
import com.acitelight.aether.helper.DownloadType
import com.acitelight.aether.helper.getType
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicResponse
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.tonyodev.fetch2.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaManager @Inject constructor(
    val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    private val apiClient: ApiClient
) {
    suspend fun listVideoKlasses(): List<String> {
        try {
            val j = apiClient.api!!.getVideoClasses()
            return j.toList()
        } catch (_: Exception) {
            return listOf()
        }
    }

    suspend fun queryVideoKlasses(klass: String): List<String> {
        try {
            val j = apiClient.api!!.queryVideoClasses(klass)
            return j.toList()
        } catch (_: Exception) {
            return listOf()
        }
    }

    suspend fun queryVideo(klass: String, id: String, model: VideoDownloadItemState): Video? {
        if (model.status == Status.COMPLETED) {
            val jsonString = File(
                context.getExternalFilesDir(null),
                "videos/$klass/$id/summary.json"
            ).readText()
            return Json.decodeFromString<Video>(jsonString)
                .toLocal(context.getExternalFilesDir(null)?.path!!)
        }

        try {
            val j = apiClient.api!!.queryVideo(klass, id)
            return Video(klass = klass, id = id, isLocal = false, localBase = "", video = j)
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun queryVideo(klass: String, id: String): Video? {
        val downloaded = fetchManager.getAllDownloadsAsync().filter {
            it.extras.getString("id", "") == id &&
                    it.extras.getString("class", "") == klass
        }

        val summaryFile = File(
            context.getExternalFilesDir(null),
            "videos/$klass/$id/summary.json"
        )

        if (downloaded.any { it.status == Status.COMPLETED && it.getType() == DownloadType.VideoMainFile }
            && downloaded.all {
                it.status == Status.COMPLETED || it.extras.getString(
                    "type",
                    ""
                ) == "subtitle"
            }
            && summaryFile.exists()) {
            val jsonString = summaryFile.readText()
            return Json.decodeFromString<Video>(jsonString)
                .toLocal(context.getExternalFilesDir(null)?.path!!)
        }

        try {
            val j = apiClient.api!!.queryVideo(klass, id)
            return Video(klass = klass, id = id, isLocal = false, localBase = "", video = j)
        } catch (_: Exception) {
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

                if (o.any { it.status == Status.COMPLETED && it.getType() == DownloadType.VideoMainFile }
                    && o.all {
                        it.status == Status.COMPLETED || it.extras.getString(
                            "type",
                            ""
                        ) == "subtitle"
                    }) {
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
                val j = apiClient.api!!.queryVideoBulk(klass, remoteIds)
                j.zip(remoteIds).map {
                    Video(
                        klass = klass,
                        id = it.second,
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

    suspend fun listComics(): List<String> {
        try {
            val j = apiClient.api!!.getComics()
            return j.sorted()
        } catch (_: Exception) {
            return listOf()
        }
    }

    private fun readLocalComicInfo(id: String, file: File): Comic? {
        if (!file.exists()) {
            return null
        }

        try {
            val zipFile = ZipFile(file)
            zipFile.use { zf ->
                val entry = zf.getEntry("summary.json")
                if (entry != null) {
                    zf.getInputStream(entry).use { inputStream ->
                        val cr = Json.decodeFromString<ComicResponse>(
                            String(
                                inputStream.readAllBytes(),
                                Charsets.UTF_8
                            )
                        )
                        return Comic(
                            isLocal = true,
                            localBase = file.path,
                            comic = cr,
                            id = id
                        )
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }
        return null
    }

    suspend fun queryComicInfoSingle(id: String): Comic? {
        val f = File(context.getExternalFilesDir(null), "comics/${id}.zip")

        try {
            if (f.exists()) {
                val r = readLocalComicInfo(id, f)
                r?.let { return it }
            }
            val j = apiClient.api!!.queryComicInfo(id)
            return Comic(isLocal = false, localBase = f.path, id = id, comic = j)
        } catch (_: Exception) {
            return null
        }
    }

    suspend fun queryComicInfoBulk(ids: List<String>): List<Comic>? {
        try {
            val resultMap = mutableMapOf<String, Comic>()
            val remoteIds = mutableListOf<String>()

            for (id in ids) {
                val f = File(context.getExternalFilesDir(null), "comics/${id}.zip")
                val localComic = readLocalComicInfo(id, f)

                if (localComic != null) {
                    resultMap[id] = localComic
                } else {
                    remoteIds.add(id)
                }
            }

            if (remoteIds.isNotEmpty()) {
                val remoteResponses = apiClient.api!!.queryComicInfoBulk(remoteIds)

                remoteResponses.zip(remoteIds).forEach { (comicResponse, id) ->
                    val f = File(context.getExternalFilesDir(null), "comics/${id}.zip")
                    resultMap[id] = Comic(
                        isLocal = false,
                        localBase = f.path,
                        id = id,
                        comic = comicResponse
                    )
                }
            }

            return ids.mapNotNull { id -> resultMap[id] }

        } catch (_: Exception) {
            return null
        }
    }

    suspend fun postBookmark(id: String, bookMark: BookMark): Boolean {
        try {
            apiClient.api!!.postBookmark(id, bookMark)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}