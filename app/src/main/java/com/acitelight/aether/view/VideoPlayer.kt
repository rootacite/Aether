package com.acitelight.aether.view

import android.R
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.text.Layout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.viewModel.VideoPlayerViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.BottomNavigationBar
import com.acitelight.aether.Global
import com.acitelight.aether.ToggleFullScreen
import com.acitelight.aether.model.KeyImage
import com.acitelight.aether.ui.theme.AetherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.jetbrains.annotations.Async
import java.nio.file.WatchEvent

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
    val thumbRadius = 6.dp
    val trackHeight = 3.dp

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFFFFFFF),  // B站粉色
            activeTrackColor = Color(0xFFFF6699),
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
                        .background(Color(0xFFFF6699), RoundedCornerShape(50))
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
    val thumbRadius = 6.dp
    val trackHeight = 3.dp

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFFFFFFF),  // B站粉色
            activeTrackColor = Color(0xFFFF6699),
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
                        .background(Color(0xFFFF6699), RoundedCornerShape(50))
                )
            }
        }
    )
}

@Composable
fun VideoPlayer(
    videoPlayerViewModel: VideoPlayerViewModel = viewModel(),
    videoId: String,
    navController: NavHostController
) {
    videoPlayerViewModel.Init(videoId);
    videoPlayerViewModel.startListen()

    if (isLandscape()) {
        VideoPlayerLandscape(videoPlayerViewModel)
    }
    else
    {
        VideoPlayerPortal(videoPlayerViewModel, navController)
    }
}

@Composable
fun VideoPlayerPortal(videoPlayerViewModel: VideoPlayerViewModel, navController: NavHostController?) {
    val context = LocalContext.current
    val activity = context as? Activity
    val exoPlayer: ExoPlayer = videoPlayerViewModel._player!!;
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp;

    ToggleFullScreen(false)
    Column()
    {
        Box(modifier = Modifier.padding(top = 42.dp).heightIn(max = screenHeight * 0.65f))
        {
            AndroidView(
                factory = {
                    PlayerView(
                        it
                    ).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                videoPlayerViewModel.dragging = true
                                videoPlayerViewModel.planeVisibility = true
                                exoPlayer.pause()
                            },
                            onDragEnd = {
                                videoPlayerViewModel.dragging = false
                                if (videoPlayerViewModel.isPlaying)
                                    exoPlayer.play()
                            },
                            onDrag = { change, dragAmount ->
                                exoPlayer.seekTo((exoPlayer.currentPosition + dragAmount.x * 200.0f).toLong())
                                videoPlayerViewModel.playProcess = exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                                if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                            },
                            onTap = {
                                videoPlayerViewModel.planeVisibility =
                                    !videoPlayerViewModel.planeVisibility
                            },
                            onLongPress = {
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
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp).background(Color(0x44000000), RoundedCornerShape(18)))
                {
                    Row{
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = "Fast Forward",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp).padding(4.dp).align(Alignment.CenterVertically)
                        )

                        Text(
                            text = "3X Speed...",
                            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }
            }

            IconButton(
                onClick = { navController?.popBackStack() },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = videoPlayerViewModel.dragging,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "${formatTime((exoPlayer.duration * videoPlayerViewModel.playProcess).toLong())}/${
                        formatTime(
                            (exoPlayer.duration).toLong()
                        )
                    }",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    fontSize = 18.sp
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !videoPlayerViewModel.planeVisibility,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
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
                visible = videoPlayerViewModel.planeVisibility,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .align(Alignment.BottomCenter),
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

                    IconButton(
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
                    }
                }
            }
        }

        Row()
        {
            TabRow (
                selectedTabIndex = videoPlayerViewModel.tabIndex,
                modifier = Modifier.height(38.dp).fillMaxWidth(0.6f)
            ) {
                Tab(
                    selected = videoPlayerViewModel.tabIndex == 0,
                    onClick = { videoPlayerViewModel.tabIndex = 0  },
                    text = { Text(text = "Introduction", maxLines = 1) },
                    modifier = Modifier.height(38.dp)
                )

                Tab(
                    selected = videoPlayerViewModel.tabIndex == 1,
                    onClick = { videoPlayerViewModel.tabIndex = 1  },
                    text = { Text(text = "Comment", maxLines = 1) },
                    modifier = Modifier.height(38.dp)
                )
            }
        }

        LazyColumn( modifier = Modifier.fillMaxWidth()) {
            item{
                HorizontalDivider(Modifier, 2.dp, DividerDefaults.color)

                Text(
                    modifier = Modifier.align(Alignment.Start).padding(horizontal = 12.dp).padding(top = 12.dp),
                    text = Global.videoName,
                    fontSize = 16.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                )

                Row(Modifier.align(Alignment.Start).padding(horizontal = 4.dp).alpha(0.5f)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = Global.videoClass,
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = formatTime(Global.video?.video?.duration ?: 0),
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                    )
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, DividerDefaults.color)

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                {
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        IconButton(
                            onClick = {  },
                            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterHorizontally).size(36.dp),
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
                            fontWeight = FontWeight.Bold)
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        IconButton(
                            onClick = {  },
                            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterHorizontally).size(36.dp),
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
                            fontWeight = FontWeight.Bold)
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        IconButton(
                            onClick = { videoPlayerViewModel.star = !videoPlayerViewModel.star  },
                            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterHorizontally).size(36.dp),
                        ) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = if(videoPlayerViewModel.star) Color(0xFFFF6699) else Color.Gray
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterHorizontally).size(36.dp),
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
                            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterHorizontally).size(36.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalGallery()
                HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, DividerDefaults.color)
            }
        }
    }
}

