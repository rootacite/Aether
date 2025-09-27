package com.acitelight.aether.viewModel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.Global
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.FetchManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.RecentManager
import com.acitelight.aether.service.VideoLibrary
import com.tonyodev.fetch2.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@HiltViewModel
class VideoScreenViewModel @Inject constructor(
    private val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    val mediaManager: MediaManager,
    val recentManager: RecentManager,
    val videoLibrary: VideoLibrary
) : ViewModel() {
    private val _tabIndex = mutableIntStateOf(0)
    val tabIndex: State<Int> = _tabIndex
    var imageLoader: ImageLoader? = null;
    var menuVisibility = mutableStateOf(false)
    var searchFilter = mutableStateOf("")
    var doneInit = mutableStateOf(false)

    suspend fun init() {
        fetchManager.configured.filter { it }.first()

        if (Global.loggedIn) {
            videoLibrary.classes.addAll(mediaManager.listVideoKlasses())
            videoLibrary.classes.distinct()

            if(videoLibrary.classes.isEmpty())
                return

            var i = 0
            for (it in videoLibrary.classes) {
                videoLibrary.updatingMap[i++] = false
                if(!videoLibrary.classesMap.containsKey(it))
                    videoLibrary.classesMap[it] = mutableStateListOf()
            }
            videoLibrary.updatingMap[0] = true
            val vl =
                mediaManager.queryVideoBulk(videoLibrary.classes[0], mediaManager.queryVideoKlasses(videoLibrary.classes[0]))

            if (vl != null) {
                val r = vl.sortedWith(compareBy(naturalOrder()) { it.video.name })
                val existsId = videoLibrary.classesMap[videoLibrary.classes[0]]?.map { it.id }

                videoLibrary.classesMap[videoLibrary.classes[0]]?.addAll(r.filter { existsId == null || it.id !in existsId })
            }
        }
        else {
            videoLibrary.classes.add("Offline")
            videoLibrary.updatingMap[0] = true
            videoLibrary.classesMap["Offline"] = mutableStateListOf<Video>()

            val downloaded = fetchManager.getAllDownloadsAsync().filter {
                it.status == Status.COMPLETED && it.extras.getString("class", "") != "comic"
            }

            val jsonQuery = downloaded.map{ File(
                context.getExternalFilesDir(null),
                "videos/${it.extras.getString("class", "")}/${it.extras.getString("id", "")}/summary.json").readText() }
                .map {  Json.decodeFromString<Video>(it).toLocal(context.getExternalFilesDir(null)!!.path) }

            videoLibrary.classesMap[videoLibrary.classes[0]]?.addAll(jsonQuery)
        }

        doneInit.value = true
    }

    fun setTabIndex(index: Int) {
        viewModelScope.launch()
        {
            _tabIndex.intValue = index;
            if (videoLibrary.updatingMap[index] == true) return@launch

            videoLibrary.updatingMap[index] = true

            val vl = mediaManager.queryVideoBulk(
                videoLibrary.classes[index],
                mediaManager.queryVideoKlasses(videoLibrary.classes[index])
            )

            if (vl != null) {
                val r = vl.sortedWith(compareBy(naturalOrder()) { it.video.name })
                val existsId = videoLibrary.classesMap[videoLibrary.classes[index]]?.map { it.id }
                videoLibrary.classesMap[videoLibrary.classes[index]]?.addAll(r.filter { existsId == null || it.id !in existsId })
            }
        }
    }

    suspend fun download(video: Video) {
        fetchManager.startVideoDownload(video)
    }

    init {
        imageLoader = ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            init()
        }
    }
}