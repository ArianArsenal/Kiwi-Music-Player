package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>()
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpgradeToPremium -> {
                // Mocks a successful purchase flow
                _uiState.update { it.copy(isPremium = true) }
            }
            is ProfileIntent.ChangeAvatar -> {
                // Mocks the avatar changing by swapping the URL
                _uiState.update {
                    it.copy(avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=200&auto=format&fit=crop")
                }
            }
        }
    }
}