package com.acitelight.aether.service

import android.content.Context
import com.acitelight.aether.model.Video
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.Extras
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: ApiClient
) {
    private var fetch: Fetch? = null
    private var listener: FetchListener? = null
    val configured = MutableStateFlow(false)

    fun init() {
        val fetchConfiguration = FetchConfiguration.Builder(context)
            .setDownloadConcurrentLimit(8)
            .setHttpDownloader(OkHttpDownloader(apiClient.getClient()))
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

    suspend fun getAllDownloadsAsync(): List<Download> {
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

    fun retry(id: Int) {
        fetch?.retry(id)
    }

    fun cancel(id: Int) {
        fetch?.cancel(id)
    }

    fun delete(id: Int, callback: (() -> Unit)? = null) {
        fetch?.delete(id) {
            callback?.invoke()
        } ?: callback?.invoke()
    }

    private suspend fun enqueue(
        request: Request,
        onEnqueued: ((Request) -> Unit)? = null,
        onError: ((com.tonyodev.fetch2.Error) -> Unit)? = null
    ) {
        configured.filter { it }.first()
        fetch?.enqueue(request, { r -> onEnqueued?.invoke(r) }, { e -> onError?.invoke(e) })
    }

    private fun makeFolder(video: Video) {
        val appFilesDir = context.getExternalFilesDir(null)
        val videosDir = File(appFilesDir, "videos/${video.klass}/${video.id}/gallery")
        videosDir.mkdirs()
    }

    suspend fun startVideoDownload(video: Video) {
        if(getAllDownloadsAsync().any{
            it.extras.getString("class", "") == video.klass && it.extras.getString("id", "") == video.id })
            return

        makeFolder(video)
        File(
            context.getExternalFilesDir(null),
            "videos/${video.klass}/${video.id}/summary.json"
        ).writeText(Json.encodeToString(video))

        val videoPath =
            File(context.getExternalFilesDir(null), "videos/${video.klass}/${video.id}/video.mp4")
        val coverPath =
            File(context.getExternalFilesDir(null), "videos/${video.klass}/${video.id}/cover.jpg")
        val subtitlePath = File(
            context.getExternalFilesDir(null),
            "videos/${video.klass}/${video.id}/subtitle.vtt"
        )

        val requests = mutableListOf(
            Request(video.getVideo(apiClient), videoPath.path).apply {
                extras = Extras(
                    mapOf(
                        "name" to video.video.name,
                        "id" to video.id,
                        "class" to video.klass,
                        "group" to (video.video.group ?: ""),
                        "type" to "main"
                    )
                )
            },
            Request(video.getCover(apiClient), coverPath.path).apply {
                extras = Extras(
                    mapOf(
                        "name" to video.video.name,
                        "id" to video.id,
                        "class" to video.klass,
                        "group" to (video.video.group ?: ""),
                        "type" to "cover"
                    )
                )
            },
            Request(video.getSubtitle(apiClient), subtitlePath.path).apply {
                extras = Extras(
                    mapOf(
                        "name" to video.video.name,
                        "id" to video.id,
                        "class" to video.klass,
                        "group" to (video.video.group ?: ""),
                        "type" to "subtitle"
                    )
                )
            },
        )
        for (p in video.getGallery(apiClient)) {
            requests.add(
                Request(p.url, File(
                    context.getExternalFilesDir(null),
                    "videos/${video.klass}/${video.id}/gallery/${p.name}"
                ).path).apply {
                    extras = Extras(
                        mapOf(
                            "name" to video.video.name,
                            "id" to video.id,
                            "class" to video.klass,
                            "group" to (video.video.group ?: ""),
                            "type" to "gallery"
                        )
                    )
                }
            )
        }

        for (i in requests)
            enqueue(i)
    }
}