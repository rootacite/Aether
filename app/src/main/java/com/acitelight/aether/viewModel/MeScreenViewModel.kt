package com.acitelight.aether.viewModel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acitelight.aether.AetherApp
import com.acitelight.aether.Global
import com.acitelight.aether.service.ApiClient
import com.acitelight.aether.service.AuthManager
import com.acitelight.aether.service.MediaManager
import com.acitelight.aether.service.SettingsDataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MeScreenViewModel @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager,
    @ApplicationContext private val context: Context,
    val mediaManager: MediaManager,
    private val apiClient: ApiClient,
    private val authManager: AuthManager
) : ViewModel() {

    val username = mutableStateOf("")
    val privateKey = mutableStateOf("")
    val url = mutableStateOf("")
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
                apiClient.apply(context, url.value, if(uss.first()) cert.value else "")

                authManager.fetchToken(
                    username.value,
                    settingsDataStoreManager.privateKeyFlow.first()
                )!!

                Global.loggedIn = true
                withContext(Dispatchers.IO)
                {
                    (context as AetherApp).abyssService?.proxy?.config(apiClient.getBase().toUri().host!!, 4096)
                    context.abyssService?.downloader?.init()
                }
            }catch(e: Exception)
            {
                Global.loggedIn = false
                withContext(Dispatchers.IO)
                {
                    (context as AetherApp).abyssService?.downloader?.init()
                }
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onUseSelfSignedCheckedChange(isChecked: Boolean) {
        viewModelScope.launch {
            settingsDataStoreManager.saveUseSelfSigned(isChecked)
        }
    }

    fun updateServer(u: String, c: String)
    {
        viewModelScope.launch {
            settingsDataStoreManager.saveUrl(u)
            settingsDataStoreManager.saveCert(c)

            Global.loggedIn = false

            val us = settingsDataStoreManager.userNameFlow.first()
            val p = settingsDataStoreManager.privateKeyFlow.first()

            if (u == "" || p == "" || us == "") return@launch

            try {
                val usedUrl = apiClient.apply(context, u, if(uss.first()) c else "")
                (context as AetherApp).abyssService?.proxy?.config(apiClient.getBase().toUri().host!!, 4096)
                context.abyssService?.downloader?.init()
                authManager.fetchToken(
                    us,
                    p
                )!!

                Global.loggedIn = true
                Toast.makeText(context, "Server Updated, Used Url: $usedUrl", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                print(e.message)
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateAccount(u: String, p: String) {
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
                authManager.fetchToken(
                    u,
                    p
                )!!

                Global.loggedIn = true
                withContext(Dispatchers.IO)
                {
                    (context as AetherApp).abyssService?.proxy?.config(apiClient.getBase().toUri().host!!, 4096)
                    context.abyssService?.downloader?.init()
                }
                Toast.makeText(context, "Account Updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                print(e.message)
                Toast.makeText(context, "Invalid Account Information", Toast.LENGTH_SHORT).show()
            }
        }
    }
}