package com.acitelight.aether.viewModel.transmission

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.acitelight.aether.Global
import com.acitelight.aether.helper.DownloadType
import com.acitelight.aether.helper.getComicCover
import com.acitelight.aether.helper.getGroup
import com.acitelight.aether.helper.getId
import com.acitelight.aether.helper.getName
import com.acitelight.aether.helper.getType
import com.acitelight.aether.helper.getVideoClass
import com.acitelight.aether.model.ComicDownloadItemState
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.FetchManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.VideoLibrary
import com.acitelight.aether.view.pages.video.toHex
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.DownloadBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TransmissionScreenViewModel @Inject constructor(
    val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    val videoLibrary: VideoLibrary,
    val mediaManager: MediaManager,
    val apiClient: ApiClient
) : ViewModel() {
    val downloadVideos: SnapshotStateList<VideoDownloadItemState> = mutableStateListOf()
    val downloadComics: SnapshotStateList<ComicDownloadItemState> = mutableStateListOf()

    // map id -> state object reference (no index bookkeeping)
    private val idToStateVideos: MutableMap<Int, VideoDownloadItemState> = mutableMapOf()
    private val idToStateComics: MutableMap<Int, ComicDownloadItemState> = mutableMapOf()
    var mutiSelection = mutableStateOf(false)
    var mutiSelectionListVideo = mutableStateListOf<String>()
    var mutiSelectionListComic = mutableStateListOf<Int>()
    var groupExpandMap = mutableStateMapOf<String, Boolean>()

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

            if (download.getType() == DownloadType.VideoMainFile) {
                val klass = download.getVideoClass()
                val index =
                    videoLibrary.classesMap[klass]?.indexOfFirst { it.id == download.getId() }

                if (index != null) {
                    val item = videoLibrary.classesMap[klass]?.get(index)
                    if (item != null) videoLibrary.classesMap[klass]?.set(
                        index, item.toLocal(context.getExternalFilesDir(null)!!.path)
                    )
                }
            }
        }

        override fun onCancelled(download: Download) {
            handleUpsert(download)
        }

        override fun onRemoved(download: Download) {
            handleRemove(download)
        }

        override fun onDeleted(download: Download) {
            handleRemove(download)

            if (download.getType() == DownloadType.VideoMainFile) {
                viewModelScope.launch {
                    val klass = download.getVideoClass()
                    val index =
                        videoLibrary.classesMap[klass]?.indexOfFirst { it.id == download.getId() }

                    if (index != null) {
                        val v = mediaManager.queryVideo(klass, download.getId())
                        if (v != null) {
                            val item = videoLibrary.classesMap[klass]?.get(index)
                            if (item != null) videoLibrary.classesMap[klass]?.set(
                                index, v
                            )
                        }
                    }
                }
            }
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
            download: Download, error: Error, throwable: Throwable?
        ) {
            handleUpsert(download)
        }
    }

    private fun handleUpsert(download: Download) {
        viewModelScope.launch(Dispatchers.Main) {
            upsertOnMain(download)
        }

        if (download.getType() == DownloadType.VideoMainFile) {
            val state = downloadToStateVideo(download)
            if (!videoLibrary.classes.contains(state.klass)) videoLibrary.classes.add(state.klass)
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
    }

    private fun handleRemove(download: Download) {
        viewModelScope.launch(Dispatchers.Main) {
            removeOnMain(download)
        }
    }

    private fun upsertOnMain(download: Download) {
        when (download.getType()) {
            DownloadType.VideoMainFile -> {
                val existing = idToStateVideos[download.id]
                if (existing != null) {
                    // update fields in-place -> minimal recomposition
                    existing.filePath = download.file
                    existing.fileName = download.getName()
                    existing.url = download.url
                    existing.progress = download.progress
                    existing.status = download.status
                    existing.downloadedBytes = download.downloaded
                    existing.totalBytes = download.total
                } else {
                    // new item: add to head (or tail depending on preference)
                    val newState = downloadToStateVideo(download)
                    downloadVideos.add(0, newState)
                    idToStateVideos[newState.id] = newState
                }
            }

            DownloadType.Comic -> {
                val existing = idToStateComics[download.id]
                if (existing != null) {
                    // update fields in-place -> minimal recomposition
                    existing.filePath = download.file
                    existing.fileName = download.getName()
                    existing.url = download.url
                    existing.progress = download.progress
                    existing.status = download.status
                    existing.downloadedBytes = download.downloaded
                    existing.totalBytes = download.total
                } else {
                    // new item: add to head (or tail depending on preference)
                    val newState = downloadToStateComic(download)
                    downloadComics.add(0, newState)
                    idToStateComics[newState.id] = newState
                }
            }

            else -> {}
        }
    }

    private fun removeOnMain(download: Download) {
        when (download.getType()) {
            DownloadType.VideoMainFile -> {
                val state = idToStateVideos.remove(download.id)
                if (state != null) {
                    downloadVideos.remove(state)
                } else {
                    val idx = downloadVideos.indexOfFirst { it.id == download.id }
                    if (idx >= 0) {
                        val removed = downloadVideos.removeAt(idx)
                        idToStateVideos.remove(removed.id)
                    }
                }
            }

            DownloadType.Comic -> {
                val state = idToStateComics.remove(download.id)
                if (state != null) {
                    downloadComics.remove(state)
                } else {
                    val idx = downloadComics.indexOfFirst { it.id == download.id }
                    if (idx >= 0) {
                        val removed = downloadComics.removeAt(idx)
                        idToStateComics.remove(removed.id)
                    }
                }
            }

            else -> {}
        }

    }

    private fun downloadToStateVideo(download: Download): VideoDownloadItemState {
        val filePath = download.file

        return VideoDownloadItemState(
            id = download.id,
            fileName = download.getName(),
            filePath = filePath,
            url = download.url,
            progress = download.progress,
            status = download.status,
            downloadedBytes = download.downloaded,
            totalBytes = download.total,
            klass = download.getVideoClass(),
            vid = download.getId(),
            type = download.getType(),
            group = download.getGroup()
        )
    }

    private fun downloadToStateComic(download: Download): ComicDownloadItemState {
        val filePath = download.file

        return ComicDownloadItemState(
            id = download.id,
            fileName = download.getName(),
            filePath = filePath,
            url = download.url,
            progress = download.progress,
            status = download.status,
            downloadedBytes = download.downloaded,
            totalBytes = download.total,
            cid = download.getId(),
            type = download.getType(),
            cover = download.getComicCover()
        )
    }

    // UI actions delegated to FetchManager
    fun pause(id: Int) = fetchManager.pause(id)
    fun resume(id: Int) = fetchManager.resume(id)
    fun retry(id: Int) = fetchManager.retry(id)
    fun delete(id: Int) = fetchManager.delete(id)

    override fun onCleared() {
        super.onCleared()
        fetchManager.removeListener()
    }

    suspend fun playStart(model: VideoDownloadItemState, navigator: NavHostController) {
        val downloaded = fetchManager.getAllDownloadsAsync().filter {
            it.status == Status.COMPLETED && it.getType() == DownloadType.VideoMainFile
        }

        val jsonQuery = downloaded.map {
            File(
                context.getExternalFilesDir(null),
                "videos/${it.getVideoClass()}/${it.getId()}/summary.json"
            ).readText()
        }.map { Json.Default.decodeFromString<Video>(it).toLocal(context.getExternalFilesDir(null)!!.path) }

        Global.updateRelate(
            jsonQuery,
            jsonQuery.first { it.id == model.vid && it.klass == model.klass }
        )

        val playList = mutableListOf<String>()
        val fv = videoLibrary.classesMap.map { it.value }.flatten()
        val video = fv.firstOrNull { it.klass == model.klass && it.id == model.vid }

        if (video != null) {
            val group =
                fv.filter { it.klass == video.klass && it.video.group == video.video.group && it.video.group != "null" }
            for (i in group.sortedWith(compareBy(naturalOrder()) { it.video.name })) {
                playList.add("${i.klass}/${i.id}")
            }
        }

        val route = "video_player_route/${(playList.joinToString(",") + "|${model.vid}").toHex()}"
        withContext(Dispatchers.Main) {
            navigator.navigate(route)
        }
    }

    init {
        viewModelScope.launch {
            fetchManager.setListener(fetchListener)
            val downloaded = fetchManager.getAllDownloadsAsync()

            downloadVideos.clear()
            downloadComics.clear()
            idToStateVideos.clear()
            downloaded.filter { it.getType() != DownloadType.Comic }.forEach { d ->
                val s = downloadToStateVideo(d)
                downloadVideos.add(s)
                idToStateVideos[s.id] = s

                if (d.getType() == DownloadType.VideoMainFile) {
                    if (!videoLibrary.classes.contains(s.klass))
                        videoLibrary.classes.add(s.klass)

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

            downloaded.filter { it.getType() == DownloadType.Comic }.forEach { d ->
                val s = downloadToStateComic(d)
                downloadComics.add(s)
                idToStateComics[s.id] = s
            }
        }
    }
}