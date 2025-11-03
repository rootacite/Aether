package com.acitelight.aether.view.pages.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.setFullScreen
import com.acitelight.aether.view.components.video.BiliMiniSlider
import com.acitelight.aether.view.pages.video.hexToString
import com.acitelight.aether.view.pages.video.toHex
import com.acitelight.aether.viewModel.comic.ComicGridViewModel


@Composable
fun ComicGridView(
    comicId: String,
    navController: NavHostController,
    comicGridViewModel: ComicGridViewModel = hiltViewModel<ComicGridViewModel>()
) {
    comicGridViewModel.resolve(comicId.hexToString())
    comicGridViewModel.updateProcess(comicId.hexToString()) {}
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val record by comicGridViewModel.record
    val comic by comicGridViewModel.comic

    val view = LocalView.current
    DisposableEffect(Unit) {
        setFullScreen(view, true)
        onDispose {
            val nextRoute = navController.currentBackStackEntry?.destination?.route
            if (nextRoute?.startsWith("comic_page_route") != true) {
                setFullScreen(view, false)
            }
        }
    }

    LaunchedEffect(comicGridViewModel) {
        comicGridViewModel.coverHeight = screenHeight * 0.3f
        if(comicGridViewModel.maxHeight == 0.dp)
            comicGridViewModel.maxHeight = screenHeight * 0.8f
    }

    val dens = LocalDensity.current
    val listState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y // px
                val deltaDp = with(dens) { deltaY.toDp() }

                val r = if (deltaY < 0 && comicGridViewModel.coverHeight > 0.dp) {
                    val newHeight = (comicGridViewModel.coverHeight + deltaDp).coerceIn(0.dp, comicGridViewModel.maxHeight)
                    val consumedDp = newHeight - comicGridViewModel.coverHeight
                    comicGridViewModel.coverHeight = newHeight
                    val consumedPx = with(dens) { consumedDp.toPx() }
                    Offset(0f, consumedPx)
                } else if (
                    deltaY > 0
                    && comicGridViewModel.coverHeight < comicGridViewModel.maxHeight
                    && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    ) {
                    val newHeight = (comicGridViewModel.coverHeight + deltaDp).coerceIn(0.dp, comicGridViewModel.maxHeight)
                    val consumedDp = newHeight - comicGridViewModel.coverHeight
                    comicGridViewModel.coverHeight = newHeight
                    val consumedPx = with(dens) { consumedDp.toPx() }
                    Offset(0f, consumedPx)
                } else {
                    Offset.Zero
                }
                return r
            }
        }
    }

    if (comic != null) {
        val comic = comic!!
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { comic.comic.bookmarks.size })

        Column(Modifier
            .nestedScroll(nestedScrollConnection).fillMaxSize()) {
            Box(Modifier
                .fillMaxWidth()
                .height(comicGridViewModel.coverHeight))
            {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                )
                { page ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(comic.getPage(comic.comic.bookmarks[page].page, comicGridViewModel.apiClient))
                            .memoryCacheKey("${comic.id}/${comic.comic.bookmarks[page].page}")
                            .diskCacheKey("${comic.id}/${comic.comic.bookmarks[page].page}")
                            .build(),
                        contentDescription = null,
                        imageLoader = comicGridViewModel.apiClient.getImageLoader(),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.FillWidth,
                        onSuccess = { success ->
                            val drawable = success.result.image
                            val width = drawable.width
                            val height = drawable.height
                            val aspectRatio = width.toFloat() / height.toFloat()
                            comicGridViewModel.maxHeight = min(screenWidth / aspectRatio, screenHeight * 0.8f)

                            if(comicGridViewModel.coverHeight > comicGridViewModel.maxHeight)
                                comicGridViewModel.coverHeight = comicGridViewModel.maxHeight
                        },
                    )
                }

                Box(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(50.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                )
                            )
                        )
                )

                BiliMiniSlider(
                    value = (pagerState.currentPage + 1) / pagerState.pageCount.toFloat(),
                    modifier = Modifier
                        .height(6.dp)
                        .width(100.dp)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    onValueChange = {

                    }
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            {
                item()
                {
                    Text(
                        text = comic.comic.comic_name,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp).padding(bottom = 4.dp)
                    )

                    FlowRow(
                        modifier =  Modifier.padding(horizontal =  16.dp).padding(bottom = 4.dp)
                    )
                    {
                        comic.comic.tags.take(15).forEach()
                        {
                            ic ->
                            Card(
                                Modifier.padding(1.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = ic,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    modifier = Modifier
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    Box(Modifier.fillMaxWidth())
                    {
                        Text(
                            text = "Author: ${comic.comic.author} \n${comic.comic.list.size} Pages",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 3,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp).align(Alignment.CenterStart)
                        )

                        Button(onClick = {
                            comicGridViewModel.updateProcess(comicId.hexToString())
                            {
                                if (record != null) {
                                    val route = "comic_page_route/${comic.id.toHex()}/${
                                        record!!.position
                                    }"
                                    navController.navigate(route)
                                } else {
                                    val route = "comic_page_route/${comic.id.toHex()}/${0}"
                                    navController.navigate(route)
                                }
                            }
                        }, modifier = Modifier.align(Alignment.CenterEnd))
                        {
                            Text(text = "Continue", fontSize = 16.sp)
                        }
                    }

                    HorizontalDivider(Modifier.padding(horizontal =  12.dp).padding(bottom = 4.dp), thickness = 1.5.dp)
                }

                items(comicGridViewModel.chapterList)
                { c ->
                    ChapterCard(comic, navController, c, comicGridViewModel)
                    HorizontalDivider(Modifier.padding(horizontal = 26.dp), thickness = 1.5.dp)
                }
            }
    /*
            Card(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp)
                    .padding(bottom = 20.dp)
                    .height(42.dp)
                    .clickable {
                        comicGridViewModel.updateProcess(comicId.hexToString())
                        {
                            if (record != null) {
                                val route = "comic_page_route/${comic.id.toHex()}/${
                                    record!!.position
                                }"
                                navController.navigate(route)
                            } else {
                                val route = "comic_page_route/${comic.id.toHex()}/${0}"
                                navController.navigate(route)
                            }
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            )
            {
                Box(Modifier.fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp)
                    ) {
                        if (record != null) {
                            val k = comic.getPageChapterIndex(record!!.position)

                            Text(
                                text = "Last Read Position: ${k.first.name} ${k.second}/${
                                    comic.getChapterLength(
                                        k.first.page
                                    )
                                }",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                            )
                        } else {
                            Text(
                                text = "Read from scratch",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                            )
                        }
                    }
                }
            }

     */
        }
    }
}

