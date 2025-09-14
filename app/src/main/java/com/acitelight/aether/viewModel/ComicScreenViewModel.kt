package com.acitelight.aether.viewModel

import android.content.Context
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val mediaManager: MediaManager
) : ViewModel() {

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

    init {
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()

        viewModelScope.launch {
            val l = mediaManager.listComics()
            val m = mediaManager.queryComicInfoBulk(l)

            if(m != null) {
                comics.addAll(m.sortedWith(compareBy(naturalOrder()) { it.comic.comic_name }))
                tags.addAll(m.flatMap { it.comic.tags }.groupingBy { it }.eachCount()
                    .entries.sortedByDescending { it.value }
                    .map { it.key })
            }
        }
    }
}