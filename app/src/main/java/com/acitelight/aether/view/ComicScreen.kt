package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.acitelight.aether.model.Video
import com.acitelight.aether.viewModel.VideoScreenViewModel
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil3.request.ImageRequest
import com.acitelight.aether.Global
import com.acitelight.aether.model.Comic
import com.acitelight.aether.viewModel.ComicScreenViewModel
import java.nio.charset.Charset

@Composable
fun ComicScreen(navController: NavHostController, comicScreenViewModel: ComicScreenViewModel = viewModel())
{
    comicScreenViewModel.SetupClient()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(128.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    )
    {
        items(comicScreenViewModel.comics) { comic ->
            ComicCard(comic, navController, comicScreenViewModel)
        }
    }
}

@Composable
fun ComicCard(comic: Comic, navController: NavHostController, comicScreenViewModel: ComicScreenViewModel) {
    Card(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {
            val route = "comic_grid_route/${"${comic.id}".toHex() }"
            navController.navigate(route)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        )  {
            Box(modifier = Modifier.fillMaxSize()){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(comic.getPage(0))
                        .memoryCacheKey("${comic.id}/${0}")
                        .diskCacheKey("${comic.id}/${0}")
                        .build(),
                    contentDescription = null,
                    imageLoader = comicScreenViewModel.imageLoader!!,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )


                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background( brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f)
                            )
                        ))
                        .align(Alignment.BottomCenter))
                {
                    Text(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                        fontSize = 12.sp,
                        text = "${comic.comic.list.size} Pages",
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                }
            }
            Text(
                text = comic.comic.comic_name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.padding(8.dp).background(Color.Transparent).heightIn(48.dp)
            )
        }
    }
}