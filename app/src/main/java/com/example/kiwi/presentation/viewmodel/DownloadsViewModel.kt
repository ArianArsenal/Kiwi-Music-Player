package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.SongDao
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val songDao: SongDao,
    private val userPreferencesRepository: UserPreferencesRepository // INJECTED
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<DownloadsEffect>()
    val effect = _effect.receiveAsFlow()

    // 1. Get the active user ID
    private val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    init {
        // 2. Observe the Room database dynamically based on the active user
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch {
            currentUserId.flatMapLatest { uid ->
                if (uid.isBlank()) {
                    flowOf(emptyList())
                } else {
                    songDao.getAllSongs(uid) // MUST UPDATE DAO TO ACCEPT THIS
                }
            }.collect { songEntities ->
                val songs = songEntities.map { entity ->
                    Song(
                        id = entity.id,
                        title = entity.title,
                        artistName = entity.artistName,
                        coverImageUrl = entity.coverImageUrl,
                        audioUrl = entity.audioUrl
                    )
                }
                _uiState.update { it.copy(downloadedSongs = songs, isLoading = false) }
            }
        }
    }

    fun processIntent(intent: DownloadsIntent) {
        val uid = currentUserId.value
        when (intent) {
            is DownloadsIntent.ChangeSortOrder -> {
                _uiState.update { state ->
                    val sortedList = when (intent.order) {
                        SortOrder.TITLE_ASC -> state.downloadedSongs.sortedBy { it.title }
                        SortOrder.ARTIST_ASC -> state.downloadedSongs.sortedBy { it.artistName }
                        SortOrder.NEWEST -> state.downloadedSongs
                    }
                    state.copy(sortOrder = intent.order, downloadedSongs = sortedList)
                }
            }
            is DownloadsIntent.DeleteSong -> {
                if (uid.isNotBlank()) {
                    viewModelScope.launch {
                        songDao.deleteSongById(intent.song.id, uid) // MUST UPDATE DAO TO ACCEPT THIS
                        _effect.send(DownloadsEffect.ShowToast("${intent.song.title} removed from downloads"))
                    }
                }
            }
        }
    }
}