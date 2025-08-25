package com.acitelight.aether.viewModel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.Global
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import com.acitelight.aether.model.VideoQueryIndex
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.AuthManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.MediaManager.token
import com.acitelight.aether.service.RecentManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeScreenViewModel(application: Application) : AndroidViewModel(application)
{
    private val dataStore = application.dataStore
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val PRIVATE_KEY   = stringPreferencesKey("private_key")
    private val URL_KEY = stringPreferencesKey("url")
    private val CERT_KEY   = stringPreferencesKey("cert")

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }

    val privateKeyFlow: Flow<String> = dataStore.data.map {  preferences ->
        preferences[PRIVATE_KEY] ?: ""
    }

    val urlFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[URL_KEY] ?: ""
    }

    val certFlow: Flow<String> = dataStore.data.map {  preferences ->
        preferences[CERT_KEY] ?: ""
    }

    var _init = false

    @Composable
    fun Init(){
        if(_init) return
        _init = true

        val context = LocalContext.current
        remember {
            viewModelScope.launch {
                RecentManager.Query(context)
            }
        }
    }

    init {
        viewModelScope.launch {
            val u = userNameFlow.first()
            val p = privateKeyFlow.first()
            val ur = urlFlow.first()
            val c = certFlow.first()

            if(u=="" || p=="" || ur=="" || c=="") return@launch

            try{
                ApiClient.apply(ur, c)

                if (MediaManager.token == "null")
                    MediaManager.token = AuthManager.fetchToken(
                        u,
                        p
                    )!!

                Global.loggedIn = true
            }catch(e: Exception)
            {
                Global.loggedIn = false
                print(e.message)
            }
        }
    }
}