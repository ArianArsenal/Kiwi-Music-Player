package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kiwi.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class SocialViewModel @Inject constructor() : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery


    private val initialFollowedList = listOf(
        User(id = "user_hossein", username = "hossein_dev", displayName = "Hossein", followerCount = 42, isFollowing = true),
        User(id = "user_zakeri", username = "dr_zakeri", displayName = "Dr. Zakeri", followerCount = 150, isFollowing = true),
        User(id = "artist_hans", username = "hans_zimmer", displayName = "Hans Zimmer", followerCount = 98000, isFollowing = true)
    )

    private val _followedUsers = MutableStateFlow(initialFollowedList)
    val followedUsers = _followedUsers.asStateFlow()
    // Mocking the backend database of users
    private val mockUsers = listOf(
        User(
            id = "user_hossein",
            username = "hossein_dev",
            displayName = "Hossein",
            followerCount = 42,
            isFollowing = true,
            publicPlaylists = listOf(
                com.example.kiwi.domain.model.Playlist(id = "p1", name = "Coding Focus", coverImageUrl = ""),
                com.example.kiwi.domain.model.Playlist(id = "p2", name = "Gym Motivation", coverImageUrl = "")
            )
        ),
        User(
            id = "user_zakeri",
            username = "dr_zakeri",
            displayName = "Dr. Zakeri",
            followerCount = 150,
            isFollowing = false,
            publicPlaylists = listOf(
                com.example.kiwi.domain.model.Playlist(id = "p3", name = "Cloud Computing Lo-Fi", coverImageUrl = "")
            )
        )
    )

    private val _searchResults = MutableStateFlow<List<User>>(mockUsers)
    val searchResults: StateFlow<List<User>> = _searchResults

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            _followedUsers.update { currentList ->
                // In a real app, you would make an API call here.
                // For the UI, we simply remove them from the local state list.
                currentList.filterNot { it.id == userId }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = mockUsers
        } else {
            _searchResults.value = mockUsers.filter {
                it.username.contains(query, ignoreCase = true) ||
                        it.displayName.contains(query, ignoreCase = true)
            }
        }
    }

    fun toggleFollow(userId: String) {
        _searchResults.update { users ->
            users.map { user ->
                if (user.id == userId) {
                    val newIsFollowing = !user.isFollowing
                    user.copy(
                        isFollowing = newIsFollowing,
                        followerCount = if (newIsFollowing) user.followerCount + 1 else user.followerCount - 1
                    )
                } else {
                    user
                }
            }
        }
    }

    fun getUserById(userId: String): User? {
        return searchResults.value.find { it.id == userId }
    }
}