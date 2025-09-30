package com.acitelight.aether.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.KeyImage
import com.acitelight.aether.viewModel.VideoPlayerViewModel


@Composable
fun HorizontalGallery(videoPlayerViewModel: VideoPlayerViewModel) {
    val gallery by videoPlayerViewModel.currentGallery
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(gallery) { it ->
            SingleImageItem(img = it, videoPlayerViewModel.imageLoader!!)
        }
    }
}

@Composable
private fun SingleImageItem(img: KeyImage, imageLoader: ImageLoader) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(img.url)
            .memoryCacheKey(img.key)
            .diskCacheKey(img.key)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
        imageLoader = imageLoader
    )
}
