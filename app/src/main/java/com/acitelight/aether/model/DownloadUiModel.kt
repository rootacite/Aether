package com.acitelight.aether.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tonyodev.fetch2.Status

class DownloadItemState(
    val id: Int,
    fileName: String,
    filePath: String,
    url: String,
    progress: Int,
    status: Status,
    downloadedBytes: Long,
    totalBytes: Long,
    klass: String,
    vid: String
) {
    var fileName by mutableStateOf(fileName)
    var filePath by mutableStateOf(filePath)
    var url by mutableStateOf(url)
    var progress by mutableStateOf(progress)
    var status by mutableStateOf(status)
    var downloadedBytes by mutableStateOf(downloadedBytes)
    var totalBytes by mutableStateOf(totalBytes)

    var klass by mutableStateOf(klass)
    var vid by mutableStateOf(vid)
}