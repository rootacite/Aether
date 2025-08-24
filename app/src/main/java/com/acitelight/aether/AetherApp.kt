package com.acitelight.aether

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configure")

class AetherApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}