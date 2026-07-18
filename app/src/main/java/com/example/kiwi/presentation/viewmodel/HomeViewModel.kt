package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _featuredSongs = MutableStateFlow<List<Song>>(emptyList())
    val featuredSongs: StateFlow<List<Song>> = _featuredSongs.asStateFlow()

    init {
        loadFeaturedSongs()
    }

    private fun loadFeaturedSongs() {
        viewModelScope.launch {
            repository.getFeaturedSongs().collect { songs ->
                _featuredSongs.value = songs
            }
        }
    }
}