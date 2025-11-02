package com.acitelight.aether.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.acitelight.aether.model.VideoDownloadItemState
import com.acitelight.aether.view.components.BiliMiniSlider
import com.acitelight.aether.view.components.VideoDownloadCard
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status
import java.io.File
import kotlin.comparisons.compareBy
import kotlin.comparisons.naturalOrder

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