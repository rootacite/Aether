package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.Global.updateRelate
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.model.Video
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.sortedWith
import kotlin.math.abs

@Composable
fun TransmissionScreen(
    navigator: NavHostController,
    transmissionScreenViewModel: TransmissionScreenViewModel = hiltViewModel<TransmissionScreenViewModel>()
) {
    val downloads = transmissionScreenViewModel.downloads
    Column()
    {
        Text(
            text = "Video Tasks",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp).align(Alignment.Start)
        )

        Text(
            text = "All: ${downloads.count { it.type == "main" }}",
            modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.Start),
            fontSize = 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )

        Text(
            text = "Completed: ${downloads.count { it.type == "main" && it.status == Status.COMPLETED }}",
            modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.Start),
            fontSize = 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )

        val downloading = downloads.filter { it.status == Status.DOWNLOADING }
        BiliMiniSlider(
            value = if (downloading.sumOf { it.totalBytes } == 0L) 1f else downloading.sumOf { it.downloadedBytes } / downloading.sumOf { it.totalBytes }.toFloat(),
            modifier = Modifier
                .height(6.dp)
                .align(Alignment.End)
                .fillMaxWidth(),
            onValueChange = {

            }
        )

        HorizontalDivider(Modifier.padding(8.dp), 2.dp, DividerDefaults.color)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            items(downloads.filter { it.type == "main" }.sortedBy { it.status == Status.COMPLETED }, key = { it.id }) { item ->
                VideoDownloadCardMini(
                    navigator = navigator,
                    viewModel = transmissionScreenViewModel,
                    model = item,
                    onPause = {
                        for (i in downloadToGroup(
                            item,
                            downloads
                        )) transmissionScreenViewModel.pause(i.id)
                    },
                    onResume = {
                        for (i in downloadToGroup(
                            item,
                            downloads
                        )) transmissionScreenViewModel.resume(i.id)
                    },
                    onCancel = {
                        for (i in downloadToGroup(
                            item,
                            downloads
                        )) transmissionScreenViewModel.delete(i.id)
                    },
                    onDelete = {
                        for (i in downloadToGroup(
                            item,
                            downloads
                        )) transmissionScreenViewModel.delete(i.id)
                    },
                    onRetry = {
                        for (i in downloadToGroup(
                            item,
                            downloads
                        )) transmissionScreenViewModel.retry(i.id)
                    }
                )
            }
        }
    }
}

fun downloadToGroup(
    i: VideoDownloadItemState,
    downloads: List<VideoDownloadItemState>
): List<VideoDownloadItemState> {
    return downloads.filter { it.vid == i.vid && it.klass == i.klass }
}
