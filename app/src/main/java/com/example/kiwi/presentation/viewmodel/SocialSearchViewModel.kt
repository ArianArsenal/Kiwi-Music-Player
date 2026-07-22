package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.UserDao
import com.example.kiwi.data.local.UserEntity
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SocialSearchViewModel @Inject constructor(
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Dynamically queries the Room database whenever the text or active user changes
    val searchResults: StateFlow<List<UserEntity>> = combine(
        _searchQuery.debounce(300L), // Wait 300ms after user stops typing
        currentUserId
    ) { query, currentUid ->
        Pair(query, currentUid)
    }.flatMapLatest { (query, currentUid) ->
        if (query.isBlank() || currentUid.isBlank()) {
            flowOf(emptyList())
        } else {
            userDao.searchUsers(query, currentUid)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}