package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    repository: SongRepository,
    savedStateHandle: SavedStateHandle // Intercepts the navigation arguments
) : ViewModel() {

    // Automatically extracts the ID from the NavGraph route
    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    val playlistSongs: StateFlow<List<Song>> = repository.getPlaylistSongs(playlistId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}