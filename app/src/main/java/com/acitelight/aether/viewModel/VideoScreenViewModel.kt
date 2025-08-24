package com.acitelight.aether.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.Global
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient
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

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos
    private val _klasses = MutableStateFlow<List<String>>(emptyList())
    val klasses: StateFlow<List<String>> = _klasses;

    suspend fun init() {
        _klasses.value = MediaManager.listVideoKlasses()
        val p = MediaManager.listVideos(_klasses.value.first())
        _videos.value = p
    }

    fun setTabIndex(index: Int)
    {
        viewModelScope.launch()
        {
            _tabIndex.intValue = index;
            val p = MediaManager.listVideos(_klasses.value[index])
            _videos.value = p
        }
    }

    init {
        viewModelScope.launch {
            val u = userNameFlow.first()
            val p = privateKeyFlow.first()

            if(u=="" || p=="") return@launch

            try{
                if (MediaManager.token == "null")
                    MediaManager.token = AuthManager.fetchToken(
                        ApiClient.base,
                        u,
                        p
                    )!!

                init()
            }catch(e: Exception)
            {
                print(e.message)
            }
        }
    }
}