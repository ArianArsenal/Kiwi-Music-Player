package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.domain.model.Song

data class SearchUiState(
    val query: String = "",
    val errorMessage: String? = null
)

sealed class SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent()
}

sealed class SearchEffect {
    data class ShowToast(val messageResId: Int) : SearchEffect()
}