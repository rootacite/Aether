package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.acitelight.aether.ToggleFullScreen
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.viewModel.ComicGridViewModel

@Composable
fun ComicGridView(comicId: String, navController: NavHostController, comicGridViewModel: ComicGridViewModel = viewModel()) {
    comicGridViewModel.SetupClient()
    comicGridViewModel.resolve(comicId.hexToString())
    comicGridViewModel.updateProcess(comicId.hexToString()){}
    ToggleFullScreen(false)
    val colorScheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val comic by comicGridViewModel.comic
    val record by comicGridViewModel.record

    if (comic != null) {
        Column {
            Box(
                Modifier
                    .padding(horizontal = 16.dp).padding(top = 36.dp)
                    .background(colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(12.dp))
            )
            {
                Text(
                text = comic!!.comic.comic_name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(4.dp))
            }
            Box(
                Modifier
                    .padding(horizontal = 16.dp).padding(top = 4.dp)
                    .background(colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = comic!!.comic.author,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Box(
                Modifier
                    .padding(horizontal = 16.dp).padding(top = 4.dp)
                    .background(colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Tags : ${comic!!.comic.tags.joinToString(", ")}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 5,
                    modifier = Modifier.padding(4.dp)
                )
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp).clip(RoundedCornerShape(6.dp)))
            {
                items(comicGridViewModel.chapterList)
                { c ->
                    ChapterCard(comic!!, navController, c, comicGridViewModel)
                }
            }

            Box(
                Modifier
                    .padding(horizontal = 16.dp).padding(top = 6.dp).padding(bottom = 20.dp).heightIn(min = 42.dp)
                    .background(colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(12.dp))
                    .clickable{
                        comicGridViewModel.updateProcess(comicId.hexToString())
                        {
                            if(record != null) {
                                val k = comic!!.getPageChapterIndex(record!!.position)
                                val route = "comic_page_route/${"${comic!!.id}".toHex()}/${
                                    record!!.position
                                }"
                                navController.navigate(route)
                            }else
                            {
                                val route = "comic_page_route/${"${comic!!.id}".toHex()}/${0}"
                                navController.navigate(route)
                            }
                        }
                    }
            )
            {
                Row(Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 8.dp)) {
                    if(record != null)
                    {
                        val k = comic!!.getPageChapterIndex(record!!.position)

                        Text(
                            text = "Last Read Position: ${k.first.name} ${k.second}/${comic!!.getChapterLength(k.first.page)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(4.dp).weight(1f)
                        )
                    }else{
                        Text(
                            text = "Read from scratch",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(4.dp).weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterCard(comic: Comic, navController: NavHostController, chapter: BookMark, comicGridViewModel: ComicGridViewModel = viewModel())
{
    val c = chapter
    val iv = comic.getPageIndex(c.page)

    Card(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp).padding(vertical = 6.dp),
        onClick = {
            val route = "comic_page_route/${"${comic.id}".toHex()}/${comic.getPageIndex(chapter.page)}"
            navController.navigate(route)
        }
    ) {
        Column(Modifier.fillMaxWidth())
        {
            Row(Modifier.padding(6.dp))
            {
                Box(Modifier
                    .height(170.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x44FFFFFF)))
                {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(comic.getPage(c.page))
                            .memoryCacheKey("${comic.id}/${c.page}")
                            .diskCacheKey("${comic.id}/${c.page}")
                            .build(),
                        contentDescription = null,
                        imageLoader = comicGridViewModel.imageLoader!!,
                        modifier = Modifier.padding(8.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = chapter.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 5,
                        modifier = Modifier.padding(8.dp).background(Color.Transparent)
                    )
                    Text(
                        text = "${comic.getChapterLength(chapter.page)} Pages",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(8.dp).background(Color.Transparent)
                    )
                }
            }

            val r = comic.comic.list.subList(iv, iv + comic.getChapterLength(c.page))
            LazyRow(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                items(r)
                {
                    r ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .height(140.dp)
                            .padding(horizontal = 6.dp),
                        onClick = {
                            val route = "comic_page_route/${"${comic.id}".toHex()}/${comic.getPageIndex(r)}"
                            navController.navigate(route)
                        }
                    ){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(comic.getPage(r))
                                .memoryCacheKey("${comic.id}/${r}")
                                .diskCacheKey("${comic.id}/${r}")
                                .build(),
                            contentDescription = null,
                            imageLoader = comicGridViewModel.imageLoader!!,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}