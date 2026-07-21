package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kiwi.data.local.SearchHistoryDao
import com.example.kiwi.data.local.SearchHistoryEntity
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
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
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    // Expose recent history directly from Room
    val recentSearches = searchHistoryDao.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Observe BOTH query and currentFilter dynamically
    val pagedSearchResults: Flow<PagingData<Song>> = _uiState
        .map { Pair(it.query, it.currentFilter) } // Trigger search when either changes
        .debounce(500L) // Wait 500ms after user stops typing/clicking
        .distinctUntilChanged()
        .flatMapLatest { (query, filter) ->
            if (query.isNotBlank()) {
                // Pass both variables to your repository
                repository.getPagedSearchSongs(query, filter)
            } else {
                flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    fun processIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                _uiState.update { it.copy(query = intent.query) }
            }
            is SearchIntent.UpdateFilter -> {
                _uiState.update { it.copy(currentFilter = intent.filter) }
            }
            is SearchIntent.DeleteHistoryItem -> {
                viewModelScope.launch { searchHistoryDao.deleteSearchQuery(intent.item.query) }
            }
            is SearchIntent.SaveQuery -> {
                if (intent.query.isNotBlank()) {
                    viewModelScope.launch {
                        searchHistoryDao.insertSearchQuery(
                            SearchHistoryEntity(
                                query = intent.query.trim(),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }
}