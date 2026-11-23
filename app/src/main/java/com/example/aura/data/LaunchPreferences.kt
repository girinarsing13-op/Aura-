package com.example.aura.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.launchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aura_launch_prefs"
)

class LaunchPreferences(private val context: Context) {

    private val hasSeenWelcomeKey = booleanPreferencesKey("has_seen_welcome")

    val hasSeenWelcomeFlow: Flow<Boolean> = context.launchDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> prefs[hasSeenWelcomeKey] ?: false }

    suspend fun setHasSeenWelcome(seen: Boolean) {
        context.launchDataStore.edit { prefs ->
            prefs[hasSeenWelcomeKey] = seen
        }
    }
}
