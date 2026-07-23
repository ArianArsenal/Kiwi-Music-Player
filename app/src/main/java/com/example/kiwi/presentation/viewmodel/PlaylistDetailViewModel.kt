package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val repository: SongRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>()
    val effect = _effect.receiveAsFlow()

    val currentUserId = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchSongs(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // NEW: Load the full playlist object so the UI can verify its category
    val currentPlaylist: StateFlow<Playlist?> = _uiState
        .map { it.playlistId }
        .filter { it.isNotBlank() }
        .flatMapLatest { id ->
            repository.getPlaylists().map { list -> list.find { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addSongToPlaylist(songId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val playlistId = _uiState.value.playlistId
            if (playlistId.isNotBlank()) {
                repository.addSongToPlaylist(playlistId, songId)
                onComplete()
            }
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