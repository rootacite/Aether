package com.acitelight.aether.viewModel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class VideoScreenViewModel(application: Application) : AndroidViewModel(application)
{
    private val _tabIndex = mutableIntStateOf(0)
    val tabIndex: State<Int> = _tabIndex
    // val videos = mutableStateListOf<Video>()
    // private val _klasses = MutableStateFlow<List<String>>(emptyList())
    var classes = mutableStateListOf<String>()
    val classesMap = mutableStateMapOf<String, SnapshotStateList<Video>>()

    var imageLoader: ImageLoader? = null;

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

    suspend fun init() {
        classes.addAll(MediaManager.listVideoKlasses())
        for(it in classes)
        {
            classesMap[it] = mutableStateListOf<Video>()
        }

        MediaManager.listVideos(classes[0]){
            v -> classesMap[classes[0]]?.add(v)
        }
    }

    fun setTabIndex(index: Int)
    {
        viewModelScope.launch()
        {
            _tabIndex.intValue = index;

            MediaManager.listVideos(classes[index])
            {
                v -> classesMap[classes[index]]?.add(v)
            }
        }
    }

    init {
        viewModelScope.launch {
            init()
        }
    }
}