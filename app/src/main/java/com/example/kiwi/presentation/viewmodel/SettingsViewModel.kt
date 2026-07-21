package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.UserPreferencesRepository
import com.example.kiwi.domain.repository.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Converts the DataStore Flow into a StateFlow for Compose
    val settingsState: StateFlow<UserSettings> = userPreferencesRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings(
                isDarkMode = true,
                fontSize = 16f,
                language = "en",
                isPremium = false,
                isLoggedIn = false,
                accountEmail = null,    // FIXED: Added missing parameter
                accountPassword = null, // FIXED: Added missing parameter
                username = null,         // FIXED: Added missing parameter
                profilePicUri = null
            )
        )

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleDarkMode(isDark)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setFontSize(size)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(language)
        }
    }

    // Usually triggered after a successful mock payment
    fun upgradeToPremium() {
        viewModelScope.launch {
            userPreferencesRepository.setPremiumStatus(true)
        }
    }

    // Add this function inside your SettingsViewModel
    fun logOutUser() {
        viewModelScope.launch {
            userPreferencesRepository.setLoggedInState(false)
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            userPreferencesRepository.setProfilePictureUri(uri)
        }
    }
}