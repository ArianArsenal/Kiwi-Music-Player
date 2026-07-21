package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.remote.ChatWebSocketManager
import com.example.kiwi.domain.model.ChatMessage
import com.example.kiwi.domain.model.MessageStatus
import com.example.kiwi.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val webSocketManager: ChatWebSocketManager
) : ViewModel() {

    val currentUserId = "user_arian"

    // 1. Make the friend ID a StateFlow so Room reacts when it changes
    private val currentFriendId = MutableStateFlow("")

    // 2. Use flatMapLatest to dynamically swap the Room query when you open a new chat
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = currentFriendId.flatMapLatest { friendId ->
        if (friendId.isBlank()) {
            flowOf(emptyList())
        } else {
            // Uses the correct repository method
            chatRepository.getMessagesWithUser(friendId)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val inputText = MutableStateFlow("")
    val isFriendTyping = MutableStateFlow(false)

    fun setChatPartner(friendId: String) {
        currentFriendId.value = friendId
    }

    init {
        webSocketManager.connect(currentUserId)

        webSocketManager.onTypingStatusChange = { isFriendTypingNow ->
            isFriendTyping.value = isFriendTypingNow
        }
    }

    fun updateInputText(text: String) {
        inputText.value = text
        if (currentFriendId.value.isNotBlank()) {
            webSocketManager.sendTypingStatus(currentUserId, currentFriendId.value, text.isNotEmpty())
        }
    }

    fun sendMessage() {
        val text = inputText.value.trim()
        if (text.isNotBlank() && currentFriendId.value.isNotBlank()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = currentFriendId.value,
                text = text,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENDING,
                sharedSongId = null
            )
            webSocketManager.sendMessage(message)
            inputText.value = ""
        }
    }

    fun shareSong(songId: String) {
        if (currentFriendId.value.isNotBlank()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = currentFriendId.value,
                text = "Check out this song!",
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENDING,
                sharedSongId = songId
            )
            webSocketManager.sendMessage(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}