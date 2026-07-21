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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SongRepository,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>()
    val effect = _effect.receiveAsFlow()

    // Expose recent history directly from Room
    val recentSearches = searchHistoryDao.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val queryFlow = MutableStateFlow("")

    val pagedSearchResults: Flow<PagingData<Song>> = queryFlow
        .debounce(500L) // Debounce operator
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isNotBlank()) {
                repository.getPagedSearchSongs(query)
            } else {
                flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    fun processIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                _uiState.update { it.copy(query = intent.query) }
                queryFlow.value = intent.query
            }
            is SearchIntent.UpdateFilter -> {
                _uiState.update { it.copy(currentFilter = intent.filter) }
            }
            is SearchIntent.DeleteHistoryItem -> {
                // FIXED: Extract the .query string from the entity
                viewModelScope.launch { searchHistoryDao.deleteSearchQuery(intent.item.query) }
            }
            is SearchIntent.SaveQuery -> {
                if (intent.query.isNotBlank()) {
                    viewModelScope.launch {
                        // FIXED: Uses the correct insert method and provides the timestamp
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