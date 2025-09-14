package com.acitelight.aether.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.acitelight.aether.model.Video
import com.acitelight.aether.viewModel.VideoScreenViewModel
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.request.ImageRequest
import com.acitelight.aether.Global
import com.acitelight.aether.Global.updateRelate
import java.nio.charset.Charset

fun String.toHex(): String {
    return this.toByteArray().joinToString("") { "%02x".format(it) }
}

fun String.hexToString(charset: Charset = Charsets.UTF_8): String {
    require(length % 2 == 0) { "Hex string must have even length" }

    val bytes = ByteArray(length / 2)
    for (i in bytes.indices) {
        val hexByte = substring(i * 2, i * 2 + 2)
        bytes[i] = hexByte.toInt(16).toByte()
    }
    return String(bytes, charset)
}

@Composable
fun VideoScreen(videoScreenViewModel: VideoScreenViewModel = hiltViewModel<VideoScreenViewModel>(), navController: NavHostController)
{
    val tabIndex by videoScreenViewModel.tabIndex;
    videoScreenViewModel.SetupClient()

    Column(
        modifier = Modifier.fillMaxSize() // 或至少 fillMaxWidth()
    ){
        TopRow(videoScreenViewModel);

        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            if(videoScreenViewModel.classes.isNotEmpty())
            {
                items(videoScreenViewModel.classesMap[videoScreenViewModel.classes[tabIndex]] ?: mutableStateListOf()) { video ->
                    VideoCard(video, navController, videoScreenViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopRow(videoScreenViewModel: VideoScreenViewModel)
{
    val tabIndex by videoScreenViewModel.tabIndex;
    if(videoScreenViewModel.classes.isEmpty()) return
    val colorScheme = MaterialTheme.colorScheme

    ScrollableTabRow (selectedTabIndex = tabIndex, modifier = Modifier.background(colorScheme.surface)) {
        videoScreenViewModel.classes.forEachIndexed { index, title ->
            Tab(
                selected = tabIndex == index,
                onClick = { videoScreenViewModel.setTabIndex(index)  },
                text = { Text(text = title, maxLines = 1) },
            )
        }
    }
}

@Composable
fun VideoCard(video: Video, navController: NavHostController, videoScreenViewModel: VideoScreenViewModel) {
    val tabIndex by videoScreenViewModel.tabIndex;
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    updateRelate(videoScreenViewModel.classesMap[videoScreenViewModel.classes[tabIndex]] ?: mutableStateListOf(), video)
                    val route = "video_player_route/${ "${video.klass}/${video.id}".toHex() }"
                    navController.navigate(route)
                },
                onLongClick = {
                    videoScreenViewModel.download(video)
                    Toast.makeText(videoScreenViewModel.context, "Start downloading ${video.video.name}", Toast.LENGTH_SHORT).show()
                }
            ),
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        )  {
            Box(modifier = Modifier.fillMaxSize()){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.getCover())
                        .memoryCacheKey("${video.klass}/${video.id}/cover")
                        .diskCacheKey("${video.klass}/${video.id}/cover")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    imageLoader = videoScreenViewModel.imageLoader!!
                )

                Text(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                    text = formatTime(video.video.duration), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background( brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f)
                            )
                        ))
                        .align(Alignment.BottomCenter))
            }
            Text(
                text = video.video.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.padding(8.dp).background(Color.Transparent).heightIn(48.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Class", fontSize = 12.sp)
                Text(video.klass, fontSize = 12.sp)
            }
        }
    }
}