package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow // FIXED: Missing import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    // FIXED: The Flow import above resolves the type inference error in HomeScreen
    val pagedSongs: Flow<PagingData<Song>> = repository
        .getPagedFeaturedSongs()
        .cachedIn(viewModelScope)

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.PlaySong -> {
                // Playback tracking or telemetry can go here
            }
            // LoadFeaturedSongs intent is removed because pagedSongs loads automatically
            else -> {}
        }
    }
}