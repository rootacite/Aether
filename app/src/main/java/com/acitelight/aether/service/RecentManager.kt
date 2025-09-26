package com.acitelight.aether.service

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentManager @Inject constructor(
    private val mediaManager: MediaManager
)
{
    private val mutex = Mutex()

    private suspend fun readFile(context: Context, filename: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, filename)
                val content = file.readText()
                content
            } catch (_: FileNotFoundException) {
                "[]"
            } catch (_: IOException) {
                "[]"
            }
        }
    }

    private suspend fun writeFile(context: Context, filename: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, filename)
                file.writeText(content)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun queryComic(context: Context): List<String> {
        val content = readFile(context, "recent_comic.json")
        try {
            val ids = Json.decodeFromString<List<String>>(content)


            recentComic.clear()

            try {
                val comics = mediaManager.queryComicInfoBulk(ids)
                if (comics != null) {
                    for (c in comics) {
                        recentComic.add(recentComic.size, c)
                    }
                } else {
                    for (id in ids) {
                        val c = mediaManager.queryComicInfoSingle(id)
                        if (c != null) recentComic.add(recentComic.size, c)
                    }
                }
            } catch (_: NoSuchMethodError) {
                for (id in ids) {
                    val c = mediaManager.queryComicInfoSingle(id)
                    if (c != null) recentComic.add(recentComic.size, c)
                }
            } catch (_: Exception) {
                for (id in ids) {
                    val c = mediaManager.queryComicInfoSingle(id)
                    if (c != null) recentComic.add(recentComic.size, c)
                }
            }


            return ids
        } catch (e: Exception) {
            print(e.message)
        }


        return listOf()
    }

    suspend fun pushComic(context: Context, comicId: String) {
        mutex.withLock {
            val o = recentComic.map { it.id }.toMutableList()


            if (o.contains(comicId)) {
                val index = o.indexOf(comicId)
                recentComic.removeAt(index)
            }


            val comic = mediaManager.queryComicInfoSingle(comicId)
            if (comic != null) {
                recentComic.add(0, comic)
            } else {
                return
            }

            if (recentComic.size > 21) {
                recentComic.removeAt(recentComic.size - 1)
            }


            writeFile(context, "recent_comic.json", Json.encodeToString(recentComic.map { it.id }))
        }
    }

    suspend fun queryVideo(context: Context): List<VideoQueryIndex>
    {
        val content = readFile(context, "recent.json")
        try{
            val r = Json.decodeFromString<List<VideoQueryIndex>>(content)

            recentVideo.clear()
            val gr = r.groupBy { it.klass }

            for(it in gr)
            {
                val v = mediaManager.queryVideoBulk(it.key, it.value.map { it.id })
                if(v != null)
                    for(j in v)
                    {
                        recentVideo.add(recentVideo.size, j)
                    }
            }

            return r
        }catch (e: Exception)
        {
            print(e.message)
        }

        return listOf()
    }

    suspend fun pushVideo(context: Context, video: VideoQueryIndex)
    {
        mutex.withLock{
            val o = recentVideo.map{ VideoQueryIndex(it.klass, it.id) }.toMutableList()

            if(o.contains(video))
            {
                val index = o.indexOf(video)
                recentVideo.removeAt(index)
            }
            recentVideo.add(0, mediaManager.queryVideo(video.klass, video.id)!!)


            if(recentVideo.size >= 21)
                recentVideo.removeAt(o.size - 1)

            writeFile(context, "recent.json", Json.encodeToString(recentVideo.map{ VideoQueryIndex(it.klass, it.id) }))
        }
    }

    val recentVideo = mutableStateListOf<Video>()
    val recentComic = mutableStateListOf<Comic>()
}