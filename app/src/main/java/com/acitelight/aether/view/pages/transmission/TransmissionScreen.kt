package com.acitelight.aether.view.pages.transmission

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.viewModel.transmission.TransmissionScreenViewModel

@Composable
fun TransmissionScreen(
    navigator: NavHostController,
    transmissionScreenViewModel: TransmissionScreenViewModel = hiltViewModel<TransmissionScreenViewModel>()
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
    ) { p ->
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.align(Alignment.TopCenter))
            {
                when(p)
                {
                    0 -> TransmissionVideo(navigator = navigator, transmissionScreenViewModel = transmissionScreenViewModel)
                    1 -> TransmissionComic(navigator = navigator, transmissionScreenViewModel = transmissionScreenViewModel)
                    else -> { }
                }
            }
        }
    }
}

fun downloadToGroup(
    i: VideoDownloadItemState,
    downloads: List<VideoDownloadItemState>
): List<VideoDownloadItemState> {
    return downloads.filter { it.vid == i.vid && it.klass == i.klass }
}

fun downloadToGroup(
    klass: String, id: String,
    downloads: List<VideoDownloadItemState>
): List<VideoDownloadItemState> {
    return downloads.filter { it.vid == id && it.klass == klass }
}