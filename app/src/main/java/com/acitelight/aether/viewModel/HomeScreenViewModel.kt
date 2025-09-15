package com.acitelight.aether.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.service.ApiClient.createOkHttp
import com.acitelight.aether.service.RecentManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val recentManager: RecentManager,
    @ApplicationContext val context: Context
) : ViewModel()
{
    var imageLoader: ImageLoader? = null

    init{
        imageLoader =  ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(createOkHttp()))
            }
            .build()
        viewModelScope.launch {
            recentManager.queryVideo(context)
            recentManager.queryComic(context)
        }
    }
}