package com.acitelight.aether.viewModel

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.BookMark
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicRecord
import com.acitelight.aether.model.ComicRecordDatabase
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicGridViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val mediaManager: MediaManager
)  : ViewModel()
{
    var imageLoader: ImageLoader? = null
    var comic = mutableStateOf<Comic?>(null)
    val chapterList = mutableStateListOf<BookMark>()
    var db: ComicRecordDatabase? = null
    var record = mutableStateOf<ComicRecord?>(null)

    init {
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()
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