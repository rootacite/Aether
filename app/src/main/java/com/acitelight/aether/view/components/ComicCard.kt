package com.acitelight.aether.view.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.Comic
import com.acitelight.aether.view.pages.toHex
import com.acitelight.aether.viewModel.ComicScreenViewModel
import kotlinx.coroutines.launch


@Composable
fun ComicCard(
    comic: Comic,
    navController: NavHostController,
    comicScreenViewModel: ComicScreenViewModel
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                0.65f
            )
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    val route = "comic_grid_route/${comic.id.toHex()}"
                    navController.navigate(route)
                },
                onLongClick = {
                    comicScreenViewModel.viewModelScope.launch()
                    {
                        comicScreenViewModel.download(comic = comic)
                        Toast.makeText(
                            comicScreenViewModel.context,
                            "Start downloading ${comic.comic.comic_name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(comic.getCover(comicScreenViewModel.apiClient))
                        .memoryCacheKey("${comic.id}/cover")
                        .diskCacheKey("${comic.id}/cover")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    imageLoader = comicScreenViewModel.apiClient.getImageLoader(),
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
                {
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp),
                        fontSize = 12.sp,
                        text = "${comic.comic.list.size} Pages",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = comic.comic.comic_name,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier
                    .padding(4.dp)
                    .heightIn(min = 14.dp)
            )
            Text(
                text = "Id: ${comic.id}",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}