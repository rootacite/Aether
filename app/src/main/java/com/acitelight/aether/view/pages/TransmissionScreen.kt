package com.acitelight.aether.view.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.view.components.BiliMiniSlider
import com.acitelight.aether.view.components.VideoDownloadCardMini
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import java.io.File
import kotlin.collections.sortedWith

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
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Start)
        )

        Text(
            text = "All: ${downloads.count { it.type == "main" }}",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Start),
            fontSize = 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )

        Text(
            text = "Completed: ${downloads.count { it.type == "main" && it.status == Status.COMPLETED }}",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Start),
            fontSize = 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )

        val downloading = downloads.filter { it.status == Status.DOWNLOADING }
        BiliMiniSlider(
            value = if (downloading.sumOf { it.totalBytes } == 0L) 1f else downloading.sumOf { it.downloadedBytes } / downloading.sumOf { it.totalBytes }
                .toFloat(),
            modifier = Modifier
                .height(6.dp)
                .align(Alignment.End)
                .fillMaxWidth(),
            onValueChange = {

            }
        )

        HorizontalDivider(Modifier.padding(8.dp), 2.dp, DividerDefaults.color)

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        )
        {
            items(
                downloads
                .filter { it.type == "main" }
                .sortedBy { it.status == Status.COMPLETED }, key = { it.id })
            { item ->
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

                        File(
                            transmissionScreenViewModel.context.getExternalFilesDir(null),
                            "videos/${item.klass}/${item.vid}/summary.json"
                        ).delete()
                    },
                    onRetry = {
                        for (i in downloadToGroup(
                            item,
                            downloads
                        )) transmissionScreenViewModel.retry(i.id)
                    }
                )
                HorizontalDivider(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    2.dp,
                    DividerDefaults.color
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
