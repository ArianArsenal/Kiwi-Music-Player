package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<DownloadsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        processIntent(DownloadsIntent.LoadDownloads)
    }

    fun processIntent(intent: DownloadsIntent) {
        when (intent) {
            is DownloadsIntent.LoadDownloads -> loadDownloadedSongs()
        }
    }

    private fun loadDownloadedSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Fetching from local Room DB
            repository.getDownloadedSongs().collect { songs ->
                _uiState.update { it.copy(downloadedSongs = songs, isLoading = false) }
            }
        }
    }
}