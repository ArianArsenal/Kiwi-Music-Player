package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.ChatDao
import com.example.kiwi.data.local.RecentChat
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentChats: StateFlow<List<RecentChat>> = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) {
            flowOf(emptyList())
        } else {
            chatDao.getRecentChats(uid).map { dbChats ->
                // Inject the KiwiSupport Bot at the top of the list for WebSocket testing
                val supportBot = RecentChat(
                    friendId = "KiwiSupport",
                    lastMessageText = "Always online for testing.",
                    lastMessageTime = System.currentTimeMillis(),
                    unreadCount = 0,
                    profilePicUri = null
                )

                // Filter out KiwiSupport from the DB results just in case it's already there,
                // then put our static one at the top.
                val filteredChats = dbChats.filter { it.friendId != "KiwiSupport" }
                listOf(supportBot) + filteredChats
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}