package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.repository.SongRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: SongRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistsEffect>()
    val effect = _effect.receiveAsFlow()

    // 1. Get current logged in user ID
    private val currentUserId = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    // 2. Fetch the master PagingData from the repository
    val pagedPlaylists: Flow<PagingData<Playlist>> = repository
        .getPagedPlaylists()
        .cachedIn(viewModelScope)

    // 3. Partition into World Music (Strips accidental quotes)
    val worldPlaylists: Flow<PagingData<Playlist>> = pagedPlaylists.map { pagingData ->
        pagingData.filter { it.category.replace("'", "") == "world" }
    }.cachedIn(viewModelScope)

    // 4. Partition into Local Music (Strips accidental quotes)
    val localPlaylists: Flow<PagingData<Playlist>> = pagedPlaylists.map { pagingData ->
        pagingData.filter { it.category.replace("'", "") == "local" }
    }.cachedIn(viewModelScope)

    // 5. Partition into Personal Playlists matching the active user (Strips accidental quotes)
    @OptIn(ExperimentalCoroutinesApi::class)
    val myPlaylists: Flow<PagingData<Playlist>> = currentUserId.flatMapLatest { uid ->
        pagedPlaylists.map { pagingData ->
            pagingData.filter { it.category.replace("'", "") == "user" && it.userId == uid }
        }
    }.cachedIn(viewModelScope)

    fun createNewPlaylist(name: String, coverBytes: ByteArray?, isPublic: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            val uid = currentUserId.value
            if (uid.isNotBlank() && name.isNotBlank()) {
                // Wait for the repository to finish uploading and inserting
                repository.createPlaylist(name, coverBytes, isPublic, uid)

                // Switch back to the Main thread to update the UI
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun processIntent(intent: PlaylistsIntent) {}
}