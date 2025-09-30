package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.Global.updateRelate
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.abs

@Composable
fun VideoDownloadCardMini(
    navigator: NavHostController,
    viewModel: TransmissionScreenViewModel,
    model: VideoDownloadItemState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    val video = viewModel.modelToVideo(model)
    val imageModel =
        if (video == null)
            ImageRequest.Builder(LocalContext.current)
                .data(
                    File(
                        viewModel.context.getExternalFilesDir(null),
                        "videos/${model.klass}/${model.vid}/cover.jpg"
                    )
                )
                .memoryCacheKey("${model.klass}/${model.vid}/cover")
                .diskCacheKey("${model.klass}/${model.vid}/cover")
                .build()
        else
            ImageRequest.Builder(LocalContext.current)
                .data(video.getCover(viewModel.apiClient))
                .memoryCacheKey("${model.klass}/${model.vid}/cover")
                .diskCacheKey("${model.klass}/${model.vid}/cover")
                .build()

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(Color.Transparent)
            .clickable(onClick = {
                when (model.status) {
                    Status.COMPLETED -> viewModel.viewModelScope.launch(Dispatchers.IO)
                    {
                        viewModel.playStart(model, navigator)
                    }
                    Status.DOWNLOADING -> onPause()
                    Status.PAUSED -> onResume()
                    Status.ADDED, Status.FAILED, Status.CANCELLED -> onRetry()
                    else -> {}
                }
            })
            .height(85.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        )
        {
            Box(Modifier
                .fillMaxHeight()
                .widthIn(max = 152.dp))
            {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onDelete,
                    Modifier
                        .padding(2.dp)
                        .size(24.dp)
                        .align(Alignment.TopStart)
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(all = 4.dp)
                    .padding(end = 4.dp)
            )
            {
                Text(
                    text = model.fileName,
                    lineHeight = 14.sp,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Column(Modifier.align(Alignment.BottomEnd)) {
                    Text(
                        modifier = Modifier.align(Alignment.End),
                        text = when (model.status) {
                            Status.COMPLETED -> "Completed"
                            Status.PAUSED, Status.QUEUED -> "Paused"
                            Status.DOWNLOADING -> "Downloading"
                            else -> "Error"
                        },
                        fontSize = 10.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )

                    Row(Modifier
                        .align(Alignment.End)
                        .padding(vertical = 2.dp)) {
                        Text(
                            modifier = Modifier,
                            text = "%.2f MB/%.2f MB".format(
                                model.downloadedBytes / (1024.0 * 1024.0),
                                model.totalBytes / (1024.0 * 1024.0)
                            ),
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "${model.progress.coerceIn(0, 100)}%",
                            modifier = Modifier,
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                    BiliMiniSlider(
                        value = abs(model.progress).coerceIn(0, 100) / 100f,
                        modifier = Modifier
                            .height(6.dp)
                            .align(Alignment.End)
                            .fillMaxWidth(),
                        onValueChange = {

                        },
                        colors = when(model.status)
                        {
                            Status.DOWNLOADING, Status.QUEUED, Status.ADDED -> SliderDefaults.colors(
                                thumbColor = Color(0xFFFFFFFF),
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                            Status.PAUSED -> SliderDefaults.colors(
                                thumbColor = Color(0xFFFFFFFF),
                                activeTrackColor = Color(0xFFFFA500),
                                inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                            Status.COMPLETED -> SliderDefaults.colors(
                                thumbColor = Color(0xFFFFFFFF),
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                            else -> SliderDefaults.colors(
                                thumbColor = Color(0xFFFFFFFF),
                                activeTrackColor = MaterialTheme.colorScheme.error,
                                inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                        }
                    )
                }
            }
        }
    }
}