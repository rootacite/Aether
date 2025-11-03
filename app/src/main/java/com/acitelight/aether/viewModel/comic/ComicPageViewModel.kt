package com.acitelight.aether.viewModel.comic

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicRecord
import com.acitelight.aether.model.ComicRecordDatabase
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.MediaManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicPageViewModel @Inject constructor(
    val mediaManager: MediaManager,
    @ApplicationContext private val context: Context,
    val apiClient: ApiClient
) : ViewModel()
{
    var comic = mutableStateOf<Comic?>(null)
    var pageList = mutableStateListOf<String>()
    var title = mutableStateOf<String>("")
    var listState: LazyListState? = null
    var showPlane = mutableStateOf(true)
    var db: ComicRecordDatabase


    init{
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