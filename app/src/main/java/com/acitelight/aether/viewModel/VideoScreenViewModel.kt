package com.acitelight.aether.viewModel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.FetchManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoScreenViewModel @Inject constructor(
    private val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    val mediaManager: MediaManager,
    val recentManager: RecentManager
) : ViewModel()
{
    private val _tabIndex = mutableIntStateOf(0)
    val tabIndex: State<Int> = _tabIndex
    // val videos = mutableStateListOf<Video>()
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
        classes.addAll(mediaManager.listVideoKlasses())
        var i = 0
        for(it in classes)
        {
            updatingMap[i++] = false
            classesMap[it] = mutableStateListOf<Video>()
        }
        updatingMap[0] = true
        val vl = mediaManager.queryVideoBulk(classes[0], mediaManager.queryVideoKlasses(classes[0]))

        if(vl != null){
            val r = vl.sortedWith(compareBy(naturalOrder()) { it.video.name })
            classesMap[classes[0]]?.addAll(r)
        }
    }

    fun setTabIndex(index: Int)
    {
        viewModelScope.launch()
        {
            _tabIndex.intValue = index;
            if(updatingMap[index] == true) return@launch

            updatingMap[index] = true

            val vl = mediaManager.queryVideoBulk(classes[index], mediaManager.queryVideoKlasses(classes[index]))

            if(vl != null){
                val r = vl.sortedWith(compareBy(naturalOrder()) { it.video.name })
                classesMap[classes[index]]?.addAll(r)
            }
        }
    }

    fun download(video :Video)
    {
        fetchManager.startVideoDownload(video)
    }

    init {
        viewModelScope.launch {
            init()
        }
    }
}