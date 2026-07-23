package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.data.local.TopArtist
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.RecentlyPlayedRepository
import com.example.kiwi.domain.repository.SongRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SongRepository,
    private val recentlyPlayedRepository: RecentlyPlayedRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    private val currentUserId = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }

    val pagedSongs: Flow<PagingData<Song>> = repository
        .getPagedFeaturedSongs()
        .cachedIn(viewModelScope)

    // NEW: Real-time flows from your local listening history
    @OptIn(ExperimentalCoroutinesApi::class)
    val recentlyPlayedSongs: StateFlow<List<Song>> = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else recentlyPlayedRepository.getRecentlyPlayed(uid)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topArtists: StateFlow<List<TopArtist>> = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else recentlyPlayedRepository.getTopArtists(uid)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.PlaySong -> {
                // Playback tracking or telemetry can go here
            }
            else -> {}
        }
    }
}