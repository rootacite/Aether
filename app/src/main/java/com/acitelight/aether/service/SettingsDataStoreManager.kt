package com.acitelight.aether.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val PRIVATE_KEY = stringPreferencesKey("private_key")
        val URL_KEY = stringPreferencesKey("url")
        val CERT_KEY = stringPreferencesKey("cert")
        val USE_SELF_SIGNED_KEY = booleanPreferencesKey("use_self_signed")
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }

    val privateKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PRIVATE_KEY] ?: ""
    }

    val urlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[URL_KEY] ?: ""
    }

    val certFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CERT_KEY] ?: ""
    }

    val useSelfSignedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_SELF_SIGNED_KEY] ?: false
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun savePrivateKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PRIVATE_KEY] = key
        }
    }

    suspend fun saveUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[URL_KEY] = url
        }
    }

    suspend fun saveCert(cert: String) {
        context.dataStore.edit { preferences ->
            preferences[CERT_KEY] = cert
        }
    }

    suspend fun saveUseSelfSigned(useSelfSigned: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_SELF_SIGNED_KEY] = useSelfSigned
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}