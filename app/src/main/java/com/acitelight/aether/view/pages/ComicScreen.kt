package com.acitelight.aether.view.pages

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.acitelight.aether.view.components.ComicCard
import com.acitelight.aether.viewModel.ComicScreenViewModel

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
    val included = comicScreenViewModel.included
    val state = rememberLazyStaggeredGridState()
    val colorScheme = MaterialTheme.colorScheme
    var searchFilter by comicScreenViewModel.searchFilter

    Column {
        Row(
            Modifier
                .padding(4.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Comics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(36.dp)
                    .widthIn(max = 240.dp)
                    .background(colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Default.Search,
                    contentDescription = "Catalogue"
                )
                Spacer(Modifier.width(4.dp))
                BasicTextField(
                    value = searchFilter,
                    onValueChange = { searchFilter = it },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Start
                    ),
                    singleLine = true,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        VariableGrid(
            modifier = Modifier
                .heightIn(max = 88.dp)
                .padding(4.dp),
            rowHeight = 32.dp
        )
        {
            for (i in comicScreenViewModel.tags) {

                Box(
                    Modifier
                        .background(
                            if (included.contains(i)) Color.Green.copy(alpha = 0.65f) else colorScheme.surface,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .height(32.dp)
                        .widthIn(max = 72.dp)
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

        HorizontalDivider(Modifier.padding(1.dp), thickness = 1.5.dp)

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(4.dp),
            verticalItemSpacing = 6.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = comicScreenViewModel.comics
                    .filter { searchFilter.isEmpty() || searchFilter in it.comic.comic_name }
                    .filter { x ->
                        included.all { y -> y in x.comic.tags } || included.isEmpty()
                    },
                key = { it.id }
            ) { comic ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    ComicCard(comic, navController, comicScreenViewModel)
                }
            }
        }
    }
}
