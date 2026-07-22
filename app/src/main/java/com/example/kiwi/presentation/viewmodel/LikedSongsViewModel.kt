package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.LikedSongsRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val likedSongsRepository: LikedSongsRepository,
    private val userPreferencesRepository: UserPreferencesRepository // INJECTED
) : ViewModel() {

    private val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    // Automatically observes Room but only fetches for the active user
    @OptIn(ExperimentalCoroutinesApi::class)
    val likedSongs = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) {
            flowOf(emptyList())
        } else {
            likedSongsRepository.getAllLikedSongs(uid)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun removeLikedSong(songId: String) {
        viewModelScope.launch {
            val uid = currentUserId.value
            if (uid.isNotBlank()) {
                likedSongsRepository.toggleLikeState(
                    songId = songId,
                    title = "",
                    artistName = "",
                    coverImageUrl = "",
                    isCurrentlyLiked = true,
                    accountId = uid // Pass active user
                )
            }
        }
    }
}