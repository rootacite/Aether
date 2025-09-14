package com.acitelight.aether.view

import android.nfc.Tag
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.request.ImageRequest
import com.acitelight.aether.Global
import com.acitelight.aether.model.Comic
import com.acitelight.aether.viewModel.ComicScreenViewModel
import java.nio.charset.Charset

@Composable
fun VariableGrid(
    modifier: Modifier = Modifier,
    rowHeight: Dp,
    horizontalSpacing: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    Layout(
        modifier = modifier
            .verticalScroll(scrollState),
        content = content
    ) { measurables, constraints ->

        val rowHeightPx = rowHeight.roundToPx()
        val hSpacePx = horizontalSpacing.roundToPx()
        val vSpacePx = verticalSpacing.roundToPx()

        val placeables = measurables.map { measurable ->
            measurable.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = rowHeightPx,
                    maxHeight = rowHeightPx
                )
            )
        }

        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        val maxWidth = constraints.maxWidth

        for (placeable in placeables) {
            if (currentRow.isNotEmpty() && currentWidth + placeable.width + hSpacePx > maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + hSpacePx
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val layoutHeight = if (rows.isEmpty()) {
            0
        } else {
            rows.size * rowHeightPx + (rows.size - 1) * vSpacePx
        }

        layout(
            width = constraints.maxWidth.coerceAtLeast(constraints.minWidth),
            height = layoutHeight.coerceAtLeast(constraints.minHeight)
        ) {
            var y = 0
            for (row in rows) {
                var x = 0
                for (placeable in row) {
                    placeable.placeRelative(x, y)
                    x += placeable.width + hSpacePx
                }
                y += rowHeightPx + vSpacePx
            }
        }
    }
}


@Composable
fun ComicScreen(
    navController: NavHostController,
    comicScreenViewModel: ComicScreenViewModel = hiltViewModel<ComicScreenViewModel>()
) {
    comicScreenViewModel.SetupClient()
    val included = comicScreenViewModel.included
    val state = rememberLazyGridState()
    val colorScheme = MaterialTheme.colorScheme

    Column {

        VariableGrid(
            modifier = Modifier
                .heightIn(max = 120.dp)
                .padding(8.dp),
            rowHeight = 32.dp
        )
        {
            for (i in comicScreenViewModel.tags) {

                Box(
                    Modifier
                        .background(
                            if (included.contains(i)) Color.Green.copy(alpha = 0.65f) else colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .height(32.dp).widthIn(max = 72.dp)
                        .clickable {
                            if (included.contains(i))
                                included.remove(i)
                            else
                                included.add(i)
                        }
                ) {
                    Text(
                        text = i,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(2.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.5.dp)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = state
        )
        {
            items(comicScreenViewModel.comics.filter { x ->
                included.all { y -> y in x.comic.tags } || included.isEmpty()
            })
            { comic ->
                ComicCard(comic, navController, comicScreenViewModel)
            }
        }
    }
}

@Composable
fun ComicCard(
    comic: Comic,
    navController: NavHostController,
    comicScreenViewModel: ComicScreenViewModel
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {
            val route = "comic_grid_route/${"${comic.id}".toHex()}"
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