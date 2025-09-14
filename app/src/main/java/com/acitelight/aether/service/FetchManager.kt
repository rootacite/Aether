package com.acitelight.aether.service

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.acitelight.aether.Screen
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.Extras
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var fetch: Fetch? = null
    private var listener: FetchListener? = null
    private var client: OkHttpClient? = null
    val configured = MutableStateFlow(false)

    fun init()
    {
        client = createOkHttp()
        val fetchConfiguration = FetchConfiguration.Builder(context)
            .setDownloadConcurrentLimit(8)
            .setHttpDownloader(OkHttpDownloader(client))
            .build()

        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        configured.update { true }
    }

    // listener management
    suspend fun setListener(l: FetchListener) {
        configured.filter { it }.first()

        listener?.let { fetch?.removeListener(it) }
        listener = l
        fetch?.addListener(l)
    }

    fun removeListener() {
        listener?.let {
            fetch?.removeListener(it)
        }
        listener = null
    }

    // query downloads
    suspend fun getAllDownloads(callback: (List<Download>) -> Unit) {
        configured.filter { it }.first()
        fetch?.getDownloads { list -> callback(list) } ?: callback(emptyList())
    }

    suspend fun getAllDownloadsAsync(): List<Download>
    {
        configured.filter { it }.first()
        val completed = MutableStateFlow(false)
        var r = listOf<Download>()

        fetch?.getDownloads { list ->
            r = list
            completed.update { true }
        }

        completed.filter { it }.first()
        return r
    }

    // operations
    fun pause(id: Int) {
        fetch?.pause(id)
    }

    fun resume(id: Int) {
        fetch?.resume(id)
    }

    fun cancel(id: Int) {
        fetch?.cancel(id)
    }

    fun delete(id: Int, callback: (() -> Unit)? = null) {
        fetch?.delete(id) {
            callback?.invoke()
        } ?: callback?.invoke()
    }

    private suspend fun enqueue(request: Request, onEnqueued: ((Request) -> Unit)? = null, onError: ((com.tonyodev.fetch2.Error) -> Unit)? = null) {
        configured.filter { it }.first()
        fetch?.enqueue(request, { r -> onEnqueued?.invoke(r) }, { e -> onError?.invoke(e) })
    }

    private fun getVideosDirectory() {
        val appFilesDir = context.getExternalFilesDir(null)
        val videosDir = File(appFilesDir, "videos")

        if (!videosDir.exists()) {
            val created = videosDir.mkdirs()
        }
    }

    suspend fun downloadFile(
        client: OkHttpClient,
        url: String,
        destFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Unexpected code $response"))
                }

                destFile.parentFile?.mkdirs()
                response.body.byteStream().use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startVideoDownload(video: Video)
    {
        val path = File(context.getExternalFilesDir(null), "videos/${video.klass}/${video.id}/video.mp4")
        val request = Request(video.getVideo(), path.path).apply {
            extras = Extras(mapOf("name" to video.video.name, "id" to video.id, "class" to video.klass))
        }

        downloadFile(
            client!!,
            video.getCover(),
            File(context.getExternalFilesDir(null), "videos/${video.klass}/${video.id}/cover.jpg"))

        enqueue(request)
        File(context.getExternalFilesDir(null), "videos/${video.klass}/${video.id}/summary.json").writeText(Json.encodeToString(video))

        for(p in video.getGallery())
        {
            downloadFile(
                client!!,
                p.url,
                File(context.getExternalFilesDir(null), "videos/${video.klass}/${video.id}/gallery/${p.name}"))
        }
    }
}