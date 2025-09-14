package com.acitelight.aether.service

import android.content.Context
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var fetch: Fetch? = null
    private var listener: FetchListener? = null

    fun init()
    {
        val fetchConfiguration = FetchConfiguration.Builder(context)
            .setDownloadConcurrentLimit(8)
            .setHttpDownloader(OkHttpDownloader(createOkHttp()))
            .build()

         fetch = Fetch.Impl.getInstance(fetchConfiguration)
    }

    // listener management
    fun setListener(l: FetchListener) {
        if (fetch == null)
            return
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
    fun getAllDownloads(callback: (List<Download>) -> Unit) {
        if (fetch == null) init()
        fetch?.getDownloads { list -> callback(list) } ?: callback(emptyList())
    }

    fun getDownloadsByStatus(status: Status, callback: (List<Download>) -> Unit) {
        if (fetch == null) init()
        fetch?.getDownloadsWithStatus(status) { list -> callback(list) } ?: callback(emptyList())
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


    // enqueue helper if needed
    fun enqueue(request: Request, onEnqueued: ((Request) -> Unit)? = null, onError: ((com.tonyodev.fetch2.Error) -> Unit)? = null) {
        if (fetch == null) init()
        fetch?.enqueue(request, { r -> onEnqueued?.invoke(r) }, { e -> onError?.invoke(e) })
    }
}