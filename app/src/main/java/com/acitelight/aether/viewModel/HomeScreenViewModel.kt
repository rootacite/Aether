package com.acitelight.aether.viewModel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient
import com.acitelight.aether.Global
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.AuthManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.MediaManager.token
import com.acitelight.aether.service.RecentManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.acitelight.aether.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel()
{
    var _init = false
    var imageLoader: ImageLoader? = null;
    val uss = settingsDataStoreManager.useSelfSignedFlow

    @Composable
    fun Init(){
        if(_init) return
        _init = true

        val context = LocalContext.current
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()
        remember {
            viewModelScope.launch {
                RecentManager.Query(context)
            }
        }
    }

    init {
        viewModelScope.launch {
            val u = settingsDataStoreManager.userNameFlow.first()
            val p = settingsDataStoreManager.privateKeyFlow.first()
            val ur = settingsDataStoreManager.urlFlow.first()
            val c = settingsDataStoreManager.certFlow.first()

            if(u=="" || p=="" || ur=="") return@launch

            try{
                val usedUrl = ApiClient.apply(context, ur, if(uss.first()) c else "")

                if (MediaManager.token == "null")
                    MediaManager.token = AuthManager.fetchToken(
                        u,
                        p
                    )!!

                Global.loggedIn = true
            }catch(e: Exception)
            {
                Global.loggedIn = false
                print(e.message)
            }
        }
    }
}