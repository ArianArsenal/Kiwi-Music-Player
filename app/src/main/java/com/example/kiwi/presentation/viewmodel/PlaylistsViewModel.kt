package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistsEffect>()
    val effect = _effect.receiveAsFlow()

    val pagedPlaylists: Flow<PagingData<Playlist>> = repository
        .getPagedPlaylists()
        .cachedIn(viewModelScope)

    fun processIntent(intent: PlaylistsIntent) {}
}