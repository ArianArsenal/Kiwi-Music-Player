package com.example.kiwi.domain.repository

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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kiwi_settings")

data class UserSettings(
    val isDarkMode: Boolean,
    val fontSize: Float,
    val language: String,
    val isPremium: Boolean,
    val isLoggedIn: Boolean,
    val accountEmail: String?,
    val accountPassword: String?,
    val username: String?,
    val profilePicUri: String?
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LANGUAGE = stringPreferencesKey("language")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_PASSWORD = stringPreferencesKey("account_password")
        val USERNAME = stringPreferencesKey("username")
        val PROFILE_PIC_URI = stringPreferencesKey("profile_pic_uri")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true,
                fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 16f,
                language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
                isPremium = preferences[PreferencesKeys.IS_PREMIUM] ?: false,
                isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false,
                accountEmail = preferences[PreferencesKeys.ACCOUNT_EMAIL],
                accountPassword = preferences[PreferencesKeys.ACCOUNT_PASSWORD],
                username = preferences[PreferencesKeys.USERNAME],
                profilePicUri = preferences[PreferencesKeys.PROFILE_PIC_URI]
            )
        }

    suspend fun setProfilePictureUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_PIC_URI] = uri
        }
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = lang
        }
    }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] = isPremium
        }
    }

    suspend fun setLoggedInState(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun saveAccountCredentials(email: String, password: String, username: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCOUNT_EMAIL] = email
            preferences[PreferencesKeys.ACCOUNT_PASSWORD] = password
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    // NEW: Retain core settings (language/theme) while wiping user profile specific data
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.IS_LOGGED_IN)
            preferences.remove(PreferencesKeys.IS_PREMIUM)
            preferences.remove(PreferencesKeys.PROFILE_PIC_URI)
            preferences.remove(PreferencesKeys.USERNAME)
            preferences.remove(PreferencesKeys.ACCOUNT_EMAIL)
            preferences.remove(PreferencesKeys.ACCOUNT_PASSWORD)

            preferences.remove(PreferencesKeys.IS_DARK_MODE)
            preferences.remove(PreferencesKeys.LANGUAGE)
        }
    }
}