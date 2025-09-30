package com.acitelight.aether.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.viewModel.VideoPlayerViewModel
import kotlinx.coroutines.launch


@Composable
fun PlaylistPanel(modifier: Modifier, videoPlayerViewModel: VideoPlayerViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val name by videoPlayerViewModel.currentName
    val id by videoPlayerViewModel.currentId

    val listState = rememberLazyListState()
    val videos = videoPlayerViewModel.videos

    LaunchedEffect(id, videos) {
        val targetIndex = videos.indexOfFirst { it.id == id }
        if (targetIndex >= 0) {
            listState.scrollToItem(targetIndex)
        }
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(videos) { it ->
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(140.dp),
                onClick = {
                    if (name == it.video.name)
                        return@Card

                    videoPlayerViewModel.viewModelScope.launch {
                        videoPlayerViewModel.startPlay(it)
                    }
                },
                colors =
                    if (it.id == id)
                        CardDefaults.cardColors(containerColor = colorScheme.primary)
                    else
                        CardDefaults.cardColors()
            ) {
                Box(
                    Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = it.video.name,
                        maxLines = 4,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
