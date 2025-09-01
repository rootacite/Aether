package com.acitelight.aether.viewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.Comic
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ComicPageViewModel : ViewModel()
{
    var imageLoader: ImageLoader? = null
    var comic: Comic? = null
    var pageList = mutableStateListOf<String>()
    var title = mutableStateOf<String>("")
    var listState: LazyListState? = null
    var coroutineScope: CoroutineScope? = null
    var showPlane = mutableStateOf(false)

    @Composable
    fun SetupClient()
    {
        val context = LocalContext.current
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()
        listState = rememberLazyListState()
        coroutineScope = rememberCoroutineScope()
    }

    @Composable
    fun Resolve(id: String, page: Int)
    {
        if(comic != null) return
        LaunchedEffect(id, page) {
            coroutineScope?.launch {
                comic = MediaManager.queryComicInfo(id)
                comic?.let {
                    pageList.addAll(it.comic.list)
                    title.value = it.comic.comic_name
                    listState?.scrollToItem(index = page)
                }
            }
        }
    }
}