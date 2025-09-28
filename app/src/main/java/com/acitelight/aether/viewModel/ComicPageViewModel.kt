package com.acitelight.aether.viewModel

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicRecord
import com.acitelight.aether.model.ComicRecordDatabase
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.MediaManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicPageViewModel @Inject constructor(
    val mediaManager: MediaManager,
    @ApplicationContext private val context: Context,
    val apiClient: ApiClient
) : ViewModel()
{
    var imageLoader: ImageLoader? = null
    var comic = mutableStateOf<Comic?>(null)
    var pageList = mutableStateListOf<String>()
    var title = mutableStateOf<String>("")
    var listState: LazyListState? = null
    var showPlane = mutableStateOf(true)
    var db: ComicRecordDatabase


    init{
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(apiClient.getClient()))
            }
            .build()
        listState = LazyListState(0, 0)
        db = ComicRecordDatabase.getDatabase(context)
    }

    @Composable
    fun Resolve(id: String, page: Int)
    {
        if(comic.value != null) return
        LaunchedEffect(id, page) {
            viewModelScope.launch {
                comic.value = mediaManager.queryComicInfoSingle(id)
                comic.value?.let {
                    pageList.addAll(it.comic.list)
                    title.value = it.comic.comic_name
                    listState?.scrollToItem(index = page)
                    updateProcess(page)
                }
            }
        }
    }

    fun updateProcess(page: Int)
    {
        if(comic.value == null) return
        viewModelScope.launch {
            db.userDao().insert(ComicRecord(id = comic.value!!.id.toInt(), name = comic.value!!.comic.comic_name, position = page))
        }
    }
}