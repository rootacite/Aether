package com.acitelight.aether.view

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.acitelight.aether.viewModel.VideoPlayerViewModel

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.Global
import com.acitelight.aether.ToggleFullScreen
import com.acitelight.aether.model.KeyImage
import com.acitelight.aether.model.Video
import kotlinx.coroutines.launch
import kotlin.math.abs

fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}


@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiliStyleSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val colorScheme = MaterialTheme.colorScheme
    val trackHeight = 3.dp

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFFFFFFF),
            activeTrackColor = colorScheme.primary,
            inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
        ),

        track = { sliderPositions ->
            Box(
                Modifier
                    .height(trackHeight)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(value)
                        .fillMaxHeight()
                        .background(colorScheme.primary, RoundedCornerShape(50))
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiliMiniSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val colorScheme = MaterialTheme.colorScheme
    val trackHeight = 3.dp

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFFFFFFF),
            activeTrackColor = colorScheme.primary,
            inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
        ),
        thumb = {

        },
        track = { sliderPositions ->
            Box(
                Modifier
                    .height(trackHeight)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(value)
                        .fillMaxHeight()
                        .background(colorScheme.primary, RoundedCornerShape(50))
                )
            }
        }
    )
}

@Composable
fun SubtitleOverlay(
    cues: List<Cue>,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    textSize: TextUnit = 14.sp,
    backgroundAlpha: Float = 0.6f,
    horizontalMargin: Dp = 16.dp,
    bottomMargin: Dp = 14.dp,
    contentPadding: Dp = 6.dp,
    cornerRadius: Dp = 6.dp,
    textColor: Color = Color.White
) {
    val raw = if (cues.isEmpty()) "" else cues.joinToString(separator = "\n") {
        it.text?.toString() ?: ""
    }.trim()
    if (raw.isEmpty()) return

    val textAlign = when (cues.firstOrNull()?.textAlignment) {
        android.text.Layout.Alignment.ALIGN_CENTER -> TextAlign.Center
        android.text.Layout.Alignment.ALIGN_OPPOSITE -> TextAlign.End
        android.text.Layout.Alignment.ALIGN_NORMAL -> TextAlign.Start
        else -> TextAlign.Center
    }

    val blurPx = with(LocalDensity.current) { (2.dp).toPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .padding(start = horizontalMargin, end = horizontalMargin, bottom = bottomMargin)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.Black.copy(alpha = backgroundAlpha))
                .padding(horizontal = 12.dp, vertical = contentPadding)
        ) {
            Text(
                text = raw,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = textColor,
                    fontSize = textSize,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = blurPx
                    )
                ),
                textAlign = textAlign,
                modifier = Modifier
            )
        }
    }
}


