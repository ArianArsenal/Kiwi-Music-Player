package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.remote.ChatWebSocketManager
import com.example.kiwi.domain.model.ChatMessage
import com.example.kiwi.domain.model.MessageStatus
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.ChatRepository
import com.example.kiwi.domain.repository.SongRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val songRepository: SongRepository, // <-- INJECT SONG REPOSITORY
    private val webSocketManager: ChatWebSocketManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private val currentFriendId = MutableStateFlow("")

    val messages = combine(currentUserId, currentFriendId) { uid, fid ->
        Pair(uid, fid)
    }.flatMapLatest { (uid, fid) ->
        if (uid.isBlank() || fid.isBlank()) {
            flowOf(emptyList())
        } else {
            chatRepository.getMessagesWithUser(currentUserId = uid, friendId = fid)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val inputText = MutableStateFlow("")
    val isFriendTyping = MutableStateFlow(false)

    // NEW: Real-time global song search for the bottom sheet
    private val _songSearchQuery = MutableStateFlow("")
    val songSearchQuery = _songSearchQuery.asStateFlow()

    val songSearchResults: StateFlow<List<Song>> = _songSearchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                songRepository.getFeaturedSongs() // Show featured songs by default
            } else {
                songRepository.searchSongs(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSongSearchQuery(query: String) {
        _songSearchQuery.value = query
    }

    fun setChatPartner(friendId: String) {
        currentFriendId.value = friendId
    }

    init {
        viewModelScope.launch {
            currentUserId.collect { userId ->
                if (userId.isNotBlank()) {
                    webSocketManager.connect(userId)
                }
            }
        }

        webSocketManager.onTypingStatusChange = { isFriendTypingNow ->
            isFriendTyping.value = isFriendTypingNow
        }

        viewModelScope.launch {
            combine(currentUserId, currentFriendId) { uid, fid -> Pair(uid, fid) }
                .collect { (uid, fid) ->
                    if (uid.isNotBlank() && fid.isNotBlank()) {
                        chatRepository.markMessagesAsRead(currentUserId = uid, friendId = fid)
                    }
                }
        }
    }

    fun updateInputText(text: String) {
        inputText.value = text
        if (currentFriendId.value.isNotBlank() && currentUserId.value.isNotBlank()) {
            webSocketManager.sendTypingStatus(currentUserId.value, currentFriendId.value, text.isNotEmpty())
        }
    }

    fun sendMessage() {
        val text = inputText.value.trim()
        if (text.isNotBlank() && currentFriendId.value.isNotBlank() && currentUserId.value.isNotBlank()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId.value,
                receiverId = currentFriendId.value,
                text = text,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT,
                sharedSongId = null
            )
            webSocketManager.sendMessage(message)
            viewModelScope.launch { chatRepository.saveMessage(message) }
            inputText.value = ""
        }
    }

    fun shareSong(song: Song) {
        if (currentFriendId.value.isNotBlank() && currentUserId.value.isNotBlank()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId.value,
                receiverId = currentFriendId.value,
                text = "Check out this song!",
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT,
                sharedSongId = song.id,
                sharedSongTitle = song.title,
                sharedSongArtist = song.artistName,
                sharedSongCoverUrl = song.coverImageUrl,
                sharedSongAudioUrl = song.audioUrl
            )
            viewModelScope.launch { chatRepository.saveMessage(message) }
            webSocketManager.sendMessage(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}