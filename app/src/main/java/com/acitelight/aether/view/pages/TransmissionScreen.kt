package com.acitelight.aether.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.view.components.BiliMiniSlider
import com.acitelight.aether.view.components.VideoDownloadCard
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import java.io.File
import kotlin.comparisons.compareBy
import kotlin.comparisons.naturalOrder

@Composable
fun TransmissionScreen(
    navigator: NavHostController,
    transmissionScreenViewModel: TransmissionScreenViewModel = hiltViewModel<TransmissionScreenViewModel>()
) {
    var mutiSelection by transmissionScreenViewModel.mutiSelection
    val downloads = transmissionScreenViewModel.downloads

    Column(modifier = Modifier.animateContentSize())
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

        AnimatedVisibility(
            visible = mutiSelection,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ){
            Column {
                Button(onClick = {
                    mutiSelection = false
                    transmissionScreenViewModel.mutiSelectionList.forEach {
                        item ->
                        val klass = item.split("/").first()
                        val id = item.split("/").last()
                        for (i in downloadToGroup(
                            klass, id,
                            downloads
                        )) transmissionScreenViewModel.delete(i.id)

                        File(
                            transmissionScreenViewModel.context.getExternalFilesDir(null),
                            "videos/${klass}/${id}/summary.json"
                        ).delete()
                    }
                }, modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp))
                {
                    Text(text = "Delete", fontWeight = FontWeight.Bold)
                }
            }

        }

        HorizontalDivider(Modifier.padding(8.dp), 2.dp, DividerDefaults.color)

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        )
        {
            items(
                items = downloads
                .filter { it.type == "main" }
                .sortedBy { it.status == Status.COMPLETED }
                .groupBy { it.group }.map { it.value }
                , key = { it.first().id }
            )
            { item ->
                VideoDownloadCard(
                    navigator = navigator,
                    viewModel = transmissionScreenViewModel,
                    models = item.sortedWith(compareBy(naturalOrder()) { it.fileName })
                )
                HorizontalDivider(
                    Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
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

fun downloadToGroup(
    klass: String, id: String,
    downloads: List<VideoDownloadItemState>
): List<VideoDownloadItemState> {
    return downloads.filter { it.vid == id && it.klass == klass }
}