@Composable
fun VideoPlayer(
    videoPlayerViewModel: VideoPlayerViewModel = hiltViewModel<VideoPlayerViewModel>(),
    videoId: String,
    navController: NavHostController
) {
    val colorScheme = MaterialTheme.colorScheme
    videoPlayerViewModel.init(videoId)

    if (videoPlayerViewModel.startPlaying) {
        if (isLandscape()) {
            Box {
                VideoPlayerLandscape(videoPlayerViewModel)
                AnimatedVisibility(
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
        } else {
            VideoPlayerPortal(videoPlayerViewModel, navController)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PortalCorePlayer(modifier: Modifier, videoPlayerViewModel: VideoPlayerViewModel, cover: Float) {
    val exoPlayer: ExoPlayer = videoPlayerViewModel.player!!
    val context = LocalContext.current
    val activity = context as? Activity

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var volFactor by remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume.toFloat()
        )
    }

    fun setVolume(value: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            value.coerceIn(0, maxVolume),
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    Box(modifier)
    {
        AndroidView(
            factory = {
                PlayerView(
                    it
                ).apply {
                    player = exoPlayer
                    useController = false
                    subtitleView?.let { sv ->
                        sv.visibility = View.GONE
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (videoPlayerViewModel.locked) return@detectDragGestures
                            if (offset.x < size.width / 2) {
                                videoPlayerViewModel.draggingPurpose = -1
                            } else {
                                videoPlayerViewModel.draggingPurpose = -2
                            }
                        },
                        onDragEnd = {
                            if (videoPlayerViewModel.isPlaying && videoPlayerViewModel.draggingPurpose == 0)
                                exoPlayer.play()

                            videoPlayerViewModel.draggingPurpose = -1
                        },
                        onDrag = { change, dragAmount ->
                            if (videoPlayerViewModel.locked) return@detectDragGestures
                            if (abs(dragAmount.x) > abs(dragAmount.y) &&
                                (videoPlayerViewModel.draggingPurpose == -1 || videoPlayerViewModel.draggingPurpose == -2)
                            ) {
                                videoPlayerViewModel.draggingPurpose = 0
                                videoPlayerViewModel.planeVisibility = true
                                exoPlayer.pause()
                            } else if (videoPlayerViewModel.draggingPurpose == -1) videoPlayerViewModel.draggingPurpose =
                                1
                            else if (videoPlayerViewModel.draggingPurpose == -2) videoPlayerViewModel.draggingPurpose =
                                2

                            if (videoPlayerViewModel.draggingPurpose == 0) {
                                exoPlayer.seekTo((exoPlayer.currentPosition + dragAmount.x * 200.0f).toLong())
                                videoPlayerViewModel.playProcess =
                                    exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
                            } else if (videoPlayerViewModel.draggingPurpose == 2) {
                                val cu = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                volFactor = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    .toFloat() / maxVolume.toFloat()
                                if (dragAmount.y < 0)
                                    setVolume(cu + 1)
                                else if (dragAmount.y > 0)
                                    setVolume(cu - 1)
                            } else if (videoPlayerViewModel.draggingPurpose == 1) {
                                videoPlayerViewModel.brit =
                                    (videoPlayerViewModel.brit - dragAmount.y * 0.002f).coerceIn(
                                        0f,
                                        1f
                                    )

                                activity?.window?.attributes = activity.window.attributes.apply {
                                    screenBrightness = videoPlayerViewModel.brit.coerceIn(0f, 1f)
                                }
                                activity?.window?.setAttributes(activity.window.attributes)
                            }

                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (videoPlayerViewModel.locked) return@detectTapGestures
                            videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                            if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                        },
                        onTap = {
                            if (videoPlayerViewModel.locked) return@detectTapGestures
                            videoPlayerViewModel.planeVisibility =
                                !videoPlayerViewModel.planeVisibility
                        },
                        onLongPress = {
                            if (videoPlayerViewModel.locked) return@detectTapGestures
                            videoPlayerViewModel.isLongPressing = true
                            exoPlayer.playbackParameters = exoPlayer.playbackParameters
                                .withSpeed(3.0f)
                        },
                        onPress = { offset ->
                            val pressResult = tryAwaitRelease()
                            if (pressResult && videoPlayerViewModel.isLongPressing) {
                                videoPlayerViewModel.isLongPressing = false
                                exoPlayer.playbackParameters = exoPlayer.playbackParameters
                                    .withSpeed(1.0f)
                            }
                        },
                    )
                }
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = videoPlayerViewModel.isLongPressing,
            enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
        {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .background(Color(0x44000000), RoundedCornerShape(18))
            )
            {
                Row {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "Fast Forward",
                        tint = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                            .align(Alignment.CenterVertically)
                    )

                    Text(
                        text = "3X Speed...",
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.CenterVertically),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = videoPlayerViewModel.draggingPurpose == 0,
            enter = fadeIn(
                initialAlpha = 0f,
            ),
            exit = fadeOut(
                targetAlpha = 0f
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        {
            Text(
                text = "${formatTime((exoPlayer.duration * videoPlayerViewModel.playProcess).toLong())}/${
                    formatTime(
                        (exoPlayer.duration)
                    )
                }",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
                fontSize = 18.sp
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = videoPlayerViewModel.draggingPurpose == 2,
            enter = fadeIn(
                initialAlpha = 0f,
            ),
            exit = fadeOut(
                targetAlpha = 0f
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        {
            Row(Modifier
                .background(Color(0x88000000), RoundedCornerShape(18))
                .width(200.dp))
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Vol",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically)
                )
                BiliMiniSlider(
                    value = volFactor,
                    onValueChange = {},
                    modifier = Modifier
                        .height(4.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = videoPlayerViewModel.draggingPurpose == 1,
            enter = fadeIn(
                initialAlpha = 0f,
            ),
            exit = fadeOut(
                targetAlpha = 0f
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        {
            Row(Modifier
                .background(Color(0x88000000), RoundedCornerShape(18))
                .width(200.dp))
            {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = "Brightness",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically)
                )
                BiliMiniSlider(
                    value = videoPlayerViewModel.brit,
                    onValueChange = {},
                    modifier = Modifier
                        .height(4.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        if (cover > 0.0f)
            Spacer(Modifier
                .background(MaterialTheme.colorScheme.primary.copy(cover))
                .fillMaxSize())

        androidx.compose.animation.AnimatedVisibility(
            visible = (!videoPlayerViewModel.planeVisibility) || videoPlayerViewModel.locked,
            enter = fadeIn(
                initialAlpha = 0f,
            ),
            exit = fadeOut(
                targetAlpha = 0f
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BiliMiniSlider(
                value = videoPlayerViewModel.playProcess,
                onValueChange = {},
                modifier = Modifier
                    .height(4.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = videoPlayerViewModel.planeVisibility && (!videoPlayerViewModel.locked),
            enter = fadeIn(
                initialAlpha = 0f,
            ),
            exit = fadeOut(
                targetAlpha = 0f
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(42.dp)
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                            )
                        )
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = {
                        videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                        if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                    },
                    Modifier
                        .size(36.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = if (videoPlayerViewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                BiliStyleSlider(
                    value = videoPlayerViewModel.playProcess,
                    onValueChange = { value ->
                        exoPlayer.seekTo((exoPlayer.duration * value).toLong())
                    },
                    modifier = Modifier
                        .height(8.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                )

                Text(
                    text = formatTime((exoPlayer.duration * videoPlayerViewModel.playProcess).toLong()),
                    maxLines = 1,
                    fontSize = 12.sp,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .width(80.dp)
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                )

                /* IconButton(
                    onClick = {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    },
                    Modifier
                        .size(36.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } */
            }
        }

        SubtitleOverlay(
            cues = videoPlayerViewModel.cues,
            modifier = Modifier.matchParentSize()
        )
    }
}

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

@Composable
fun SocialPanel(modifier: Modifier, videoPlayerViewModel: VideoPlayerViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center
    )
    {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(36.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = "ThumbUp",
                    tint = Color.Gray
                )
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = videoPlayerViewModel.thumbUp.toString(),
                fontSize = 12.sp,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(36.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Filled.ThumbDown,
                    contentDescription = "ThumbDown",
                    tint = Color.Gray
                )
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = videoPlayerViewModel.thumbDown.toString(),
                fontSize = 12.sp,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            IconButton(
                onClick = { videoPlayerViewModel.star = !videoPlayerViewModel.star },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(36.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star",
                    tint = if (videoPlayerViewModel.star) colorScheme.primary else Color.Gray
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(36.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Forward",
                    tint = Color.Gray
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(36.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Detail",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PlaylistPanel(modifier: Modifier, videoPlayerViewModel: VideoPlayerViewModel)
{
    val name by videoPlayerViewModel.currentName

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(videoPlayerViewModel.videos) { it ->
            // SingleImageItem(img = it, videoPlayerViewModel.imageLoader!!)
            Card(
                modifier = Modifier.fillMaxHeight().width(140.dp),
                onClick = {
                    if(name == it.video.name)
                        return@Card

                    videoPlayerViewModel.viewModelScope.launch {
                        videoPlayerViewModel.startPlay(it)
                    }
                }
            ) {
                Box(Modifier.padding(8.dp).fillMaxSize())
                {
                    Text(modifier = Modifier.align(Alignment.Center), text = it.video.name, maxLines = 4, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 13.sp)
                }
            }
        }
    }
}

@Composable
fun HorizontalGallery(videoPlayerViewModel: VideoPlayerViewModel) {
    val gallery by videoPlayerViewModel.currentGallery
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(gallery) { it ->
            SingleImageItem(img = it, videoPlayerViewModel.imageLoader!!)
        }
    }
}

@Composable
fun SingleImageItem(img: KeyImage, imageLoader: ImageLoader) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(img.url)
            .memoryCacheKey(img.key)
            .diskCacheKey(img.key)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
        imageLoader = imageLoader
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerLandscape(videoPlayerViewModel: VideoPlayerViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val exoPlayer: ExoPlayer = videoPlayerViewModel.player!!;

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var volFactor by remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume.toFloat()
        )
    }

    val name by videoPlayerViewModel.currentName

    fun setVolume(value: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            value.coerceIn(0, maxVolume),
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    ToggleFullScreen(true)
    Box(Modifier.fillMaxSize())
    {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .align(Alignment.Center)
        )
        {
            AndroidView(
                factory = {
                    PlayerView(
                        it
                    ).apply {
                        player = exoPlayer
                        useController = false
                        subtitleView?.let { sv ->
                            sv.visibility = View.GONE
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (videoPlayerViewModel.locked) return@detectDragGestures
                                if (offset.x < size.width / 2) {
                                    videoPlayerViewModel.draggingPurpose = -1;
                                } else {
                                    videoPlayerViewModel.draggingPurpose = -2;
                                }
                            },
                            onDragEnd = {
                                if (videoPlayerViewModel.isPlaying && videoPlayerViewModel.draggingPurpose == 0)
                                    exoPlayer.play()

                                videoPlayerViewModel.draggingPurpose = -1;
                            },
                            onDrag = { change, dragAmount ->
                                if (videoPlayerViewModel.locked) return@detectDragGestures
                                if (abs(dragAmount.x) > abs(dragAmount.y) &&
                                    (videoPlayerViewModel.draggingPurpose == -1 || videoPlayerViewModel.draggingPurpose == -2)
                                ) {
                                    videoPlayerViewModel.draggingPurpose = 0
                                    videoPlayerViewModel.planeVisibility = true
                                    exoPlayer.pause()
                                } else if (videoPlayerViewModel.draggingPurpose == -1) videoPlayerViewModel.draggingPurpose =
                                    1
                                else if (videoPlayerViewModel.draggingPurpose == -2) videoPlayerViewModel.draggingPurpose =
                                    2

                                if (videoPlayerViewModel.draggingPurpose == 0) {
                                    exoPlayer.seekTo((exoPlayer.currentPosition + dragAmount.x * 200.0f).toLong())
                                    videoPlayerViewModel.playProcess =
                                        exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
                                } else if (videoPlayerViewModel.draggingPurpose == 2) {
                                    val cu = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    volFactor =
                                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                            .toFloat() / maxVolume.toFloat()
                                    if (dragAmount.y < 0)
                                        setVolume(cu + 1);
                                    else if (dragAmount.y > 0)
                                        setVolume(cu - 1);
                                } else if (videoPlayerViewModel.draggingPurpose == 1) {
                                    videoPlayerViewModel.brit =
                                        (videoPlayerViewModel.brit - dragAmount.y * 0.002f).coerceIn(
                                            0f,
                                            1f
                                        );

                                    activity?.window?.attributes =
                                        activity.window.attributes.apply {
                                            screenBrightness =
                                                videoPlayerViewModel.brit.coerceIn(0f, 1f)
                                        }
                                    activity?.window?.setAttributes(activity.window.attributes)
                                }

                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (videoPlayerViewModel.locked) return@detectTapGestures

                                videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                                if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                            },
                            onTap = {
                                if (videoPlayerViewModel.locked) return@detectTapGestures

                                videoPlayerViewModel.planeVisibility =
                                    !videoPlayerViewModel.planeVisibility
                            },
                            onLongPress = {
                                if (videoPlayerViewModel.locked) return@detectTapGestures

                                videoPlayerViewModel.isLongPressing = true
                                exoPlayer.playbackParameters = exoPlayer.playbackParameters
                                    .withSpeed(3.0f)
                            },
                            onPress = { offset ->
                                val pressResult = tryAwaitRelease()
                                if (pressResult && videoPlayerViewModel.isLongPressing) {
                                    videoPlayerViewModel.isLongPressing = false
                                    exoPlayer.playbackParameters = exoPlayer.playbackParameters
                                        .withSpeed(1.0f)
                                }
                            },
                        )
                    }
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = videoPlayerViewModel.draggingPurpose == 0,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.align(Alignment.Center)
            )
            {
                Text(
                    text = "${formatTime((exoPlayer.duration * videoPlayerViewModel.playProcess).toLong())}/${
                        formatTime(
                            (exoPlayer.duration).toLong()
                        )
                    }",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = videoPlayerViewModel.draggingPurpose == 2,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.align(Alignment.Center)
            )
            {
                Row(Modifier
                    .background(Color(0x88000000), RoundedCornerShape(18))
                    .width(200.dp))
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Vol",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    BiliMiniSlider(
                        value = volFactor,
                        onValueChange = {},
                        modifier = Modifier
                            .height(4.dp)
                            .padding(horizontal = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = videoPlayerViewModel.draggingPurpose == 1,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.align(Alignment.Center)
            )
            {
                Row(Modifier
                    .background(Color(0x88000000), RoundedCornerShape(18))
                    .width(200.dp))
                {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = "Brightness",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    BiliMiniSlider(
                        value = videoPlayerViewModel.brit,
                        onValueChange = {},
                        modifier = Modifier
                            .height(4.dp)
                            .padding(horizontal = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            AnimatedVisibility(
                visible = videoPlayerViewModel.isLongPressing,
                enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
            )
            {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                        .background(Color(0x44000000), RoundedCornerShape(18))
                )
                {
                    Row {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = "Fast Forward",
                            tint = Color.White,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(4.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Text(
                            text = "3X Speed...",
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.CenterVertically),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = videoPlayerViewModel.planeVisibility && (!videoPlayerViewModel.locked),
                enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            )
            {
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                )
                            )
                        )
                )
                {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 46.dp).padding(top = 12.dp)
                            .align(Alignment.CenterVertically),
                        fontSize = 18.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = videoPlayerViewModel.planeVisibility && (!videoPlayerViewModel.locked),
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
            {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                        .padding(horizontal = 36.dp)
                ) {
                    Text(
                        text = "${formatTime((exoPlayer.duration * videoPlayerViewModel.playProcess).toLong())}/${
                            formatTime(
                                (exoPlayer.duration).toLong()
                            )
                        }",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp),
                        fontSize = 12.sp
                    )
                    BiliStyleSlider(
                        value = videoPlayerViewModel.playProcess,
                        onValueChange = { value ->
                            exoPlayer.seekTo((exoPlayer.duration * value).toLong())
                        },
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .align(Alignment.Start),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            onClick = {
                                videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                                if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                            },
                            Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (videoPlayerViewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }

            SubtitleOverlay(
                cues = videoPlayerViewModel.cues,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun MiniVideoCard(modifier: Modifier, video: Video, onClick: () -> Unit, imageLoader: ImageLoader) {
    Card(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth(),
        colors = CardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        onClick = onClick
    )
    {
        Row()
        {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.getCover())
                    .memoryCacheKey("${video.klass}/${video.id}/cover")
                    .diskCacheKey("${video.klass}/${video.id}/cover")
                    .listener(
                        onStart = { },
                        onError = { _, _ -> }
                    )
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .width(128.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                imageLoader = imageLoader
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            )
            {
                Text(
                    modifier = Modifier,
                    text = video.video.name,
                    fontSize = 14.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier.weight(1f))

                Text(
                    modifier = Modifier.height(16.dp),
                    text = video.klass,
                    fontSize = 8.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    modifier = Modifier.height(16.dp),
                    text = formatTime(video.video.duration),
                    fontSize = 8.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}