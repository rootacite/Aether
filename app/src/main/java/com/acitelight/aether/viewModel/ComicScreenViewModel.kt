package com.acitelight.aether.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.acitelight.aether.Global
import com.acitelight.aether.model.Comic
import com.acitelight.aether.model.ComicRecord
import com.acitelight.aether.model.ComicResponse
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.FetchManager
import com.acitelight.aether.service.MediaManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

@HiltViewModel
class ComicScreenViewModel @Inject constructor(
    val fetchManager: FetchManager,
    @ApplicationContext val context: Context,
    val mediaManager: MediaManager,
    val apiClient: ApiClient
) : ViewModel() {
    val searchFilter = mutableStateOf("")
    val comics = mutableStateListOf<Comic>()
    val included = mutableStateListOf<String>()
    val tags = mutableStateListOf<String>()

    init {
        viewModelScope.launch {
            if (Global.loggedIn) {
                val l = mediaManager.listComics()
                val m = mediaManager.queryComicInfoBulk(l)

                if (m != null) {
                    comics.addAll(m.sortedBy { it.id.toInt() }.reversed())
                    tags.addAll(m.flatMap { it.comic.tags }.groupingBy { it }.eachCount()
                        .entries.sortedByDescending { it.value }
                        .map { it.key })
                }
            }else{
                val d = File(context.getExternalFilesDir(null), "comics")
                val jn = d.listFiles()?.filter { it.isFile }?.mapNotNull {
                    val zipFile = ZipFile(it)
                    zipFile.use {
                        zf ->
                        val entry = zf.getEntry("summary.json")
                        if (entry != null) {
                            zf.getInputStream(entry).use { inputStream ->
                                val cr = Json.decodeFromString<ComicResponse>(String(inputStream.readAllBytes(), Charsets.UTF_8))
                                return@mapNotNull Comic(
                                    isLocal = true,
                                    localBase = it.path,
                                    comic = cr,
                                    id = it.nameWithoutExtension)
                            }
                        }else return@mapNotNull null
                    }
                } ?: listOf()

                comics.addAll(jn.sortedBy { it.id.toInt() }.reversed())
                tags.addAll(jn.flatMap { it.comic.tags }.groupingBy { it }.eachCount()
                        .entries.sortedByDescending { it.value }
                        .map { it.key })
            }
        }
    }

    suspend fun download(comic: Comic) {
        fetchManager.startComicDownload(comic)
    }
}