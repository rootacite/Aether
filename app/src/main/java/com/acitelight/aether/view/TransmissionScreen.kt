package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.navigation.Navigator
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.Global.updateRelate
import com.acitelight.aether.model.DownloadItemState
import com.acitelight.aether.model.Video
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

@Composable
fun TransmissionScreen(navigator: NavHostController, transmissionScreenViewModel: TransmissionScreenViewModel = hiltViewModel<TransmissionScreenViewModel>()) {
    val downloads = transmissionScreenViewModel.downloads
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(downloads, key = { it.id }) { item ->
            DownloadCard(
                navigator = navigator,
                viewModel = transmissionScreenViewModel,
                model = item,
                onPause = { transmissionScreenViewModel.pause(item.id) },
                onResume = { transmissionScreenViewModel.resume(item.id) },
                onCancel = { transmissionScreenViewModel.cancel(item.id) },
                onDelete = { transmissionScreenViewModel.delete(item.id, true) }
            )
        }
    }
}


@Composable
private fun DownloadCard(
    navigator: NavHostController,
    viewModel: TransmissionScreenViewModel,
    model: DownloadItemState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Transparent)
            .clickable(onClick = {
                if(model.status == Status.COMPLETED)
                {
                    viewModel.viewModelScope.launch(Dispatchers.IO)
                    {
                        val downloaded = viewModel.fetchManager.getAllDownloadsAsync().filter {
                            it.status == Status.COMPLETED && it.extras.getString("isComic", "") != "true"
                        }

                        val jsonQuery = downloaded.map{ File(
                            viewModel.context.getExternalFilesDir(null),
                            "videos/${it.extras.getString("class", "")}/${it.extras.getString("id", "")}/summary.json").readText() }
                            .map {  Json.decodeFromString<Video>(it).toLocal(viewModel.context.getExternalFilesDir(null)!!.path) }

                        updateRelate(
                            jsonQuery, jsonQuery.first { it.id == model.vid && it.klass == model.klass }
                        )
                        val route = "video_player_route/${"${model.klass}/${model.vid}".toHex()}"
                        withContext(Dispatchers.Main){
                            navigator.navigate(route)
                        }
                    }
                }
            })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = model.fileName, style = MaterialTheme.typography.titleMedium)
                }

            }

            Box(Modifier
                .fillMaxWidth()
                .padding(top = 5.dp))
            {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                File(
                                    viewModel.context.getExternalFilesDir(null),
                                    "videos/${model.klass}/${model.vid}/cover.jpg"
                                )
                            )
                            .diskCacheKey("${model.klass}/${model.vid}/cover")
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.heightIn(max = 100.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(Modifier.align(Alignment.BottomEnd)) {
                    Text(
                        text = "${model.progress}%",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.End)
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.End),
                        text = "%.2f MB/%.2f MB".format(
                            model.downloadedBytes / (1024.0 * 1024.0),
                            model.totalBytes / (1024.0 * 1024.0)
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }


            // progress bar
            LinearProgressIndicator(
                progress = { model.progress.coerceIn(0, 100) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            // action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (model.status) {
                    Status.DOWNLOADING -> {
                        Button(onClick = onPause) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                            Text(text = " Pause", modifier = Modifier.padding(start = 6.dp))
                        }
                        Button(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Cancel")
                            Text(text = " Cancel", modifier = Modifier.padding(start = 6.dp))
                        }
                    }

                    Status.PAUSED, Status.QUEUED -> {
                        Button(onClick = onResume) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume"
                            )
                            Text(text = " Resume", modifier = Modifier.padding(start = 6.dp))
                        }
                        Button(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Cancel")
                            Text(text = " Cancel", modifier = Modifier.padding(start = 6.dp))
                        }
                    }

                    Status.COMPLETED -> {
                        Button(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            Text(text = " Delete", modifier = Modifier.padding(start = 6.dp))
                        }
                    }

                    else -> {
                        // for FAILED, CANCELLED, REMOVED etc.
                        Button(onClick = onResume) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Retry"
                            )
                            Text(text = " Retry", modifier = Modifier.padding(start = 6.dp))
                        }
                        Button(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            Text(text = " Delete", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }
    }
}
