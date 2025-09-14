package com.acitelight.aether.viewModel

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.SettingsDataStoreManager
import com.acitelight.aether.view.hexToString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicPageViewModel @Inject constructor(
    val mediaManager: MediaManager
) : ViewModel()
{
    var imageLoader: ImageLoader? = null
    var comic = mutableStateOf<Comic?>(null)
    var pageList = mutableStateListOf<String>()
    var title = mutableStateOf<String>("")
    var listState: LazyListState? = null
    var coroutineScope: CoroutineScope? = null
    var showPlane = mutableStateOf(true)
    var db: ComicRecordDatabase? = null

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

        db = remember {
            try{
                ComicRecordDatabase.getDatabase(context)
            }catch (e: Exception) {
                print(e.message)
            } as ComicRecordDatabase?
        }
    }

    @Composable
    fun Resolve(id: String, page: Int)
    {
        if(comic.value != null) return
        LaunchedEffect(id, page) {
            coroutineScope?.launch {
                comic.value = mediaManager.queryComicInfoSingle(id)
                comic.value?.let {
                    pageList.addAll(it.comic.list)
                    title.value = it.comic.comic_name
                    listState?.scrollToItem(index = page)
                    UpdateProcess(page)
                }
            }
        }
    }

    fun UpdateProcess(page: Int)
    {
        if(comic.value == null) return
        coroutineScope?.launch {
            db?.userDao()?.insert(ComicRecord(id = comic.value!!.id.toInt(), name = comic.value!!.comic.comic_name, position = page))
        }
    }
}