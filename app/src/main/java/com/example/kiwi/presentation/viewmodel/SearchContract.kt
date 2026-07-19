package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.data.local.room.SearchHistoryEntity

data class SearchUiState(
    val query: String = "",
    val currentFilter: String = "All",
    val errorMessage: String? = null
)

sealed class SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent()
    data class UpdateFilter(val filter: String) : SearchIntent()
    data class DeleteHistoryItem(val item: SearchHistoryEntity) : SearchIntent()
    data class SaveQuery(val query: String) : SearchIntent()
}

sealed class SearchEffect {
    data class ShowToast(val messageResId: Int) : SearchEffect()
}