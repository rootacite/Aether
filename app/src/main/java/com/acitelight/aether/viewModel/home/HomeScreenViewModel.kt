package com.acitelight.aether.viewModel.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.RecentManager
import com.acitelight.aether.service.VideoLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val recentManager: RecentManager,
    @ApplicationContext val context: Context,
    val videoLibrary: VideoLibrary,
    val apiClient: ApiClient
) : ViewModel()
{
    init{
        viewModelScope.launch {
            recentManager.queryVideo(context)
            recentManager.queryComic(context)
        }
    }
}