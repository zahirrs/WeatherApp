package com.example.weatherapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs_new")
        private val LAST_ENTERED_CITY = stringPreferencesKey("LAST_ENTERED_CITY")
        private val IS_LOCATION_GRANTED = booleanPreferencesKey("IS_LOCATION_GRANTED")
    }

    val getCityName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_ENTERED_CITY] ?: ""
    }

    suspend fun saveCityName(cityName: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ENTERED_CITY] = cityName
        }
    }

    val isGranted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOCATION_GRANTED] ?: false
    }

    suspend fun saveIsGranted(isGranted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOCATION_GRANTED] = isGranted
        }
    }
}