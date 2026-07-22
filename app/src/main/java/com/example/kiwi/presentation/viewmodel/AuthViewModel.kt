package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.UserDao
import com.example.kiwi.data.local.UserEntity
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userDao: UserDao
) : ViewModel() {

    val isLoggedIn = userPreferencesRepository.userSettingsFlow
        .map { it.isLoggedIn }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    fun loginOrSignUp(email: String, pass: String, username: String, isLoginMode: Boolean) {
        viewModelScope.launch {
            _authError.value = null

            if (isLoginMode) {
                // 1. Fetch user from Room Database
                val user = userDao.getUserByEmail(email)
                if (user != null && user.passwordHash == pass) {
                    userPreferencesRepository.saveAccountCredentials(user.email, user.passwordHash, user.username)
                    userPreferencesRepository.setPremiumStatus(user.isPremium)

                    // Restore user's specific theme and language
                    userPreferencesRepository.toggleDarkMode(user.isDarkMode)
                    userPreferencesRepository.setLanguage(user.language)

                    if (user.profilePicUri != null) {
                        userPreferencesRepository.setProfilePictureUri(user.profilePicUri)
                    }
                    userPreferencesRepository.setLoggedInState(true)
                } else {
                    _authError.value = "Invalid email or password."
                }
            } else {
                // 1. Check if email exists
                val existingEmail = userDao.getUserByEmail(email)
                if (existingEmail != null) {
                    _authError.value = "This email is already registered."
                    return@launch
                }

                // 2. Check if username exists
                val existingUsername = userDao.getUserByUsername(username)
                if (existingUsername != null) {
                    _authError.value = "This username is already taken."
                    return@launch
                }

                // 3. Create new user in Room
                val newUser = UserEntity(
                    email = email,
                    username = username,
                    passwordHash = pass,
                    isPremium = false,
                    profilePicUri = null,
                    isDarkMode = true, // Default
                    language = "en"    // Default
                )
                userDao.insertUser(newUser)

                // 4. Log them in and reset active session to defaults
                userPreferencesRepository.saveAccountCredentials(email, pass, username)
                userPreferencesRepository.setPremiumStatus(false)
                userPreferencesRepository.toggleDarkMode(true) // Ensure clean slate
                userPreferencesRepository.setLanguage("en")    // Ensure clean slate
                userPreferencesRepository.setLoggedInState(true)
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }
}