package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.UserDao
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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userDao: UserDao // <-- INJECT USER DAO
) : ViewModel() {

    val settingsState: StateFlow<UserSettings> = userPreferencesRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings(
                isDarkMode = true, fontSize = 16f, language = "en",
                isPremium = false, isLoggedIn = false,
                accountEmail = null, accountPassword = null,
                username = null, profilePicUri = null
            )
        )

    fun upgradeToPremium() {
        viewModelScope.launch {
            val email = settingsState.value.accountEmail
            if (email != null) {
                userDao.updatePremiumStatus(email, true) // Save to DB permanently
                userPreferencesRepository.setPremiumStatus(true) // Update active session
            }
        }
    }

    fun cancelPremium() {
        viewModelScope.launch {
            val email = settingsState.value.accountEmail
            if (email != null) {
                userDao.updatePremiumStatus(email, false) // Save to DB permanently
                userPreferencesRepository.setPremiumStatus(false) // Update active session
            }
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            val email = settingsState.value.accountEmail
            if (email != null) {
                userDao.updateProfilePic(email, uri) // Save to DB permanently
                userPreferencesRepository.setProfilePictureUri(uri) // Update active session
            }
        }
    }

    fun logOutUser() {
        viewModelScope.launch {
            userPreferencesRepository.clearUserSession()
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            val email = settingsState.value.accountEmail
            if (email != null) {
                userDao.updateDarkMode(email, isDark) // Save permanently
            }
            userPreferencesRepository.toggleDarkMode(isDark) // Update active session
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            val email = settingsState.value.accountEmail
            if (email != null) {
                userDao.updateLanguage(email, language) // Save permanently
            }
            userPreferencesRepository.setLanguage(language) // Update active session
        }
    }
}