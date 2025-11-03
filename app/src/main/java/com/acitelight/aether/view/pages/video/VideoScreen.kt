package com.acitelight.aether.view.pages.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acitelight.aether.model.Video
import com.acitelight.aether.viewModel.video.VideoScreenViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.acitelight.aether.CardPage
import com.acitelight.aether.view.components.video.VideoCard
import java.nio.charset.Charset
import kotlin.collections.sortedWith

fun videoToGroup(v: List<Video>): Map<String?, List<Video>> {
    return v.map {
        if (it.video.group != null) it else Video(
            id = it.id, isLocal = it.isLocal, localBase = it.localBase,
            klass = it.klass, video = it.video.copy(group = it.video.name)
        )
    }.groupBy { it.video.group }
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
    val vb = videoToGroup(
        videoScreenViewModel.videoLibrary.classesMap.getOrDefault(
            videoScreenViewModel.videoLibrary.classes.getOrNull(
                tabIndex
            ), listOf()
        ).filter { it.video.name.contains(searchFilter) }).filter { it.key != null }
        .map { i -> Pair(i.key!!, i.value.sortedWith(compareBy(naturalOrder()) { it.video.name })) }
        .toList()

    if (doneInit)
        CardPage(title = "Videos") {
            Box(Modifier.fillMaxSize())
            {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 4.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    {
                        Text(
                            text = "Videos",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Spacer(Modifier.weight(1f))

                        Row(
                            modifier = Modifier
                                .height(36.dp)
                                .widthIn(max = 240.dp)
                                .background(colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp)
                        )
                        {
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

                    TopRow(videoScreenViewModel)

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp
                        ),
                        state = state,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = vb,
                            key = { "${it.first}/${it.second}" }
                        ) { video ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                if (video.second.isNotEmpty())
                                    VideoCard(video.second, navController, videoScreenViewModel)
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun TopRow(videoScreenViewModel: VideoScreenViewModel) {
    val tabIndex by videoScreenViewModel.tabIndex;
    if (videoScreenViewModel.videoLibrary.classes.isEmpty()) return
    val colorScheme = MaterialTheme.colorScheme

    SecondaryScrollableTabRow(
        selectedTabIndex = tabIndex,
        modifier = Modifier
            .background(Color.Transparent)
            .padding(vertical = 4.dp)
    ) {
        videoScreenViewModel.videoLibrary.classes.forEachIndexed { index, title ->
            Tab(
                modifier = Modifier.height(42.dp),
                selected = tabIndex == index,
                onClick = { videoScreenViewModel.setTabIndex(index) },
                text = {
                    Text(
                        text = title,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp,
                        fontSize = 14.sp
                    )
                },
            )
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
