package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.LikedSongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val likedSongsRepository: LikedSongsRepository
) : ViewModel() {

    // Automatically observes the Room database
    val likedSongs = likedSongsRepository.getAllLikedSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun removeLikedSong(songId: String) {
        viewModelScope.launch {
            // We pass 'true' for isCurrentlyLiked to force the repository to delete the record
            likedSongsRepository.toggleLikeState(
                songId = songId,
                title = "",
                artistName = "",
                coverImageUrl = "",
                isCurrentlyLiked = true
            )
        }
    }
}