package com.acitelight.aether.view.pages

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.acitelight.aether.viewModel.LiveScreenViewModel

@Composable
fun LiveScreen(
    liveScreenViewModel: LiveScreenViewModel = hiltViewModel<LiveScreenViewModel>()
)
{

}