@Composable
fun HorizontalGallery()
{
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(Global.video?.getGallery() ?: listOf()) { it ->
            SingleImageItem(img = it)
        }
    }
}

@Composable
fun SingleImageItem(img: KeyImage) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(img.url)
            .memoryCacheKey(img.key)
            .diskCacheKey(img.key)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun VideoPlayerLandscape(videoPlayerViewModel: VideoPlayerViewModel)
{
    val context = LocalContext.current
    val activity = context as? Activity
    val exoPlayer: ExoPlayer = videoPlayerViewModel._player!!;

    BackHandler {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    ToggleFullScreen(true)
    Box(
        modifier = Modifier
            .background(Color.Black)
    )
    {
        AndroidView(
            factory = {
                PlayerView(
                    it
                ).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            videoPlayerViewModel.planeVisibility = true
                            videoPlayerViewModel.dragging = true;
                            exoPlayer.pause()
                        },
                        onDragEnd = {
                            videoPlayerViewModel.dragging = false;
                            if (videoPlayerViewModel.isPlaying)
                                exoPlayer.play()
                        },
                        onDrag = { change, dragAmount ->
                            exoPlayer.seekTo((exoPlayer.currentPosition + dragAmount.x * 200.0f).toLong())
                            videoPlayerViewModel.playProcess = exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                            if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                        },
                        onTap = {
                            videoPlayerViewModel.planeVisibility =
                                !videoPlayerViewModel.planeVisibility
                        },
                        onLongPress = {
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
            visible = videoPlayerViewModel.dragging,
            enter = fadeIn(
                initialAlpha = 0f,
            ),
            exit = fadeOut(
                targetAlpha = 0f
            ),
            modifier = Modifier.align(Alignment.Center)
        ) {
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

        AnimatedVisibility(
            visible = videoPlayerViewModel.isLongPressing,
            enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
        {
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp).background(Color(0x44000000), RoundedCornerShape(18)))
            {
                Row{
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "Fast Forward",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp).padding(4.dp).align(Alignment.CenterVertically)
                    )

                    Text(
                        text = "3X Speed...",
                        modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }
        }

        IconButton(
            onClick = {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                      },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        AnimatedVisibility(
            visible = videoPlayerViewModel.planeVisibility,
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
                    .background( brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        )
                    ))
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
                    modifier = Modifier.height(16.dp).fillMaxWidth().padding(bottom = 8.dp)
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
    }
}