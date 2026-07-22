package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.data.local.SearchHistoryDao
import com.example.kiwi.data.local.SearchHistoryEntity
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SongRepository,
    private val searchHistoryDao: SearchHistoryDao,
    private val userPreferencesRepository: UserPreferencesRepository // INJECTED
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    // Observe active user and update history accordingly
    val recentSearches = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) {
            flowOf(emptyList())
        } else {
            searchHistoryDao.getRecentSearches(uid)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pagedSearchResults: Flow<PagingData<Song>> = _uiState
        .map { Pair(it.query, it.currentFilter) }
        .debounce(500L)
        .distinctUntilChanged()
        .flatMapLatest { (query, filter) ->
            if (query.isNotBlank()) {
                repository.getPagedSearchSongs(query, filter)
            } else {
                flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    fun processIntent(intent: SearchIntent) {
        val uid = currentUserId.value
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                _uiState.update { it.copy(query = intent.query) }
            }
            is SearchIntent.UpdateFilter -> {
                _uiState.update { it.copy(currentFilter = intent.filter) }
            }
            is SearchIntent.DeleteHistoryItem -> {
                if (uid.isNotBlank()) {
                    viewModelScope.launch { searchHistoryDao.deleteSearchQuery(intent.item.query, uid) }
                }
            }
            is SearchIntent.SaveQuery -> {
                if (intent.query.isNotBlank() && uid.isNotBlank()) {
                    viewModelScope.launch {
                        searchHistoryDao.insertSearchQuery(
                            SearchHistoryEntity(
                                query = intent.query.trim(),
                                timestamp = System.currentTimeMillis(),
                                accountId = uid // Save for active user
                            )
                        )
                    }
                }
            }
        }
    }
}