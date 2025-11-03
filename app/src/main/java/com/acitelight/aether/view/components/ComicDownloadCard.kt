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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.helper.getFileNameFromUrl
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicDownloadItemState
import com.acitelight.aether.model.Video
import com.acitelight.aether.view.pages.toHex
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

@Composable
fun ComicDownloadCard(
    navigator: NavHostController,
    viewModel: TransmissionScreenViewModel,
    model: ComicDownloadItemState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
)
{
    var mutiSelection by viewModel.mutiSelection
    val cov = model.cover.getFileNameFromUrl() ?: ""

    val imageModel = if (model.status == Status.COMPLETED)
    {
        val f = File(viewModel.context.getExternalFilesDir(null), "comics/${model.cid}.zip")
        ImageRequest.Builder(LocalContext.current)
            .data("file://${f.path}?entry=$cov")
            .memoryCacheKey("${model.cid}/cover")
            .diskCacheKey("${model.cid}/cover")
            .build()
    } else
    {
        ImageRequest.Builder(LocalContext.current)
            .data(Comic.getCoverStatic(viewModel.apiClient, model.cid, cov))
            .memoryCacheKey("${model.cid}/cover")
            .diskCacheKey("${model.cid}/cover")
            .build()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.65f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    if (!mutiSelection)
                        when (model.status) {
                            Status.COMPLETED ->
                            {
                                val route = "comic_grid_route/${model.cid.toHex()}"
                                navigator.navigate(route)
                            }

                            Status.DOWNLOADING -> onPause()
                            Status.PAUSED -> onResume()
                            Status.ADDED, Status.FAILED, Status.CANCELLED -> onRetry()
                            else -> {}
                        }
                    else {
                        if (model.id !in viewModel.mutiSelectionListComic)
                            viewModel.mutiSelectionListComic.add(model.id)
                        else
                            viewModel.mutiSelectionListComic.remove(model.id)
                    }
                },
                onLongClick = {
                    mutiSelection = !mutiSelection
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    imageLoader = viewModel.apiClient.getImageLoader(),
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = mutiSelection,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ){
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Transparent,
                                    )
                                )
                            )
                            .align(Alignment.TopCenter)
                    ){
                        Checkbox(
                            modifier = Modifier.align(Alignment.TopEnd).size(28.dp),
                            checked = model.id in viewModel.mutiSelectionListComic,
                            onCheckedChange = { state ->
                                if (state)
                                    viewModel.mutiSelectionListComic.add(model.id)
                                else
                                    viewModel.mutiSelectionListComic.remove(model.id)
                            }
                        )
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
                {
                    BiliMiniSlider(
                        value = abs(model.progress).coerceIn(0, 100) / 100f,
                        modifier = Modifier
                            .height(6.dp)
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        onValueChange = {

                        }
                    )
                }
            }
            Text(
                text = model.fileName,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier
                    .padding(4.dp)
                    .heightIn(min = 14.dp)
            )
            Text(
                text = "Id: ${model.cid}",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}