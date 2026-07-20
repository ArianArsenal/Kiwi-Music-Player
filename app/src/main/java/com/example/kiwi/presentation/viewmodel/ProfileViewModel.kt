package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository // INJECTED REPOSITORY
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Observe the global premium status and update our local UI state
        viewModelScope.launch {
            userRepository.isPremium.collect { isPremiumGlobally ->
                _uiState.update { it.copy(isPremium = isPremiumGlobally) }
            }
        }
    }

    fun processIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpgradeToPremium -> {
                // Delegate the upgrade to the single source of truth
                userRepository.upgradeToPremium()
            }
            is ProfileIntent.ChangeAvatar -> {
                _uiState.update {
                    it.copy(avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=200&auto=format&fit=crop")
                }
            }
        }
    }
}