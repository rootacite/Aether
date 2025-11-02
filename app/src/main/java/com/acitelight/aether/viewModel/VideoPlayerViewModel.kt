package com.acitelight.aether.viewModel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.KeyImage
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import com.acitelight.aether.model.VideoRecord
import com.acitelight.aether.model.VideoRecordDatabase
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import com.acitelight.aether.service.VideoLibrary
import com.acitelight.aether.view.pages.formatTime
import com.acitelight.aether.view.pages.hexToString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val mediaManager: MediaManager,
    val recentManager: RecentManager,
    val videoLibrary: VideoLibrary,
    val apiClient: ApiClient
) : ViewModel() {
    var showPlaylist by mutableStateOf(false)
    var isLandscape by mutableStateOf(false)
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
    var locked by mutableStateOf(false)
    private var _init: Boolean = false
    var startPlaying by mutableStateOf(false)
    var renderedFirst = false
    var videos: List<Video> = listOf()

    private val httpDataSourceFactory = OkHttpDataSource.Factory(apiClient.getClient())
    private val defaultDataSourceFactory by lazy {
        DefaultDataSource.Factory(
            context,
            httpDataSourceFactory
        )
    }
    var brit by mutableFloatStateOf(0.0f)
    val database: VideoRecordDatabase = VideoRecordDatabase.getDatabase(context)
    var cues by mutableStateOf(listOf<Cue>())
    var currentKlass = mutableStateOf("")
    var currentId = mutableStateOf("")
    var currentName = mutableStateOf("")
    var currentDuration = mutableLongStateOf(0)
    var currentGallery = mutableStateOf(listOf<KeyImage>())

    @OptIn(UnstableApi::class)
    fun init(videoId: String) {
        if (_init)
            return
        _init = true

        val oId = videoId.hexToString()
        var spec = "-1"
        var vs: MutableList<List<String>>

        if(oId.contains("|"))
        {
            vs = oId.split("|")[0].split(",").map { it.split("/") }.toMutableList()
            spec = oId.split("|")[1]
        }else{
            vs = oId.split(",").map { it.split("/") }.toMutableList()
        }

        viewModelScope.launch {
            videos = mediaManager.queryVideoBulk(vs.first()[0], vs.map { it[1] })!!

            val ii = database.userDao().getAll().first()
            val ix = ii.filter { it.id in videos.map{ m -> m.id } }.maxByOrNull { it.time }

            startPlay(
                if(spec != "-1")
                    videos.first { it.id == spec}
                else if (ix != null)
                    videos.first { it.id == ix.id }
                else videos.first()
            )
            startListen()
        }
    }

    /**
     * Try to resolve the given subtitle pathOrUrl to a Uri.
     * - If it's a local path and file exists -> Uri.fromFile
     * - If it's a http(s) URL -> try HEAD; if HEAD unsupported, try GET with Range: bytes=0-1
     * - Return null when unreachable / 404 / not exist
     */
    private suspend fun tryResolveSubtitleUri(pathOrUrl: String?): Uri? =
        withContext(Dispatchers.IO) {
            if (pathOrUrl.isNullOrBlank()) return@withContext null
            val trimmed = pathOrUrl.trim()

            // Remote URL case (http/https)
            if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith(
                    "https://",
                    ignoreCase = true
                )
            ) {
                try {
                    val client = apiClient.getClient()

                    val headReq = Request.Builder().url(trimmed).head().build()
                    val headResp = try {
                        client.newCall(headReq).execute()
                    } catch (_: Exception) {
                        null
                    }

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

                    val rangeResp = try {
                        client.newCall(rangeReq).execute()
                    } catch (_: Exception) {
                        null
                    }

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
                } catch (_: Exception) {
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
            while (_init) {
                player?.let { playProcess = it.currentPosition.toFloat() / it.duration.toFloat() }
                delay(100)
            }
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun startPlay(video: Video) {
        if (currentId.value.isNotEmpty() && currentKlass.value.isNotEmpty()) {
            val pos = player?.currentPosition ?: 0L
            database.userDao().insert(
                VideoRecord(
                    currentId.value,
                    currentKlass.value,
                    pos,
                    System.currentTimeMillis(),
                    videos.joinToString(",") { it.id })
            )
        }

        renderedFirst = false
        currentId.value = video.id
        currentKlass.value = video.klass
        currentName.value = video.video.name
        currentDuration.longValue = video.video.duration
        currentGallery.value = video.getGallery(apiClient)

        player?.apply {
            stop()
            clearMediaItems()
        }

        recentManager.pushVideo(context, VideoQueryIndex(video.klass, video.id))

        val subtitleCandidate = video.getSubtitle(apiClient).trim()
        val subtitleUri = tryResolveSubtitleUri(subtitleCandidate)

        if (player == null) {
            val trackSelector = DefaultTrackSelector(context)
            val builder = ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(defaultDataSourceFactory))

            player = builder.setTrackSelector(trackSelector).build().apply {
                addListener(object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        val trackSelector = player?.trackSelector
                        if (trackSelector is DefaultTrackSelector) {
                            val parameters = trackSelector.buildUponParameters()
                                .setSelectUndeterminedTextLanguage(true)
                                .build()
                            trackSelector.parameters = parameters
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when(playbackState)
                        {
                            STATE_READY -> {
                                startPlaying = true
                            }
                            STATE_ENDED -> {
                                player?.seekTo(0)
                                player?.pause()
                            }
                            else -> {

                            }
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        if (!renderedFirst) {
                            viewModelScope.launch {
                                val ii = database.userDao().get(currentId.value, currentKlass.value)
                                if (ii != null) {
                                    player?.seekTo(ii.position)
                                    Toast.makeText(
                                        context,
                                        "Recover from ${formatTime(ii.position)} ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        renderedFirst = true
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        print(error.message)
                    }

                    override fun onCues(lcues: MutableList<Cue>) {
                        cues = lcues
                    }
                })
            }
        }

        val url = video.getVideo(apiClient)
        val videoUri = if (video.isLocal) Uri.fromFile(File(url)) else url.toUri()

        val mediaItem: MediaItem = if (subtitleUri != null) {
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

        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    var player: ExoPlayer? = null

    override fun onCleared() {
        super.onCleared()

        _init = false
        val pos = player?.currentPosition ?: 0L
        player?.release()
        player = null

        CoroutineScope(Dispatchers.IO).launch {
            if (currentId.value.isNotEmpty() && currentKlass.value.isNotEmpty())
                database.userDao().insert(
                    VideoRecord(
                        currentId.value,
                        currentKlass.value,
                        pos,
                        System.currentTimeMillis(),
                        videos.joinToString(",") { it.id })
                )
        }
    }
}
