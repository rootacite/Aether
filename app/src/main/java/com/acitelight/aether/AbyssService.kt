package com.acitelight.aether

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.acitelight.aether.service.AbyssTunnelProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AbyssService : Service() {
    private val binder = AbyssServiceBinder()
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    var proxy = AbyssTunnelProxy()

    inner class AbyssServiceBinder : Binder() {
        fun getService(): AbyssService = this@AbyssService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            _isInitialized.update { true }
            proxy.start()
        }
    }
}