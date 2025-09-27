package com.acitelight.aether.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.FetchManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.VideoLibrary
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransmissionScreenViewModel @Inject constructor(
    val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    val videoLibrary: VideoLibrary,
    val mediaManager: MediaManager,
) : ViewModel() {
    var imageLoader: ImageLoader? = null
    val downloads: SnapshotStateList<VideoDownloadItemState> = mutableStateListOf()

    // map id -> state object reference (no index bookkeeping)
    private val idToState: MutableMap<Int, VideoDownloadItemState> = mutableMapOf()

    fun modelToVideo(model: VideoDownloadItemState): Video? {
        val fv = videoLibrary.classesMap.map { it.value }.flatten()
        return fv.firstOrNull { it.klass == model.klass && it.id == model.vid }
    }

    private val fetchListener = object : FetchListener {
        override fun onAdded(download: Download) {
            handleUpsert(download)
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            handleUpsert(download)
        }

        override fun onWaitingNetwork(download: Download) {

        }

        override fun onProgress(
            download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long
        ) {
            handleUpsert(download)
        }

        override fun onPaused(download: Download) {
            handleUpsert(download)
        }

        override fun onResumed(download: Download) {
            handleUpsert(download)
        }

        override fun onCompleted(download: Download) {
            handleUpsert(download)

            if (download.extras.getString("type", "") == "main") {
                val ii = videoLibrary.classesMap[download.extras.getString(
                    "class",
                    ""
                )]?.indexOfFirst { it.id == download.extras.getString("id", "") }

                if (ii != null) {
                    val newi =
                        videoLibrary.classesMap[download.extras.getString("class", "")]?.get(ii)
                    if (newi != null) videoLibrary.classesMap[download.extras.getString(
                        "class",
                        ""
                    )]?.set(
                        ii, newi.toLocal(context.getExternalFilesDir(null)!!.path)
                    )
                }
            }
        }

        override fun onCancelled(download: Download) {
            handleUpsert(download)
        }

        override fun onRemoved(download: Download) {
            handleRemove(download.id)
        }

        override fun onDeleted(download: Download) {
            handleRemove(download.id)
        }

        override fun onDownloadBlockUpdated(
            download: Download, downloadBlock: DownloadBlock, totalBlocks: Int
        ) {
            handleUpsert(download)
        }

        override fun onStarted(
            download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int
        ) {
            handleUpsert(download)
        }

        override fun onError(
            download: Download, error: com.tonyodev.fetch2.Error, throwable: Throwable?
        ) {
            handleUpsert(download)
        }
    }

    private fun handleUpsert(download: Download) {
        viewModelScope.launch(Dispatchers.Main) {
            upsertOnMain(download)
        }

        val state = downloadToState(download)

        if (videoLibrary.classes.contains(state.klass)) videoLibrary.classes.add(state.klass)

        if (!videoLibrary.classesMap.containsKey(state.klass)) videoLibrary.classesMap[state.klass] =
            mutableStateListOf()

        if (videoLibrary.classesMap[state.klass]?.any { it.id == state.vid } != true) {
            viewModelScope.launch(Dispatchers.IO) {
                val v = mediaManager.queryVideo(state.klass, state.vid, state)
                if (v != null) {
                    videoLibrary.classesMap[state.klass]?.add(v)
                }
            }

        }
    }

    private fun handleRemove(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            removeOnMain(id)
        }
    }

    private fun upsertOnMain(download: Download) {
        val existing = idToState[download.id]
        if (existing != null) {
            // update fields in-place -> minimal recomposition
            existing.filePath = download.file
            existing.fileName = download.request.extras.getString("name", "")
            existing.url = download.url
            existing.progress = download.progress
            existing.status = download.status
            existing.downloadedBytes = download.downloaded
            existing.totalBytes = download.total
        } else {
            // new item: add to head (or tail depending on preference)
            val newState = downloadToState(download)
            downloads.add(0, newState)
            idToState[newState.id] = newState
        }
    }

    private fun removeOnMain(id: Int) {
        val state = idToState.remove(id)
        if (state != null) {
            downloads.remove(state)
        } else {
            val idx = downloads.indexOfFirst { it.id == id }
            if (idx >= 0) {
                val removed = downloads.removeAt(idx)
                idToState.remove(removed.id)
            }
        }
    }

    private fun downloadToState(download: Download): VideoDownloadItemState {
        val filePath = download.file

        return VideoDownloadItemState(
            id = download.id,
            fileName = download.request.extras.getString("name", ""),
            filePath = filePath,
            url = download.url,
            progress = download.progress,
            status = download.status,
            downloadedBytes = download.downloaded,
            totalBytes = download.total,
            klass = download.extras.getString("class", ""),
            vid = download.extras.getString("id", ""),
            type = download.extras.getString("type", "")
        )
    }


    // UI actions delegated to FetchManager
    fun pause(id: Int) = fetchManager.pause(id)
    fun resume(id: Int) = fetchManager.resume(id)
    fun cancel(id: Int) = fetchManager.cancel(id)
    fun delete(id: Int, deleteFile: Boolean = true) {
        fetchManager.delete(id) {
            viewModelScope.launch(Dispatchers.Main) { removeOnMain(id) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchManager.removeListener()
    }

    init {
        imageLoader = ImageLoader.Builder(context).components {
            add(OkHttpNetworkFetcherFactory(createOkHttp()))
        }.build()

        viewModelScope.launch {
            fetchManager.setListener(fetchListener)
            val downloaded = fetchManager.getAllDownloadsAsync()

            downloads.clear()
            idToState.clear()
            downloaded.sortedWith(compareBy(naturalOrder()) { it.extras.getString("name", "") })
                .forEach { d ->
                    val s = downloadToState(d)
                    downloads.add(s)
                    idToState[s.id] = s

                    if (videoLibrary.classes.contains(s.klass)) videoLibrary.classes.add(s.klass)

                    if (!videoLibrary.classesMap.containsKey(s.klass)) videoLibrary.classesMap[s.klass] =
                        mutableStateListOf()

                    if (videoLibrary.classesMap[s.klass]?.any { it.id == s.vid } != true) {
                        val v = mediaManager.queryVideo(s.klass, s.vid, s)
                        if (v != null) {
                            videoLibrary.classesMap[s.klass]?.add(v)
                        }
                    }
                }
        }
    }
}
