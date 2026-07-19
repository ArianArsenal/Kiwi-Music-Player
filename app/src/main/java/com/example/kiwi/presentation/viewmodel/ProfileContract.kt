package com.example.kiwi.presentation.viewmodel

data class ProfileUiState(
    val name: String = "Arian", // Mock data placeholder
    val avatarUrl: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop",
    val isPremium: Boolean = false
)

sealed class ProfileIntent {
    object UpgradeToPremium : ProfileIntent()
    object ChangeAvatar : ProfileIntent()
}

sealed class ProfileEffect {
    data class ShowToast(val messageResId: Int) : ProfileEffect()
}