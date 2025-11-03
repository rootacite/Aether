package com.acitelight.aether.view.pages.transmission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import com.acitelight.aether.helper.DownloadType
import com.acitelight.aether.view.components.video.BiliMiniSlider
import com.acitelight.aether.view.components.comic.ComicDownloadCard
import com.acitelight.aether.viewModel.transmission.TransmissionScreenViewModel
import com.tonyodev.fetch2.Status

@Composable
fun TransmissionComic(
    navigator: NavHostController,
    transmissionScreenViewModel: TransmissionScreenViewModel = hiltViewModel<TransmissionScreenViewModel>()
)
{
    var mutiSelection by transmissionScreenViewModel.mutiSelection
    val downloads = transmissionScreenViewModel.downloadComics

    Column(modifier = Modifier.animateContentSize())
    {
        Text(
            text = "Comic Tasks",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Start)
        )

        Text(
            text = "All: ${downloads.count { it.type == DownloadType.Comic }}",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Start),
            fontSize = 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )

        Text(
            text = "Completed: ${downloads.count { it.type == DownloadType.Comic && it.status == Status.COMPLETED }}",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Start),
            fontSize = 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )

        val downloading = downloads.filter { it.status == Status.DOWNLOADING }
        BiliMiniSlider(
            value = if (downloading.sumOf { it.totalBytes } == 0L) 1f else downloading.sumOf { it.downloadedBytes } / downloading.sumOf { it.totalBytes }
                .toFloat(),
            modifier = Modifier
                .height(6.dp)
                .align(Alignment.End)
                .fillMaxWidth(),
            onValueChange = {

            }
        )

        AnimatedVisibility(
            visible = mutiSelection,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        )
        {
            Column(Modifier.fillMaxWidth())
            {
                Button(onClick = {
                    mutiSelection = false
                    transmissionScreenViewModel.mutiSelectionListComic.forEach {
                        item -> transmissionScreenViewModel.delete(item)
                    }
                }, modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp))
                {
                    Text(text = "Delete", fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(Modifier.padding(horizontal = 8.dp).padding(vertical = 4.dp), 2.dp, DividerDefaults.color)

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(4.dp),
            verticalItemSpacing = 6.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        )
        {
            items(
                items = downloads.sortedBy { it.status == Status.COMPLETED }
                , key = { it.id }
            )
            { item ->
                ComicDownloadCard(
                    navigator = navigator,
                    viewModel = transmissionScreenViewModel,
                    model = item,
                    onPause = {
                        transmissionScreenViewModel.pause(item.id)
                    },
                    onResume = {
                        transmissionScreenViewModel.resume(item.id)
                    },
                    onRetry = {
                        transmissionScreenViewModel.retry(item.id)
                    },
                )
            }
        }
    }
}