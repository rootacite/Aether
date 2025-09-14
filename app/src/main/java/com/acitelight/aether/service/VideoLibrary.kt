package com.acitelight.aether.service

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.acitelight.aether.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoLibrary @Inject constructor(
    @ApplicationContext private val context: Context
)  {

    var classes = mutableStateListOf<String>()
    val classesMap = mutableStateMapOf<String, SnapshotStateList<Video>>()
    val updatingMap: MutableMap<Int, Boolean> = mutableMapOf()
}