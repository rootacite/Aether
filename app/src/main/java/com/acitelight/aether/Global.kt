package com.acitelight.aether

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.acitelight.aether.model.Video

object Global {
    var loggedIn by mutableStateOf(false)
    var sameClassVideos: List<Video>? = null
}