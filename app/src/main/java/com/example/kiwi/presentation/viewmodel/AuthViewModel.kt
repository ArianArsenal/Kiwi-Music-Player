package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val isLoggedIn = userPreferencesRepository.userSettingsFlow
        .map { it.isLoggedIn }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    fun loginOrSignUp(email: String, pass: String, username: String, isLoginMode: Boolean) {
        viewModelScope.launch {
            _authError.value = null
            val settings = userPreferencesRepository.userSettingsFlow.first()

            if (isLoginMode) {
                // Validate Login against saved DataStore credentials
                if (settings.accountEmail == email && settings.accountPassword == pass) {
                    userPreferencesRepository.setLoggedInState(true)
                } else {
                    _authError.value = "Invalid email or password. User does not exist."
                }
            } else {
                // Sign Up validation: Check if email or username is already taken
                if (settings.accountEmail == email) {
                    _authError.value = "This email is already registered."
                    return@launch
                }
                if (settings.username == username) {
                    _authError.value = "This username is already taken."
                    return@launch
                }

                // If unique, save new credentials and log in
                userPreferencesRepository.saveAccountCredentials(email, pass, username)
                userPreferencesRepository.setLoggedInState(true)
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }
}