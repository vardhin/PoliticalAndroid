package com.example.politicalandroid.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class PreferencesManager(private val context: Context) {
    private val gson = Gson()
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }
    
    val userData: Flow<User?> = context.dataStore.data.map { preferences ->
        preferences[USER_DATA_KEY]?.let { userJson ->
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun saveAuthData(accessToken: String, refreshToken: String, user: User) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_DATA_KEY] = gson.toJson(user)
        }
    }
    
    suspend fun updateAccessToken(accessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = accessToken
        }
    }
    
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_DATA_KEY)
        }
    }
}