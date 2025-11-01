package com.acitelight.aether.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

@Composable
fun VideoDownloadCardMini(
    navigator: NavHostController,
    viewModel: TransmissionScreenViewModel,
    model: VideoDownloadItemState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    imageHeight: Dp = 85.dp,
    imageMaxWidth: Dp = 140.dp
) {
    var mutiSelection by viewModel.mutiSelection
    val imageModel = if (model.status == Status.COMPLETED)
    {
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
    } else
    {
        ImageRequest.Builder(LocalContext.current)
            .data(Video.getCoverStatic(viewModel.apiClient, model.klass, model.vid))
            .memoryCacheKey("${model.klass}/${model.vid}/cover")
            .diskCacheKey("${model.klass}/${model.vid}/cover")
            .build()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(Color.Transparent)
            .combinedClickable(
                onClick = {
                    if (!mutiSelection)
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
                    else {
                        if ("${model.klass}/${model.vid}" !in viewModel.mutiSelectionList)
                            viewModel.mutiSelectionList.add("${model.klass}/${model.vid}")
                        else
                            viewModel.mutiSelectionList.remove("${model.klass}/${model.vid}")
                    }
                },
                onLongClick = {
                    mutiSelection = !mutiSelection
                }
            )
            .height(imageHeight)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        )
        {
            Row(
                Modifier.fillMaxHeight()
            )
            {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    visible = mutiSelection,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                )
                {
                    Checkbox(
                        checked = "${model.klass}/${model.vid}" in viewModel.mutiSelectionList,
                        onCheckedChange = { state ->
                            if (state)
                                viewModel.mutiSelectionList.add("${model.klass}/${model.vid}")
                            else
                                viewModel.mutiSelectionList.remove("${model.klass}/${model.vid}")
                        }
                    )
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .height(imageHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .widthIn(max = imageMaxWidth)
                        .background(Color.Black),
                    contentScale = ContentScale.Crop,
                    imageLoader = viewModel.imageLoader!!
                )
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

                    Row(
                        Modifier
                            .align(Alignment.End)
                            .padding(vertical = 2.dp)
                    ) {
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
                        colors = when (model.status) {
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