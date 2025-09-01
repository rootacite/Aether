package com.acitelight.aether.view

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.ToggleFullScreen
import com.acitelight.aether.viewModel.ComicPageViewModel
import kotlinx.coroutines.launch

@Composable
fun ComicPageView(comicId: String, page: String,  navController: NavHostController, comicPageViewModel: ComicPageViewModel = viewModel())
{
    comicPageViewModel.SetupClient()
    comicPageViewModel.Resolve(comicId.hexToString(), page.toInt())

    val title by comicPageViewModel.title
    val pagerState = rememberPagerState(initialPage = page.toInt(), pageCount = { comicPageViewModel.pageList.size })
    var showPlane by comicPageViewModel.showPlane

    Box()
    {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().align(Alignment.Center).background(Color.Black).clickable(){
                showPlane = !showPlane
            }
        ) {
            page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comicPageViewModel.comic!!.getPage(page))
                    .memoryCacheKey("${comicPageViewModel.comic!!.id}/${page}")
                    .diskCacheKey("${comicPageViewModel.comic!!.id}/${page}")
                    .build(),
                contentDescription = null,
                imageLoader = comicPageViewModel.imageLoader!!,
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showPlane,
            enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }),
            modifier = Modifier
                .align(Alignment.TopCenter)
        ){
            Box()
            {
                Box(modifier = Modifier.height(180.dp).align(Alignment.TopCenter).fillMaxWidth().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Transparent,
                        ))))


                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp).padding(horizontal = 12.dp)
                    .height(60.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0x90FFFFFF), shape = RoundedCornerShape(12.dp)))
                {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        modifier = Modifier.padding(8.dp).padding(horizontal = 10.dp).weight(1f).align(Alignment.CenterVertically)
                    )

                    Text(
                        text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        modifier = Modifier.padding(8.dp).widthIn(120.dp).align(Alignment.CenterVertically)
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showPlane,
            enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
        {
            Box{
                Box(modifier = Modifier.height(360.dp).align(Alignment.BottomCenter).fillMaxWidth().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.90f),
                        ))))

                LazyRow (state = comicPageViewModel.listState!!,  modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp).padding(horizontal = 12.dp)
                    .height(240.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0x90999999), shape = RoundedCornerShape(12.dp)))
                {
                    items(comicPageViewModel.pageList.size)
                    {
                            r ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 6.dp).padding(vertical = 6.dp),
                            onClick = {
                                pagerState.requestScrollToPage(page = r)
                            }
                        ){
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(comicPageViewModel.comic!!.getPage(r))
                                    .memoryCacheKey("${comicPageViewModel.comic!!.id}/${r}")
                                    .diskCacheKey("${comicPageViewModel.comic!!.id}/${r}")
                                    .build(),
                                contentDescription = null,
                                imageLoader = comicPageViewModel.imageLoader!!,
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
}