@Composable
fun ChapterCard(
    comic: Comic,
    navController: NavHostController,
    chapter: BookMark,
    comicGridViewModel: ComicGridViewModel = hiltViewModel<ComicGridViewModel>()
) {
    val c = chapter
    val iv = comic.getPageIndex(c.page)
    val r = comic.comic.list.subList(iv, iv + comic.getChapterLength(c.page))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.65f)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .padding(vertical = 6.dp),
        onClick = {
            val route = "comic_page_route/${comic.id.toHex()}/${comic.getPageIndex(chapter.page)}"
            navController.navigate(route)
        }
    ) {
        Column(Modifier.fillMaxWidth())
        {
            Text(
                text = chapter.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp).padding(vertical = 4.dp)
                    .background(Color.Transparent)
            )
            Text(
                text = "${comic.getChapterLength(chapter.page)} Pages",
                fontSize = 14.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(Color.Transparent)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp).padding(vertical = 4.dp)
            ) {
                items(r)
                { r ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .height(120.dp)
                            .padding(horizontal = 2.dp),
                        onClick = {
                            val route =
                                "comic_page_route/${comic.id.toHex()}/${comic.getPageIndex(r)}"
                            navController.navigate(route)
                        }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(comic.getPage(r, comicGridViewModel.apiClient))
                                .memoryCacheKey("${comic.id}/${r}")
                                .diskCacheKey("${comic.id}/${r}")
                                .build(),
                            contentDescription = null,
                            imageLoader = comicGridViewModel.apiClient.getImageLoader(),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}