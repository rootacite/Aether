package com.acitelight.aether.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.view.pages.downloadToGroup
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import java.io.File
import kotlin.math.abs

@Composable
private fun VideoDownloadCardMiniPack(
    navigator: NavHostController,
    viewModel: TransmissionScreenViewModel,
    model: VideoDownloadItemState,
    imageHeight: Dp = 85.dp,
    imageMaxWidth: Dp = 140.dp
) {
    val downloads = viewModel.downloads
    VideoDownloadCardMini(
        navigator = navigator,
        viewModel = viewModel,
        model = model,
        onPause = {
            for (i in downloadToGroup(
                model,
                downloads
            )) viewModel.pause(i.id)
        },
        onResume = {
            for (i in downloadToGroup(
                model,
                downloads
            )) viewModel.resume(i.id)
        },
        onRetry = {
            for (i in downloadToGroup(
                model,
                downloads
            )) viewModel.retry(i.id)
        },
        imageHeight,
        imageMaxWidth
    )
}

@Composable
fun VideoDownloadCard(
    navigator: NavHostController,
    viewModel: TransmissionScreenViewModel,
    models: List<VideoDownloadItemState>
) {
    val item = models.first()
    var mutiSelection by viewModel.mutiSelection
    val colorScheme = MaterialTheme.colorScheme

    if (models.size == 1) {
        VideoDownloadCardMiniPack(
            navigator = navigator,
            viewModel = viewModel,
            model = item
        )
    } else if (models.size > 1) {
        val imageModel = if (item.status == Status.COMPLETED) {
            ImageRequest.Builder(LocalContext.current)
                .data(
                    File(
                        viewModel.context.getExternalFilesDir(null),
                        "videos/${item.klass}/${item.vid}/cover.jpg"
                    )
                )
                .memoryCacheKey("${item.klass}/${item.vid}/cover")
                .diskCacheKey("${item.klass}/${item.vid}/cover")
                .build()
        } else {
            ImageRequest.Builder(LocalContext.current)
                .data(Video.getCoverStatic(viewModel.apiClient, item.klass, item.vid))
                .memoryCacheKey("${item.klass}/${item.vid}/cover")
                .diskCacheKey("${item.klass}/${item.vid}/cover")
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
                            viewModel.groupExpandMap[item.group] = !viewModel.groupExpandMap.getOrDefault(item.group, false)
                        else {
                            if(!models.all { "${it.klass}/${it.vid}" in viewModel.mutiSelectionList })
                                viewModel.mutiSelectionList.addAll(
                                    models.map { "${it.klass}/${it.vid}" }.filter { it !in viewModel.mutiSelectionList }
                                )
                            else
                                viewModel.mutiSelectionList.removeAll(models.map { "${it.klass}/${it.vid}" })
                        }
                    },
                    onLongClick = {
                        mutiSelection = !mutiSelection
                    }
                )
                .height(85.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            )
            {
                Box(
                    Modifier
                        .fillMaxHeight()
                )
                {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier
                            .height(85.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .widthIn(max = 140.dp)
                            .background(Color.Black),
                        contentScale = ContentScale.Crop
                    )

                    Box(Modifier
                        .padding(4.dp)
                        .background(Color.Black.copy(0.4f), shape = RoundedCornerShape(6.dp))
                        .align(Alignment.BottomEnd)
                        .padding(2.dp))
                    {
                        Text(text = "${models.size} Videos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 13.5.sp)
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
                        text = models.first().group,
                        lineHeight = 14.sp,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    Column(Modifier.align(Alignment.BottomEnd)) {

                        Row(
                            Modifier
                                .align(Alignment.End)
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "%.2f MB/%.2f MB".format(
                                    models.sumOf { it.downloadedBytes } / (1024.0 * 1024.0),
                                    models.sumOf { it.totalBytes } / (1024.0 * 1024.0)
                                ),
                                fontSize = 10.sp,
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "${
                                    models.map { it.progress }.average().toInt().coerceIn(0, 100)
                                }%",
                                modifier = Modifier,
                                fontSize = 10.sp,
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                        }
                        BiliMiniSlider(
                            value = abs(models.map { it.progress }.average().toInt()).coerceIn(
                                0,
                                100
                            ) / 100f,
                            modifier = Modifier
                                .height(6.dp)
                                .align(Alignment.End)
                                .fillMaxWidth(),
                            onValueChange = {

                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFFFFF),
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = viewModel.groupExpandMap.getOrDefault(item.group, false) || mutiSelection,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        )
        {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                HorizontalDivider(
                    Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
                    2.dp,
                    DividerDefaults.color
                )
                LazyColumn(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .background(colorScheme.surface, shape = RoundedCornerShape(6.dp))
                )
                {
                    items(
                        items = models,
                        key = { it.id }
                    ) { single ->
                        Box(Modifier.padding(vertical = 4.dp))
                        {
                            VideoDownloadCardMiniPack(
                                navigator = navigator,
                                viewModel = viewModel,
                                model = single,
                                imageHeight = 75.dp,
                                imageMaxWidth = 120.dp
                            )
                        }
                    }
                }
            }
        }
    }
}