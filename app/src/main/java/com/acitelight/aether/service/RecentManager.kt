package com.acitelight.aether.service

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    suspend fun readFile(context: Context, filename: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, filename)
                val content = file.readText()
                content
            } catch (e: FileNotFoundException) {
                "[]"
            } catch (e: IOException) {
                "[]"
            }
        }
    }

    suspend fun writeFile(context: Context, filename: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, filename)
                file.writeText(content)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun Query(context: Context): List<VideoQueryIndex>
    {
        val content = readFile(context, "recent.json")
        try{
            val r = Json.decodeFromString<List<VideoQueryIndex>>(content)

            recent.clear()
            val gr = r.groupBy { it.klass }

            for(it in gr)
            {
                val v = mediaManager.queryVideoBulk(it.key, it.value.map { it.id })
                if(v != null)
                    for(j in v)
                    {
                        recent.add(recent.size, j)
                    }
            }

            return r
        }catch (e: Exception)
        {
            print(e.message)
        }

        return listOf()
    }

    suspend fun Push(context: Context, video: VideoQueryIndex)
    {
        mutex.withLock{
            val content = readFile(context, "recent.json")
            val o = recent.map{ VideoQueryIndex(it.klass, it.id) }.toMutableList()

            if(o.contains(video))
            {
                val index = o.indexOf(video)
                val temp = recent[index]

                recent.removeAt(index)
            }
            recent.add(0, mediaManager.queryVideoBulk(video.klass, listOf(video.id))!![0])


            if(recent.size >= 21)
                recent.removeAt(o.size - 1)

            writeFile(context, "recent.json", Json.encodeToString(recent.map{ VideoQueryIndex(it.klass, it.id) }))
        }
    }

    val recent = mutableStateListOf<Video>()
}