package com.acitelight.aether.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComicScreenViewModel : ViewModel()
{
    private val _comics = MutableStateFlow<List<Comic>>(emptyList())
    val comics: StateFlow<List<Comic>> = _comics

    init
    {
      //  viewModelScope.launch {
      //      val l = MediaManager.listComics()
       //     _comics.value = l.map { MediaManager.queryComicInfo(it) }
       // }
    }
}