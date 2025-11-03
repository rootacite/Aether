package com.acitelight.aether.view.components.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.view.pages.video.formatTime


@Composable
fun MiniVideoCard(modifier: Modifier, video: Video, imageLoader: ImageLoader, apiClient: ApiClient, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth(),
        colors = CardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        onClick = onClick
    )
    {
        Row()
        {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.getCover(apiClient))
                    .memoryCacheKey("${video.klass}/${video.id}/cover")
                    .diskCacheKey("${video.klass}/${video.id}/cover")
                    .listener(
                        onStart = { },
                        onError = { _, _ -> }
                    )
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .width(128.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                imageLoader = imageLoader
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            )
            {
                Text(
                    modifier = Modifier,
                    text = video.video.name,
                    fontSize = 14.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier.weight(1f))

                Text(
                    modifier = Modifier.height(16.dp),
                    text = video.klass,
                    fontSize = 8.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    modifier = Modifier.height(16.dp),
                    text = formatTime(video.video.duration),
                    fontSize = 8.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}