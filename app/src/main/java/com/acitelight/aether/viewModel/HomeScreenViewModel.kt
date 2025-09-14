package com.acitelight.aether.viewModel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.RecentManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.acitelight.aether.service.*
import dagger.hilt.android.lifecycle.HiltViewModel


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val recentManager: RecentManager
) : ViewModel()
{
    var _init = false
    var imageLoader: ImageLoader? = null;

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
                recentManager.Query(context)
            }
        }
    }
}