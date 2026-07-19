package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.SongDao
import com.example.kiwi.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val songDao: SongDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<DownloadsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Observe the Room database automatically
        viewModelScope.launch {
            songDao.getAllSongs().collect { songEntities ->
                // Map your Room Entities back to your standard Song model
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
        when (intent) {
            is DownloadsIntent.ChangeSortOrder -> {
                _uiState.update { state ->
                    val sortedList = when (intent.order) {
                        SortOrder.TITLE_ASC -> state.downloadedSongs.sortedBy { it.title }
                        SortOrder.ARTIST_ASC -> state.downloadedSongs.sortedBy { it.artistName }
                        SortOrder.NEWEST -> state.downloadedSongs // Assuming Room returns them in insert order
                    }
                    state.copy(sortOrder = intent.order, downloadedSongs = sortedList)
                }
            }
            is DownloadsIntent.DeleteSong -> {
                viewModelScope.launch {
                    // Assuming your SongEntity uses the ID
                    songDao.deleteSongById(intent.song.id)
                    _effect.send(DownloadsEffect.ShowToast("${intent.song.title} removed from downloads"))
                }
            }
        }
    }
}