package com.acitelight.aether.view.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.acitelight.aether.Global.updateRelate
import com.acitelight.aether.model.Video
import com.acitelight.aether.view.pages.formatTime
import com.acitelight.aether.view.pages.toHex
import com.acitelight.aether.viewModel.VideoScreenViewModel
import kotlinx.coroutines.launch


@Composable
fun VideoCard(
    videos: List<Video>,
    navController: NavHostController,
    videoScreenViewModel: VideoScreenViewModel
) {
    val tabIndex by videoScreenViewModel.tabIndex;
    val video = videos.first()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    updateRelate(
                        videoScreenViewModel.videoLibrary.classesMap[videoScreenViewModel.videoLibrary.classes[tabIndex]]
                            ?: mutableStateListOf(), video
                    )
                    val vg = videos.joinToString(",") { "${it.klass}/${it.id}" }.toHex()
                    val route = "video_player_route/$vg"
                    navController.navigate(route)
                },
                onLongClick = {
                    videoScreenViewModel.viewModelScope.launch {
                        for(i in videos)
                        {
                            videoScreenViewModel.download(i)
                        }
                        Toast.makeText(
                            videoScreenViewModel.context,
                            "Start downloading ${video.video.group}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            ),
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.getCover(videoScreenViewModel.apiClient))
                        .memoryCacheKey("${video.klass}/${video.id}/cover")
                        .diskCacheKey("${video.klass}/${video.id}/cover")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    imageLoader = videoScreenViewModel.imageLoader!!
                )


                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal =  2.dp),
                    text = "${videos.size} Videos",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 13.sp,
                    color = Color.White
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal =  2.dp),
                    text = formatTime(video.video.duration),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 13.sp,
                    color = Color.White
                )

                if (videos.all{ it.isLocal })
                    Card(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                            .widthIn(max = 46.dp)
                    ) {
                        Box(Modifier.fillMaxWidth())
                        {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "Local",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
            }
            Text(
                text = video.video.group ?: video.video.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color.Transparent)
                    .heightIn(min = 24.dp),
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth()
            ) {
                Text(modifier = Modifier.align(Alignment.CenterStart), text = "Class: ${video.klass}", fontSize = 10.sp, maxLines = 1)
                Text(modifier = Modifier.align(Alignment.CenterEnd), text = "Id: ${
                    videos.take(5).joinToString(
                        ","
                    ) { it.id }
                }", fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}