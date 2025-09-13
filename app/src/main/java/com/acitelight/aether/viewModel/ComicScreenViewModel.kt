package com.acitelight.aether.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicResponse
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComicScreenViewModel : ViewModel() {

    var imageLoader: ImageLoader? = null;

    val comics = mutableStateListOf<Comic>()
    val excluded = mutableStateListOf<String>()
    val included = mutableStateListOf<String>()
    val tags = mutableStateListOf<String>()
    private val counter = mutableMapOf<String, Int>()

    fun insertItem(newItem: String) {
        val newCount = (counter[newItem] ?: 0) + 1
        counter[newItem] = newCount

        if (newItem !in tags) {
            val insertIndex = tags.indexOfFirst { counter[it]!! < newCount }
                .takeIf { it >= 0 } ?: tags.size
            tags.add(insertIndex, newItem)
        } else {
            var currentIndex = tags.indexOf(newItem)
            while (currentIndex > 0 && counter[tags[currentIndex - 1]]!! < newCount) {
                tags[currentIndex] = tags[currentIndex - 1]
                tags[currentIndex - 1] = newItem
                currentIndex--
            }
        }
    }

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

    init {
        viewModelScope.launch {
            val l = MediaManager.listComics()
            val m = MediaManager.queryComicInfoBulk(l)
            if(m != null) {
                for(i in m)
                {
                    comics.add(i)
                    for(j in i.comic.tags)
                    {
                        insertItem(j)
                    }
                }
            }
        }
    }
}