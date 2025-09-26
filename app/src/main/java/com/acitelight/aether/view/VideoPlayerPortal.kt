package com.acitelight.aether.view

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.acitelight.aether.Global
import com.acitelight.aether.ToggleFullScreen
import com.acitelight.aether.viewModel.VideoPlayerViewModel


@Composable
fun VideoPlayerPortal(
    videoPlayerViewModel: VideoPlayerViewModel,
    navController: NavHostController
) {
    val colorScheme = MaterialTheme.colorScheme
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val minHeight = 42.dp
    var coverAlpha by remember { mutableFloatStateOf(0.0f) }
    var maxHeight = remember { screenHeight * 0.65f }
    var posed = remember { false }
    val dens = LocalDensity.current
    val listState = rememberLazyListState()

    var playerHeight by remember { mutableStateOf(screenHeight * 0.65f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y // px
                val deltaDp = with(dens) { deltaY.toDp() }

                val r = if (deltaY < 0 && playerHeight > minHeight) {
                    val newHeight = (playerHeight + deltaDp).coerceIn(minHeight, maxHeight)
                    val consumedDp = newHeight - playerHeight
                    playerHeight = newHeight
                    val consumedPx = with(dens) { consumedDp.toPx() }
                    Offset(0f, consumedPx)
                } else if (deltaY > 0 && playerHeight < maxHeight && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    val newHeight = (playerHeight + deltaDp).coerceIn(minHeight, maxHeight)
                    val consumedDp = newHeight - playerHeight
                    playerHeight = newHeight
                    val consumedPx = with(dens) { consumedDp.toPx() }
                    Offset(0f, consumedPx)
                } else {
                    Offset.Zero
                }

                val dh = playerHeight - minHeight
                coverAlpha = (if (dh > 10.dp)
                    0f
                else
                    (10.dp.value - dh.value) / 10.0f)

                return r
            }
        }
    }

    val klass by videoPlayerViewModel.currentKlass
    val id by videoPlayerViewModel.currentId
    val name by videoPlayerViewModel.currentName
    val duration by videoPlayerViewModel.currentDuration

    ToggleFullScreen(false)
    Column(Modifier
        .nestedScroll(nestedScrollConnection)
        .fillMaxHeight())
    {
        Box {
            PortalCorePlayer(
                Modifier
                    .padding(top = 32.dp)
                    .heightIn(max = playerHeight)
                    .onGloballyPositioned { layoutCoordinates ->
                        if (!posed && videoPlayerViewModel.renderedFirst) {
                            maxHeight = with(dens) { layoutCoordinates.size.height.toDp() }
                            playerHeight = maxHeight
                            posed = true
                        }
                    },
                videoPlayerViewModel = videoPlayerViewModel, coverAlpha
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = videoPlayerViewModel.locked || videoPlayerViewModel.planeVisibility,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Card(
                    modifier = Modifier.padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primary.copy(
                            if (videoPlayerViewModel.locked) 0.2f else 1f
                        )
                    ),
                    onClick = {
                        videoPlayerViewModel.locked = !videoPlayerViewModel.locked
                    }) {
                    Icon(
                        imageVector = if (videoPlayerViewModel.locked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color.White.copy(if (videoPlayerViewModel.locked) 0.2f else 1f),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(6.dp)
                    )
                }
            }
        }

        Row()
        {
            TabRow(
                selectedTabIndex = videoPlayerViewModel.tabIndex,
                modifier = Modifier.height(38.dp)
            ) {
                Tab(
                    selected = videoPlayerViewModel.tabIndex == 0,
                    onClick = { videoPlayerViewModel.tabIndex = 0 },
                    text = { Text(text = "Introduction", maxLines = 1) },
                    modifier = Modifier.height(38.dp)
                )

                Tab(
                    selected = videoPlayerViewModel.tabIndex == 1,
                    onClick = { videoPlayerViewModel.tabIndex = 1 },
                    text = { Text(text = "Comment", maxLines = 1) },
                    modifier = Modifier.height(38.dp)
                )
            }
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            item {
                Text(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp),
                    text = name,
                    fontSize = 16.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                )

                Row(Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 4.dp)
                    .alpha(0.5f)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = klass,
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = formatTime(duration),
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                    )
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, DividerDefaults.color)

                PlaylistPanel(
                    Modifier,
                    videoPlayerViewModel = videoPlayerViewModel
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, DividerDefaults.color)

                HorizontalGallery(videoPlayerViewModel)
                HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, DividerDefaults.color)

                for (i in Global.sameClassVideos ?: listOf()) {
                    if (i.id == id) continue

                    MiniVideoCard(
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        i,
                        {
                            videoPlayerViewModel.isPlaying = false
                            videoPlayerViewModel.player?.pause()
                            val route = "video_player_route/${"${i.klass}/${i.id}".toHex()}"
                            navController.navigate(route)
                        }, videoPlayerViewModel.imageLoader!!
                    )
                    HorizontalDivider(
                        Modifier
                            .padding(vertical = 8.dp)
                            .alpha(0.25f),
                        1.dp,
                        DividerDefaults.color
                    )
                }
            }
        }
    }
}
