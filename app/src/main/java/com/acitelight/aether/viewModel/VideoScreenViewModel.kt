package com.acitelight.aether.viewModel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.Global
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.AuthManager
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class VideoScreenViewModel(application: Application) : AndroidViewModel(application)
{
    private val dataStore = application.dataStore
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val PRIVATE_KEY   = stringPreferencesKey("private_key")

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }

    val privateKeyFlow: Flow<String> = dataStore.data.map {  preferences ->
        preferences[PRIVATE_KEY] ?: ""
    }

    private val _tabIndex = mutableIntStateOf(0)
    val tabIndex: State<Int> = _tabIndex

    val videos = mutableStateListOf<Video>()
    private val _klasses = MutableStateFlow<List<String>>(emptyList())
    val klasses: StateFlow<List<String>> = _klasses;
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
        _klasses.value = MediaManager.listVideoKlasses()

        MediaManager.listVideos(_klasses.value.first()){
            v -> if(0 == tabIndex.value && !videos.contains(v)) videos.add(videos.size, v)
        }
    }

    fun setTabIndex(index: Int)
    {
        viewModelScope.launch()
        {
            _tabIndex.intValue = index;

            videos.clear()
            MediaManager.listVideos(_klasses.value[index])
            {
                v -> if(index == tabIndex.value) videos.add(videos.size, v)
            }
        }
    }

    init {
        viewModelScope.launch {
            init()
        }
    }
}