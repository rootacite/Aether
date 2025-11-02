package com.acitelight.aether.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.acitelight.aether.helper.DownloadType
import com.tonyodev.fetch2.Status

class ComicDownloadItemState(
    val id: Int,
    fileName: String,
    filePath: String,
    url: String,
    progress: Int,
    status: Status,
    downloadedBytes: Long,
    totalBytes: Long,
    val cid: String,
    val type: DownloadType,
    val cover: String
) {
    var fileName by mutableStateOf(fileName)
    var filePath by mutableStateOf(filePath)
    var url by mutableStateOf(url)
    var progress by mutableIntStateOf(progress)
    var status by mutableStateOf(status)
    var downloadedBytes by mutableLongStateOf(downloadedBytes)
    var totalBytes by mutableLongStateOf(totalBytes)
}