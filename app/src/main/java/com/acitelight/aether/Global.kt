package com.acitelight.aether

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.acitelight.aether.model.Video

object Global {
    var loggedIn by mutableStateOf(false)
    var sameClassVideos: List<Video>? = null
    private set

    fun updateRelate(v: List<Video>, s: Video)
    {
        sameClassVideos = if (v.contains(s)) {
            val index = v.indexOf(s)
            val afterS = v.subList(index, v.size)
            val beforeS = v.subList(0, index)
            afterS + beforeS
        } else {
            v
        }
    }
}