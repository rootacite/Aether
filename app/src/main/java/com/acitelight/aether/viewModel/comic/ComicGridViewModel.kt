package com.acitelight.aether.viewModel.comic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicRecord
import com.acitelight.aether.model.ComicRecordDatabase
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicGridViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val mediaManager: MediaManager,
    val recentManager: RecentManager,
    val apiClient: ApiClient
)  : ViewModel()
{
    var coverHeight by mutableStateOf(220.dp)
    var maxHeight = 0.dp

    var comic = mutableStateOf<Comic?>(null)
    val chapterList = mutableStateListOf<BookMark>()
    var db: ComicRecordDatabase? = null
    var record = mutableStateOf<ComicRecord?>(null)

    init {
        db = try{
                ComicRecordDatabase.getDatabase(context)
            }catch (e: Exception) {
                print(e.message)
            } as ComicRecordDatabase?
    }

    fun resolve(id: String)
    {
        viewModelScope.launch {
            if(comic.value == null) {
                comic.value = mediaManager.queryComicInfoSingle(id)
                recentManager.pushComic(context, id)
                val c = comic.value!!
                for (i in c.comic.bookmarks) {
                    chapterList.add(i)
                }
            }else comic.value = mediaManager.queryComicInfoSingle(id)
        }
    }

    fun updateProcess(id: String, callback: () -> Unit)
    {
        viewModelScope.launch {
            record.value = db?.userDao()?.getById(id.toInt())
            callback()
        }
    }
}