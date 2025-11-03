package com.acitelight.aether.viewModel.live

import androidx.lifecycle.ViewModel
import com.acitelight.aether.service.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LiveScreenViewModel @Inject constructor(
    val apiClient: ApiClient
) : ViewModel(){

}