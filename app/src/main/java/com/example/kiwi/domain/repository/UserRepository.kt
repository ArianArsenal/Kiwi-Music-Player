package com.example.kiwi.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    // In a real production app, this would read from DataStore or Supabase Auth.
    // For now, it defaults to false.
    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    fun upgradeToPremium() {
        _isPremium.value = true
    }
}