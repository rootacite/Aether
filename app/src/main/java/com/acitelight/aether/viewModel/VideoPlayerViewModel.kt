package com.acitelight.aether.viewModel

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.acitelight.aether.Global
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import com.acitelight.aether.view.hexToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoPlayerViewModel() : ViewModel()
{
    var tabIndex by mutableIntStateOf(0)
    var isPlaying by  mutableStateOf(true)
    var playProcess by mutableFloatStateOf(0.0f)
    var planeVisibility by mutableStateOf(true)
    var isLongPressing by mutableStateOf(false)
    var dragging by mutableStateOf(false)

    var thumbUp by mutableIntStateOf(0)
    var thumbDown by mutableIntStateOf(0)
    var star by mutableStateOf(false)

    private var _init: Boolean = false;
    var startPlaying by mutableStateOf(false)
    var renderedFirst = false
    var video: Video? = null

    @Composable
    fun Init(videoId: String)
    {
        if(_init) return;
        val context = LocalContext.current
        val v = videoId.hexToString()

        remember {
            viewModelScope.launch {
                video = MediaManager.queryVideo(v.split("/")[0], v.split("/")[1])!!
                RecentManager.Push(context, VideoQueryIndex(v.split("/")[0], v.split("/")[1]))
                _player = ExoPlayer.Builder(context).build().apply {
                    val url = video?.getVideo() ?: ""
                    val mediaItem = MediaItem.fromUri(url)
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true

                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == STATE_READY) {
                                startPlaying = true
                            }
                        }

                        override fun onRenderedFirstFrame() {
                            super.onRenderedFirstFrame()
                            renderedFirst = true
                        }
                    })
                }
                startListen()
            }
        }

        _init = true;
    }

    @OptIn(UnstableApi::class)
    fun startListen()
    {
        CoroutineScope(Dispatchers.Main).launch {
            while (_player?.isReleased != true) {
                val __player = _player!!;
                playProcess = __player.currentPosition.toFloat() / __player.duration.toFloat()
                delay(100)
            }
        }
    }

    var _player: ExoPlayer? = null;

    override fun onCleared() {
        super.onCleared()
        _player?.release()
    }
}