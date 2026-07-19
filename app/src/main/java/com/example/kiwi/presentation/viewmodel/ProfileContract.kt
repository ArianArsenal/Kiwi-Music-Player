package com.example.kiwi.presentation.viewmodel

data class ProfileUiState(
    val downloadedCount: Int = 0,
    val followingCount: Int = 1,
    val isLoading: Boolean = false
)

sealed class ProfileIntent {
    object LoadStats : ProfileIntent()
}

sealed class ProfileEffect