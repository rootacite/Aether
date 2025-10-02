package com.acitelight.aether.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.setFullScreen
import com.acitelight.aether.view.components.BiliMiniSlider
import com.acitelight.aether.view.components.BookmarkPop
import com.acitelight.aether.viewModel.ComicPageViewModel
import kotlinx.coroutines.launch

@Composable
fun ComicPageView(
    comicId: String,
    page: String,
    navController: NavHostController,
    comicPageViewModel: ComicPageViewModel = hiltViewModel<ComicPageViewModel>()
) {
    val colorScheme = MaterialTheme.colorScheme
    comicPageViewModel.Resolve(comicId.hexToString(), page.toInt())

    val title by comicPageViewModel.title
    val pagerState = rememberPagerState(
        initialPage = page.toInt(),
        pageCount = { comicPageViewModel.pageList.size })
    var showPlane by comicPageViewModel.showPlane
    var showBookMarkPop by remember { mutableStateOf(false) }

    comicPageViewModel.updateProcess(pagerState.currentPage)

    val comic by comicPageViewModel.comic

    val view = LocalView.current
    DisposableEffect(Unit) {
        setFullScreen(view, true)
        onDispose {

        }
    }

    comic?.let {
        Box()
        {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showPlane = !showPlane
                                if (showPlane) {
                                    comicPageViewModel.viewModelScope.launch {
                                        comicPageViewModel.listState?.scrollToItem(index = pagerState.currentPage)
                                    }
                                }
                            }
                        )
                    }
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it.getPage(page, comicPageViewModel.apiClient))
                        .memoryCacheKey("${it.id}/${page}")
                        .diskCacheKey("${it.id}/${page}")
                        .build(),
                    contentDescription = null,
                    imageLoader = comicPageViewModel.imageLoader!!,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }

            AnimatedVisibility(
                visible = showPlane,
                enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                Column(Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.9f),
                                Color.Transparent,
                            )
                        )
                    ))
                {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal =  16.dp).padding(top = 16.dp))
                    {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .padding(horizontal = 10.dp)
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        )

                        Text(
                            text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .widthIn(min = 60.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                    Box(Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp))
                    {
                        Row {
                            val k = it.getPageChapterIndex(pagerState.currentPage)
                            Text(
                                text = k.first.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .padding(horizontal = 10.dp)
                                    .align(Alignment.CenterVertically)
                            )

                            Text(
                                text = "${k.second}/${it.getChapterLength(k.first.page)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .widthIn(min = 60.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }


                        Card(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(top = 6.dp)
                                .padding(horizontal = 12.dp)
                                .height(42.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        )
                        {
                            Box(Modifier.clickable {
                                showBookMarkPop = true
                            }) {
                                Icon(
                                    Icons.Filled.Bookmarks,
                                    modifier = Modifier
                                        .padding(8.dp),
                                    contentDescription = "Bookmark"
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(64.dp))
                }
            }

            AnimatedVisibility(
                visible = showPlane,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
            {
                val k = it.getPageChapterIndex(pagerState.currentPage)
                Column(Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f),
                            )
                        )
                    )) {
                    Spacer(Modifier.height(42.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        state = comicPageViewModel.listState!!, modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 1.dp)
                            .padding(horizontal = 12.dp)
                            .height(180.dp)
                    )
                    {
                        items(comicPageViewModel.pageList.size)
                        { r ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colorScheme.primary.copy(0.8f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .wrapContentHeight()
                                    .padding(vertical = 8.dp),
                                onClick = {
                                    pagerState.requestScrollToPage(page = r)
                                }
                            ) {
                                Box(Modifier.padding(0.dp))
                                {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(it.getPage(r, comicPageViewModel.apiClient))
                                            .memoryCacheKey("${it.id}/${r}")
                                            .diskCacheKey("${it.id}/${r}")
                                            .build(),
                                        contentDescription = null,
                                        imageLoader = comicPageViewModel.imageLoader!!,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .align(Alignment.Center),
                                        contentScale = ContentScale.Fit,
                                    )
                                    val k = it.getPageChapterIndex(r)
                                    Box(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    )
                                    {
                                        Row {
                                            Text(
                                                text = "${k.second}/${it.getChapterLength(k.first.page)}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                modifier = Modifier
                                                    .padding(2.dp)
                                                    .align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BiliMiniSlider(
                        value = (k.second.toInt()) / it.getChapterLength(k.first.page).toFloat(),
                        modifier = Modifier
                            .height(6.dp)
                            .fillMaxWidth().padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        onValueChange = {

                        }
                    )


                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showBookMarkPop) {
        BookmarkPop({
            showBookMarkPop = false
        }, { s ->
            showBookMarkPop = false
            comicPageViewModel.viewModelScope.launch {
                comicPageViewModel.mediaManager.postBookmark(
                    comicId.hexToString(),
                    BookMark(name = s, page = comicPageViewModel.pageList[pagerState.currentPage])
                )
                comicPageViewModel.comic.value =
                    comicPageViewModel.mediaManager.queryComicInfoSingle(comicId.hexToString())
            }
        });
    }
}