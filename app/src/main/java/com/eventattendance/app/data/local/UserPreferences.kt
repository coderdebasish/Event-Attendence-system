package com.eventattendance.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_OPERATOR_NAME = stringPreferencesKey("operator_name")
        val KEY_SELECTED_EVENT_ID = stringPreferencesKey("selected_event_id")
        val KEY_IS_ADMIN = booleanPreferencesKey("is_admin")
        val KEY_CUSTOM_SUPABASE_URL = stringPreferencesKey("custom_supabase_url")
        val KEY_CUSTOM_SUPABASE_ANON_KEY = stringPreferencesKey("custom_supabase_anon_key")
    }

    val operatorNameFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_OPERATOR_NAME]
        }

    val selectedEventIdFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SELECTED_EVENT_ID]
        }

    val isAdminFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_IS_ADMIN] ?: false
        }

    val customSupabaseUrlFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_CUSTOM_SUPABASE_URL]
        }

    val customSupabaseAnonKeyFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_CUSTOM_SUPABASE_ANON_KEY]
        }

    suspend fun setOperatorName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OPERATOR_NAME] = name
        }
    }

    suspend fun setSelectedEventId(eventId: String?) {
        dataStore.edit { preferences ->
            if (eventId != null) {
                preferences[KEY_SELECTED_EVENT_ID] = eventId
            } else {
                preferences.remove(KEY_SELECTED_EVENT_ID)
            }
        }
    }

    suspend fun setIsAdmin(isAdmin: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_ADMIN] = isAdmin
        }
    }

    suspend fun setCustomSupabaseCredentials(url: String?, anonKey: String?) {
        dataStore.edit { preferences ->
            if (url != null) preferences[KEY_CUSTOM_SUPABASE_URL] = url else preferences.remove(KEY_CUSTOM_SUPABASE_URL)
            if (anonKey != null) preferences[KEY_CUSTOM_SUPABASE_ANON_KEY] = anonKey else preferences.remove(KEY_CUSTOM_SUPABASE_ANON_KEY)
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_IS_ADMIN)
            preferences.remove(KEY_SELECTED_EVENT_ID)
        }
    }
}
