package com.acitelight.aether.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.model.DownloadItemState
import com.acitelight.aether.service.FetchManager
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TransmissionScreenViewModel @Inject constructor(
    private val fetchManager: FetchManager
) : ViewModel() {
    private val _downloads: SnapshotStateList<DownloadItemState> = mutableStateListOf()
    val downloads: SnapshotStateList<DownloadItemState> = _downloads

    // map id -> state object reference (no index bookkeeping)
    private val idToState: MutableMap<Int, DownloadItemState> = mutableMapOf()

    private val fetchListener = object : FetchListener {
        override fun onAdded(download: Download) { handleUpsert(download) }
        override fun onQueued(download: Download, waitingOnNetwork: Boolean) { handleUpsert(download) }
        override fun onWaitingNetwork(download: Download) {

        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) { handleUpsert(download) }
        override fun onPaused(download: Download) { handleUpsert(download) }
        override fun onResumed(download: Download) { handleUpsert(download) }
        override fun onCompleted(download: Download) { handleUpsert(download) }
        override fun onCancelled(download: Download) { handleUpsert(download) }
        override fun onRemoved(download: Download) { handleRemove(download.id) }
        override fun onDeleted(download: Download) { handleRemove(download.id) }
        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, blockCount: Int) { handleUpsert(download) }
        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            handleUpsert(download)
        }

        override fun onError(download: Download, error: com.tonyodev.fetch2.Error, throwable: Throwable?) { handleUpsert(download) }
    }

    private fun handleUpsert(download: Download) {
        viewModelScope.launch(Dispatchers.Main) {
            upsertOnMain(download)
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
            _downloads.add(0, newState)
            idToState[newState.id] = newState
        }
    }

    private fun removeOnMain(id: Int) {
        val state = idToState.remove(id)
        if (state != null) {
            _downloads.remove(state)
        } else {
            val idx = _downloads.indexOfFirst { it.id == id }
            if (idx >= 0) {
                val removed = _downloads.removeAt(idx)
                idToState.remove(removed.id)
            }
        }
    }
    private fun downloadToState(download: Download): DownloadItemState {
        val filePath = download.file

        return DownloadItemState(
            id = download.id,
            fileName = download.request.extras.getString("name", ""),
            filePath = filePath,
            url = download.url,
            progress = download.progress,
            status = download.status,
            downloadedBytes = download.downloaded,
            totalBytes = download.total
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
        fetchManager.setListener(fetchListener)
        viewModelScope.launch(Dispatchers.Main) {
            fetchManager.getAllDownloads { list ->
                _downloads.clear()
                idToState.clear()
                list.sortedByDescending { it.id }.forEach { d ->
                    val s = downloadToState(d)
                    _downloads.add(s)
                    idToState[s.id] = s
                }
            }
        }
    }
}