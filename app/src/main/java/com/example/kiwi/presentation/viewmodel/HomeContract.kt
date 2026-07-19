package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.domain.model.Song

// 1. The Single Source of Truth for the UI
data class HomeUiState(
    // We remove the featuredSongs list and isLoading, Paging handles this!
    val dummyState: Boolean = false // Keep a variable here just so the class isn't empty for now
)

// 2. All user actions sent TO the ViewModel
sealed class HomeIntent {
    object LoadFeaturedSongs : HomeIntent()
    data class PlaySong(val songs: List<Song>, val index: Int) : HomeIntent()
}

// 3. One-time events sent FROM the ViewModel to the UI
sealed class HomeEffect {
    data class ShowToast(val messageResId: Int) : HomeEffect()
}