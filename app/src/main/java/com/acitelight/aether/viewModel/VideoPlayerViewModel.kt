package com.acitelight.aether.viewModel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.text.Cue
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import com.acitelight.aether.model.VideoRecord
import com.acitelight.aether.model.VideoRecordDatabase
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import com.acitelight.aether.view.formatTime
import com.acitelight.aether.view.hexToString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val mediaManager: MediaManager,
    val recentManager: RecentManager
) : ViewModel() {
    var tabIndex by mutableIntStateOf(0)
    var isPlaying by mutableStateOf(true)
    var playProcess by mutableFloatStateOf(0.0f)
    var planeVisibility by mutableStateOf(true)
    var isLongPressing by mutableStateOf(false)

    // -1 : Not dragging
    // 0  : Seek
    // 1  : Volume
    // 2  : Brightness
    var draggingPurpose by mutableIntStateOf(-1)

    var thumbUp by mutableIntStateOf(0)
    var thumbDown by mutableIntStateOf(0)
    var star by mutableStateOf(false)
    var locked by mutableStateOf(false)
    private var _init: Boolean = false;
    var startPlaying by mutableStateOf(false)
    var renderedFirst = false
    var video: Video? = null

    val dataSourceFactory = OkHttpDataSource.Factory(createOkHttp())
    var imageLoader: ImageLoader? = null;
    var brit by mutableFloatStateOf(0.5f)
    val database: VideoRecordDatabase = VideoRecordDatabase.getDatabase(context)
    var cues by mutableStateOf(listOf<Cue>())

    @OptIn(UnstableApi::class)
    fun init(videoId: String) {
        if (_init) return;
        val v = videoId.hexToString()
        imageLoader = ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()

        viewModelScope.launch {
            video = mediaManager.queryVideo(v.split("/")[0], v.split("/")[1])!!
            recentManager.pushVideo(context, VideoQueryIndex(v.split("/")[0], v.split("/")[1]))

            val subtitleCandidate = video?.getSubtitle()?.trim()
            val subtitleUri = tryResolveSubtitleUri(subtitleCandidate)

            // decide whether we need network-capable media source factory:
            val subtitleIsRemote = subtitleUri?.scheme?.startsWith("http", ignoreCase = true) == true
            val videoIsRemote = !video!!.isLocal
            val needNetworkFactory = videoIsRemote || subtitleIsRemote
            val trackSelector = DefaultTrackSelector(context)

            // build ExoPlayer with or without custom DefaultMediaSourceFactory
            val builder = if (needNetworkFactory)
                ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            else
                ExoPlayer.Builder(context)

            _player = builder.setTrackSelector(trackSelector).build().apply {
                val url = video?.getVideo() ?: ""
                val videoUri = if (video!!.isLocal) Uri.fromFile(File(url)) else url.toUri()

                val mediaItem: MediaItem = if (subtitleUri != null) {
                    // prepare subtitle configuration with guessed mime type
                    val subConfig = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                        .setMimeType("text/vtt")
                        .build()

                    MediaItem.Builder()
                        .setUri(videoUri)
                        .setSubtitleConfigurations(listOf(subConfig))
                        .build()
                } else {
                    MediaItem.fromUri(videoUri)
                }

                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        super.onTracksChanged(tracks)

                        val trackSelector = _player?.trackSelector
                        if (trackSelector is DefaultTrackSelector) {
                            val parameters = trackSelector.buildUponParameters()
                                .setSelectUndeterminedTextLanguage(true)
                                .build()
                            trackSelector.parameters = parameters
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == STATE_READY) {
                            startPlaying = true
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        super.onRenderedFirstFrame()
                        if(!renderedFirst)
                        {
                            viewModelScope.launch {
                                val ii = database.userDao().get(video!!.id, video!!.klass)
                                if(ii != null)
                                {
                                    _player!!.seekTo(ii.position)
                                    Toast.makeText(context, "Recover from ${formatTime(ii.position)} ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        renderedFirst = true
                    }

                    override fun onPlayerError(error: PlaybackException)
                    {
                        print(error.message)
                    }

                    override fun onCues(lcues: MutableList<Cue>) {
                        cues = lcues
                    }
                })
            }
            startListen()
        }
        _init = true;
    }

    /**
     * Try to resolve the given subtitle pathOrUrl to a Uri.
     * - If it's a local path and file exists -> Uri.fromFile
     * - If it's a http(s) URL -> try HEAD; if HEAD unsupported, try GET with Range: bytes=0-1
     * - Return null when unreachable / 404 / not exist
     */
    private suspend fun tryResolveSubtitleUri(pathOrUrl: String?): Uri? = withContext(Dispatchers.IO) {
        if (pathOrUrl.isNullOrBlank()) return@withContext null
        val trimmed = pathOrUrl.trim()

        // Remote URL case (http/https)
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            try {
                val client = createOkHttp()

                var headReq = Request.Builder().url(trimmed).head().build()
                var headResp = try { client.newCall(headReq).execute() } catch (e: Exception) { null }

                headResp?.use { resp ->
                    val code = resp.code
                    if (code == 200 || code == 206) {
                        return@withContext trimmed.toUri()
                    }
                    if (code == 404) {
                        return@withContext null
                    }
                }
                val rangeReq = Request.Builder()
                    .url(trimmed)
                    .addHeader("Range", "bytes=0-1")
                    .get()
                    .build()

                var rangeResp = try { client.newCall(rangeReq).execute() } catch (e: Exception) { null }

                rangeResp?.use { resp ->
                    val code = resp.code
                    if (code == 206) {
                        return@withContext trimmed.toUri()
                    }

                    if (code == 200) {
                        return@withContext trimmed.toUri()
                    }

                    if (code == 404) {
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                return@withContext null
            }
            return@withContext null
        } else {
            // Local path
            val f = File(trimmed)
            return@withContext if (f.exists() && f.isFile) Uri.fromFile(f) else null
        }
    }

    @OptIn(UnstableApi::class)
    fun startListen() {
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
        val p = _player!!.currentPosition
        _player?.release()
        CoroutineScope(Dispatchers.IO).launch {
            database.userDao().insert(VideoRecord(video!!.id, video!!.klass, p))
        }
    }
}
