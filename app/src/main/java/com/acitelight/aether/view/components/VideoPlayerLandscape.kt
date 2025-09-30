package com.acitelight.aether.view.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.acitelight.aether.ToggleFullScreen
import com.acitelight.aether.view.pages.formatTime
import com.acitelight.aether.view.pages.moveBrit
import com.acitelight.aether.viewModel.VideoPlayerViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerLandscape(videoPlayerViewModel: VideoPlayerViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val activity = (context as? Activity)!!
    val exoPlayer: ExoPlayer = videoPlayerViewModel.player!!

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var volFactor by remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume.toFloat()
        )
    }

    val name by videoPlayerViewModel.currentName
    val id by videoPlayerViewModel.currentId

    fun setVolume(value: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            value.coerceIn(0, maxVolume),
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    BackHandler {
        videoPlayerViewModel.isLandscape = false
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
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(videoPlayerViewModel) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (videoPlayerViewModel.locked) return@detectDragGestures
                                if (offset.y > size.height * 0.9 || offset.y < size.height * 0.1)
                                    videoPlayerViewModel.draggingPurpose = -3
                                // Set gesture protection for the bottom of the screen
                                // (Prevent conflicts with system gestures, such as dropdown status bar, bottom swipe up menu)
                                else if (offset.x < size.width / 2) {
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
                                    volFactor =
                                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                            .toFloat() / maxVolume.toFloat()
                                    if (dragAmount.y < 0)
                                        setVolume(cu + 1)
                                    else if (dragAmount.y > 0)
                                        setVolume(cu - 1)
                                } else if (videoPlayerViewModel.draggingPurpose == 1) {
                                    moveBrit(dragAmount.y, activity, videoPlayerViewModel)
                                }

                            }
                        )
                    }
                    .pointerInput(videoPlayerViewModel) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (videoPlayerViewModel.locked) return@detectTapGestures

                                videoPlayerViewModel.isPlaying = !videoPlayerViewModel.isPlaying
                                if (videoPlayerViewModel.isPlaying) exoPlayer.play() else exoPlayer.pause()
                            },
                            onTap = {
                                if (videoPlayerViewModel.locked) return@detectTapGestures
                                if (videoPlayerViewModel.showPlaylist) {
                                    videoPlayerViewModel.showPlaylist = false
                                    return@detectTapGestures
                                }

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
                    }) {
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
                    modifier = Modifier.fillMaxWidth()
                )
            }


            AnimatedVisibility(
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
                        formatTime(exoPlayer.duration)
                    }",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )
            }

            AnimatedVisibility(
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
                Row(
                    Modifier
                        .background(Color(0x88000000), RoundedCornerShape(18))
                        .width(200.dp)
                )
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

            AnimatedVisibility(
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
                Row(
                    Modifier
                        .background(Color(0x88000000), RoundedCornerShape(18))
                        .width(200.dp)
                )
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
                    IconButton(
                        onClick = {
                            videoPlayerViewModel.isLandscape = false
                        },
                        Modifier
                            .padding(top = 12.dp)
                            .padding(start = 46.dp)
                            .size(36.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 12.dp)
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
                            formatTime(exoPlayer.duration)
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

                        IconButton(
                            onClick = {
                                videoPlayerViewModel.viewModelScope.launch {
                                    videoPlayerViewModel.startPlay(
                                        videoPlayerViewModel.videos.getOrNull(videoPlayerViewModel.videos.indexOf(
                                            videoPlayerViewModel.videos.first {
                                                it.id == videoPlayerViewModel.currentId.value
                                            }) + 1) ?: videoPlayerViewModel.videos.first()
                                    )
                                }
                            },
                            Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                videoPlayerViewModel.isLandscape = false
                            },
                            Modifier
                                .size(36.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                Icons.Default.FullscreenExit,
                                contentDescription = "Exit FullScreen",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                videoPlayerViewModel.showPlaylist = true
                            },
                            Modifier
                                .size(36.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Playlist",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = videoPlayerViewModel.locked || videoPlayerViewModel.planeVisibility,
                enter = fadeIn(
                    initialAlpha = 0f,
                ),
                exit = fadeOut(
                    targetAlpha = 0f
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            {
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

            AnimatedVisibility(
                visible = videoPlayerViewModel.showPlaylist,
                enter = slideInHorizontally(initialOffsetX = { full -> full }),
                exit = slideOutHorizontally(targetOffsetX = { full -> full }),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            {
                Card(
                    Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .align(Alignment.CenterEnd),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(0.75f))
                )
                {
                    LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                        items(videoPlayerViewModel.videos) { item ->
                            MiniPlaylistCard(Modifier.padding(4.dp), video = item, imageLoader = videoPlayerViewModel.imageLoader!!,
                                selected = id == item.id, apiClient = videoPlayerViewModel.apiClient)
                            {
                                if (name == item.video.name)
                                    return@MiniPlaylistCard

                                videoPlayerViewModel.viewModelScope.launch {
                                    videoPlayerViewModel.startPlay(item)
                                }
                            }
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