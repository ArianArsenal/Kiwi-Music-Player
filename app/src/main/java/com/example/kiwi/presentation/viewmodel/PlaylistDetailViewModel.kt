package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val repository: SongRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>()
    val effect = _effect.receiveAsFlow()

    // Using flatMapLatest so it fetches the songs ONLY after the intent sets the ID
    val pagedPlaylistSongs: Flow<PagingData<Song>> = _uiState
        .map { it.playlistId }
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { playlistId ->
            repository.getPagedPlaylistSongs(playlistId)
        }
        .cachedIn(viewModelScope)

    init {
        val playlistId = savedStateHandle.get<String>("playlistId")
        if (playlistId != null) {
            processIntent(PlaylistDetailIntent.LoadPlaylist(playlistId))
        }
    }

    fun processIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.LoadPlaylist -> {
                _uiState.update { it.copy(playlistId = intent.playlistId) }
            }
        }
    }
}