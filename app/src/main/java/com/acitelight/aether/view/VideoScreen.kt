package com.acitelight.aether.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.min
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.request.ImageRequest
import com.acitelight.aether.CardPage
import com.acitelight.aether.Global
import com.acitelight.aether.Global.updateRelate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.security.KeyPair
import kotlin.collections.sortedWith

fun videoTOView(v: List<Video>): Map<String?, List<Video>>
{
    return v.map { if(it.video.group != null) it else Video(id=it.id, isLocal = it.isLocal, localBase = it.localBase,
        klass = it.klass, token = it.token, video = it.video.copy(group = it.video.name)) }.groupBy { it.video.group }
}

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
fun VideoScreen(
    videoScreenViewModel: VideoScreenViewModel = hiltViewModel<VideoScreenViewModel>(),
    navController: NavHostController
) {
    val state = rememberLazyStaggeredGridState()
    val colorScheme = MaterialTheme.colorScheme
    val tabIndex by videoScreenViewModel.tabIndex
    var menuVisibility by videoScreenViewModel.menuVisibility
    var searchFilter by videoScreenViewModel.searchFilter
    var doneInit by videoScreenViewModel.doneInit
    val vb = videoTOView(videoScreenViewModel.videoLibrary.classesMap.getOrDefault(
        videoScreenViewModel.videoLibrary.classes.getOrNull(
            tabIndex
        ), listOf()
    ).filter { it.video.name.contains(searchFilter) }).filter { it.key != null }
        .map{ i -> Pair(i.key!!, i.value.sortedWith(compareBy(naturalOrder()) { it.video.name }) ) }
        .toList()

    if (doneInit)
        CardPage(title = "Videos") {
            Box(Modifier.fillMaxSize())
            {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // TopRow(videoScreenViewModel);
                    Row(Modifier.padding(bottom = 4.dp))
                    {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 2.dp)
                                .size(36.dp),
                            onClick = {
                                menuVisibility = !menuVisibility
                            })
                        {
                            Box(Modifier.fillMaxSize())
                            {
                                Icon(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.Center),
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Catalogue"
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 2.dp)
                                .height(36.dp),
                            onClick = {
                                menuVisibility = !menuVisibility
                            })
                        {
                            Box(Modifier.fillMaxHeight())
                            {
                                Text(
                                    text = videoScreenViewModel.videoLibrary.classes.getOrNull(
                                        tabIndex
                                    )
                                        ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(horizontal = 8.dp),
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .height(36.dp)
                                .widthIn(max = 240.dp)
                                .background(colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically),
                                imageVector = Icons.Default.Search,
                                contentDescription = "Catalogue"
                            )
                            Spacer(Modifier.width(4.dp))
                            BasicTextField(
                                value = searchFilter,
                                onValueChange = { searchFilter = it },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Start
                                ),
                                singleLine = true,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    HorizontalDivider(
                        Modifier.padding(bottom = 8.dp),
                        1.5.dp,
                        DividerDefaults.color
                    )
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                            8.dp
                        ),
                        state = state,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = vb,
                            key = { "${it.first}/${it.second}" }
                        ) { video ->
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                if(video.second.isNotEmpty())
                                    VideoCard(video.second, navController, videoScreenViewModel)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = menuVisibility,
                    enter = slideInHorizontally(initialOffsetX = { full -> full }),
                    exit = slideOutHorizontally(targetOffsetX = { full -> full }),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Card(
                        Modifier
                            .fillMaxHeight()
                            .width(250.dp)
                            .align(Alignment.CenterEnd),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                    )
                    {
                        LazyColumn {
                            items(videoScreenViewModel.videoLibrary.classes) { item ->
                                CatalogueItemRow(
                                    item = Pair(
                                        videoScreenViewModel.videoLibrary.classes.indexOf(item),
                                        item
                                    ),
                                    onItemClick = {
                                        menuVisibility = false
                                        videoScreenViewModel.setTabIndex(
                                            videoScreenViewModel.videoLibrary.classes.indexOf(
                                                item
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun CatalogueItemRow(
    item: Pair<Int, String>,
    onItemClick: (Pair<Int, String>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .clickable { onItemClick(item) }
            .padding(4.dp)
            .padding(horizontal = 4.dp)
            .heightIn(min = 28.dp)
            .width(250.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primary)
    ) {
        Text(
            text = item.second,
            fontSize = 14.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VideoCard(
    videos: List<Video>,
    navController: NavHostController,
    videoScreenViewModel: VideoScreenViewModel
) {
    val tabIndex by videoScreenViewModel.tabIndex;
    val video = videos.first()
    Card(
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
                        videoScreenViewModel.download(video)
                    }
                    Toast.makeText(
                        videoScreenViewModel.context,
                        "Start downloading ${video.video.name}",
                        Toast.LENGTH_SHORT
                    ).show()
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
                        .data(video.getCover())
                        .memoryCacheKey("${video.klass}/${video.id}/cover")
                        .diskCacheKey("${video.klass}/${video.id}/cover")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
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

                if (video.isLocal)
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
                maxLines = 4,
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.Transparent)
                    .heightIn(min = 24.dp),
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Class: ", fontSize = 10.sp, maxLines = 1)
                Text(video.klass, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}