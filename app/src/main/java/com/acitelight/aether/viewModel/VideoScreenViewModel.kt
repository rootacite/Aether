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
import com.acitelight.aether.helper.insertInNaturalOrder
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
    val updatingMap: MutableMap<Int, Boolean> = mutableMapOf()

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
        var i = 0
        for(it in classes)
        {
            updatingMap[i++] = false
            classesMap[it] = mutableStateListOf<Video>()
        }
        updatingMap[0] = true
        MediaManager.listVideos(classes[0], listOf()){
            v -> classesMap[classes[0]]?.insertInNaturalOrder(v)
        }
    }

    fun setTabIndex(index: Int)
    {
        viewModelScope.launch()
        {
            _tabIndex.intValue = index;
            if(updatingMap[index] == true) return@launch

            updatingMap[index] = true
            MediaManager.listVideos(classes[index], (classesMap[classes[index]]?:listOf()).map{ it.id })
            {
                v ->
                if(classesMap[classes[index]]?.contains(v) == false)
                    classesMap[classes[index]]?.insertInNaturalOrder(v)
            }
        }
    }

    init {
        viewModelScope.launch {
            init()
        }
    }
}