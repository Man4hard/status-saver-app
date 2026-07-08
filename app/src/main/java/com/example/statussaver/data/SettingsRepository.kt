package com.example.statussaver.data

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val WA_TREE_URI = stringPreferencesKey("wa_tree_uri")
        val WAB_TREE_URI = stringPreferencesKey("wab_tree_uri")
    }

    val waTreeUri: Flow<Uri?> = dataStore.data.map { preferences ->
        preferences[WA_TREE_URI]?.let { Uri.parse(it) }
    }

    val wabTreeUri: Flow<Uri?> = dataStore.data.map { preferences ->
        preferences[WAB_TREE_URI]?.let { Uri.parse(it) }
    }

    suspend fun saveWaTreeUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[WA_TREE_URI] = uri
        }
    }

    suspend fun saveWabTreeUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[WAB_TREE_URI] = uri
        }
    }
}
