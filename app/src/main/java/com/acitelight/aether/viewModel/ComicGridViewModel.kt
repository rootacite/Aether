package com.acitelight.aether.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.launch

class ComicGridViewModel : ViewModel()
{
    var imageLoader: ImageLoader? = null
    var comic: Comic? = null
    val chapterList = mutableStateListOf<BookMark>()

    @Composable
    fun SetupClient()
    {
        val context = LocalContext.current
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()
    }

    fun Resolve(id: String)
    {
        if(comic != null) return
        viewModelScope.launch {
            comic = MediaManager.queryComicInfo(id)
            val c = comic!!
            for(i in c.comic.bookmarks)
            {
                chapterList.add(i)
            }
        }
    }
}