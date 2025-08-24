package com.acitelight.aether.viewModel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.Global
import com.acitelight.aether.dataStore
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.AuthManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.MediaManager.token
import kotlinx.coroutines.launch

class HomeScreenViewModel() : ViewModel()
{

}