package com.acitelight.aether.view.pages.live

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.acitelight.aether.viewModel.live.LiveScreenViewModel

@Composable
fun LiveScreen(
    liveScreenViewModel: LiveScreenViewModel = hiltViewModel<LiveScreenViewModel>()
)
{

}