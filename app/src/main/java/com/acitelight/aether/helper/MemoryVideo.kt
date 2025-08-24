import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun InMemoryVideoPlayer(
    modifier: Modifier = Modifier,
    videoData: ByteArray
) {
    val context = LocalContext.current

    val exoPlayer = remember(context, videoData) {
        createExoPlayer(context, videoData)
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
private fun createExoPlayer(context: Context, videoData: ByteArray): ExoPlayer {
    val byteArrayDataSource = ByteArrayDataSource(videoData)

    val factory = DataSource.Factory {
        byteArrayDataSource
    }

    val mediaSource = ProgressiveMediaSource.Factory(factory)
        .createMediaSource(MediaItem.fromUri("data://local/video.mp4"))

    return ExoPlayer.Builder(context).build().apply {
        setMediaSource(mediaSource)
        prepare()
        playWhenReady = false
    }
}