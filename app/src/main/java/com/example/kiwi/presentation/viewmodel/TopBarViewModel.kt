package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.ChatRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val currentUserId = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalUnreadCount: StateFlow<Int> = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) {
            flowOf(0)
        } else {
            chatRepository.getTotalUnreadCount(uid)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
}