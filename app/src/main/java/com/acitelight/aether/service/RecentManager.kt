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

object RecentManager
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

            for(it in r)
            {
                val v = MediaManager.queryVideo(it.klass, it.id)
                if(v != null)
                    recent.add(recent.size, v)
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
            var o = Json.decodeFromString<List<VideoQueryIndex>>(content).toMutableList();

            if(o.contains(video))
            {
                val temp = o[0]
                val index = o.indexOf(video)
                recent.removeAt(index)
                o[0] = o[index]
                o[index] = temp
            }
            else
            {
                o.add(0, video)
            }

            if(o.size >= 21)
                o.removeAt(o.size - 1)

            recent.add(0, MediaManager.queryVideo(video.klass, video.id)!!)
            writeFile(context, "recent.json", Json.encodeToString(o))
        }
    }

    val recent = mutableStateListOf<Video>()
}