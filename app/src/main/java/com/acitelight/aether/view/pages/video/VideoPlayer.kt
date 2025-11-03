package com.acitelight.aether.view.pages.video

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.acitelight.aether.viewModel.video.VideoPlayerViewModel

import androidx.compose.runtime.DisposableEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.acitelight.aether.view.components.video.VideoPlayerLandscape
import com.acitelight.aether.view.components.video.VideoPlayerPortal
import kotlin.math.pow

fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun moveBrit(db: Float, activity: Activity, videoPlayerViewModel: VideoPlayerViewModel) {
    val attr = activity.window.attributes

    val britUi = (videoPlayerViewModel.brit - db * 0.002f).coerceIn(0f, 1f)
    videoPlayerViewModel.brit = britUi

    val gamma = 2.2f
    val britSystem = britUi.pow(gamma).coerceIn(0.001f, 1f)

    attr.screenBrightness = britSystem
    activity.window.attributes = attr
}

@Composable
fun VideoPlayer(
    videoPlayerViewModel: VideoPlayerViewModel = hiltViewModel<VideoPlayerViewModel>(),
    videoId: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = (context as? Activity)!!

    DisposableEffect(Unit) {
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    videoPlayerViewModel.init(videoId)

    activity.requestedOrientation =
        if(videoPlayerViewModel.isLandscape)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    if (videoPlayerViewModel.startPlaying) {
        if (videoPlayerViewModel.isLandscape) {
            VideoPlayerLandscape(videoPlayerViewModel)
        } else {
            VideoPlayerPortal(videoPlayerViewModel, navController)
        }
    }
}
