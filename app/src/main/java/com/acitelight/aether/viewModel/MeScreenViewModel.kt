package com.acitelight.aether.viewModel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.Global
import com.acitelight.aether.dataStore
import com.acitelight.aether.model.Video
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.AuthManager
import com.acitelight.aether.service.MediaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.acitelight.aether.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class MeScreenViewModel @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val username = mutableStateOf("");
    val privateKey = mutableStateOf("")
    val url = mutableStateOf("");
    val cert = mutableStateOf("")

    val uss = settingsDataStoreManager.useSelfSignedFlow

    init {
        viewModelScope.launch {
            username.value = settingsDataStoreManager.userNameFlow.first()
            privateKey.value = if (settingsDataStoreManager.privateKeyFlow.first() == "") "" else "******"
            url.value = settingsDataStoreManager.urlFlow.first()
            cert.value = settingsDataStoreManager.certFlow.first()

            if(username.value=="" || privateKey.value=="" || url.value=="") return@launch

            try{
                val usedUrl = ApiClient.apply(context, url.value, if(uss.first()) cert.value else "")

                if (MediaManager.token == "null")
                    MediaManager.token = AuthManager.fetchToken(
                        username.value,
                        settingsDataStoreManager.privateKeyFlow.first()
                    )!!

                Global.loggedIn = true
            }catch(e: Exception)
            {
                Global.loggedIn = false
                print(e.message)
            }
        }
    }

    fun onUseSelfSignedCheckedChange(isChecked: Boolean) {
        viewModelScope.launch {
            settingsDataStoreManager.saveUseSelfSigned(isChecked)
        }
    }

    fun updateServer(u: String, c: String, context: Context)
    {
        viewModelScope.launch {
            settingsDataStoreManager.saveUrl(u)
            settingsDataStoreManager.saveCert(c)

            Global.loggedIn = false

            val us = settingsDataStoreManager.userNameFlow.first()
            val p = settingsDataStoreManager.privateKeyFlow.first()

            if (u == "" || p == "" || us == "") return@launch

            try {
                val usedUrl = ApiClient.apply(context, u, if(uss.first()) c else "")
                MediaManager.token = AuthManager.fetchToken(
                    us,
                    p
                )!!

                Global.loggedIn = true
                Toast.makeText(context, "Server Updated, Used Url: $usedUrl", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                print(e.message)
                Toast.makeText(context, "Invalid Account or Server Information", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateAccount(u: String, p: String, context: Context) {
        viewModelScope.launch {
            settingsDataStoreManager.saveUserName(u)
            settingsDataStoreManager.savePrivateKey(p)

            privateKey.value = "******"

            Global.loggedIn = false

            val u = settingsDataStoreManager.userNameFlow.first()
            val p = settingsDataStoreManager.privateKeyFlow.first()
            val ur = settingsDataStoreManager.urlFlow.first()

            if (u == "" || p == "" || ur == "") return@launch

            try {
                MediaManager.token = AuthManager.fetchToken(
                    u,
                    p
                )!!

                Global.loggedIn = true
                Toast.makeText(context, "Account Updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                print(e.message)
                Toast.makeText(context, "Invalid Account Information", Toast.LENGTH_SHORT).show()
            }
        }
    }
}