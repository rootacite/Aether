package com.acitelight.aether.viewModel

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MeScreenViewModel(application: Application) : AndroidViewModel(application)
{
    private val dataStore = application.dataStore
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val PRIVATE_KEY   = stringPreferencesKey("private_key")

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }

    val privateKeyFlow: Flow<String> = dataStore.data.map {  preferences ->
        preferences[PRIVATE_KEY] ?: ""
    }

    val username = mutableStateOf("");
    val privateKey = mutableStateOf("")

    init {
        viewModelScope.launch {
            username.value = userNameFlow.first()
            privateKey.value = privateKeyFlow.first()
        }
    }

    fun updateAccount(u: String, p: String)
    {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[USER_NAME_KEY] = u
                preferences[PRIVATE_KEY] = p
            }
        }
    }
}