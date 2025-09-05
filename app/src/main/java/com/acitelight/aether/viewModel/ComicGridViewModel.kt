package com.acitelight.aether.viewModel

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
import kotlinx.coroutines.launch

class ComicGridViewModel : ViewModel()
{
    var imageLoader: ImageLoader? = null
    var comic = mutableStateOf<Comic?>(null)
    val chapterList = mutableStateListOf<BookMark>()
    var db: ComicRecordDatabase? = null
    var record = mutableStateOf<ComicRecord?>(null)

    @Composable
    fun SetupClient()
    {
        val context = LocalContext.current
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()
        db = remember {
            try{
                ComicRecordDatabase.getDatabase(context)
            }catch (e: Exception) {
                print(e.message)
            } as ComicRecordDatabase?
        }
    }

    fun resolve(id: String)
    {
        viewModelScope.launch {
            if(comic.value == null) {
                comic.value = MediaManager.queryComicInfo(id)
                val c = comic.value!!
                for (i in c.comic.bookmarks) {
                    chapterList.add(i)
                }
            }else comic.value = MediaManager.queryComicInfo(id)
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