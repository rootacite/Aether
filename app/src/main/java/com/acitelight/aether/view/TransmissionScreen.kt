package com.acitelight.aether.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.acitelight.aether.model.DownloadItemState
import com.acitelight.aether.viewModel.TransmissionScreenViewModel
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.DownloadBlock

@Composable
fun TransmissionScreen(transmissionScreenViewModel: TransmissionScreenViewModel = hiltViewModel<TransmissionScreenViewModel>())
{
    val downloads = transmissionScreenViewModel.downloads

    Surface(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(downloads, key = { it.id }) { item ->
                DownloadCard(
                    model = item,
                    onPause = { transmissionScreenViewModel.pause(item.id) },
                    onResume = { transmissionScreenViewModel.resume(item.id) },
                    onCancel = { transmissionScreenViewModel.cancel(item.id) },
                    onDelete = { transmissionScreenViewModel.delete(item.id, true) }
                )
            }
        }
    }
}


@Composable
private fun DownloadCard(
    model: DownloadItemState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = model.fileName, style = MaterialTheme.typography.titleMedium)
                }

                // progress percentage
                Text(text = "${model.progress}%", modifier = Modifier.padding(start = 8.dp))
            }

            // progress bar
            LinearProgressIndicator(
            progress = { model.progress.coerceIn(0, 100) / 100f },
            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            // action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (model.status) {
                    Status.DOWNLOADING -> {
                        Button(onClick = onPause) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                            Text(text = " Pause", modifier = Modifier.padding(start = 6.dp))
                        }
                        Button(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Cancel")
                            Text(text = " Cancel", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    Status.PAUSED, Status.QUEUED -> {
                        Button(onClick = onResume) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                            Text(text = " Resume", modifier = Modifier.padding(start = 6.dp))
                        }
                        Button(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Cancel")
                            Text(text = " Cancel", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    Status.COMPLETED -> {
                        Button(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            Text(text = " Delete", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    else -> {
                        // for FAILED, CANCELLED, REMOVED etc.
                        Button(onClick = onResume) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Retry")
                            Text(text = " Retry", modifier = Modifier.padding(start = 6.dp))
                        }
                        Button(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            Text(text = " Delete", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }
    }
}
