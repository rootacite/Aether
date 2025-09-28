package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.Global.updateRelate
import com.acitelight.aether.model.Comic
import com.acitelight.aether.viewModel.HomeScreenViewModel

@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<HomeScreenViewModel>(),
    navController: NavHostController
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { p ->
        if (p == 0) {
            Column(Modifier.fillMaxHeight()) {
                Text(
                    text = "Videos",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Start)
                )

                HorizontalDivider(Modifier.padding(8.dp), 2.dp, DividerDefaults.color)

                LazyColumn(modifier = Modifier.fillMaxWidth())
                {
                    items(homeScreenViewModel.recentManager.recentVideo)
                    { i ->
                        MiniVideoCard(
                            modifier = Modifier
                                .padding(horizontal = 12.dp),
                            i,
                            apiClient = homeScreenViewModel.apiClient,
                            imageLoader = homeScreenViewModel.imageLoader!!
                        )
                        {
                            updateRelate(homeScreenViewModel.recentManager.recentVideo, i)

                            val playList = mutableListOf<String>()
                            val fv = homeScreenViewModel.videoLibrary.classesMap.map { it.value }
                                .flatten()

                            val group =
                                fv.filter { it.klass == i.klass && it.video.group == i.video.group }
                            for (i in group) {
                                playList.add("${i.klass}/${i.id}")
                            }

                            val route =
                                "video_player_route/${(playList.joinToString(",") + "|${i.id}").toHex()}"
                            navController.navigate(route)
                        }
                        HorizontalDivider(
                            Modifier
                                .padding(vertical = 8.dp)
                                .alpha(0.4f),
                            1.dp,
                            DividerDefaults.color
                        )
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxHeight()) {
                Text(
                    text = "Comics",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Start)
                )

                HorizontalDivider(Modifier.padding(8.dp), 2.dp, DividerDefaults.color)

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(128.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                )
                {
                    items(homeScreenViewModel.recentManager.recentComic)
                    { comic ->
                        ComicCardRecent(comic, navController, homeScreenViewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun ComicCardRecent(
    comic: Comic,
    navController: NavHostController,
    homeScreenViewModel: HomeScreenViewModel
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {
            val route = "comic_grid_route/${comic.id.toHex()}"
            navController.navigate(route)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(comic.getPage(0, homeScreenViewModel.apiClient))
                        .memoryCacheKey("${comic.id}/${0}")
                        .diskCacheKey("${comic.id}/${0}")
                        .build(),
                    contentDescription = null,
                    imageLoader = homeScreenViewModel.imageLoader!!,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
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
                        color = Color.White
                    )
                }
            }
            Text(
                text = comic.comic.comic_name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.Transparent)
                    .heightIn(48.dp)
            )
        }
    }
}