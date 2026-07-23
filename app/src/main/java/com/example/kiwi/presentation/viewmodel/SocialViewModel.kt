package com.example.kiwi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiwi.data.local.FollowDao
import com.example.kiwi.data.local.FollowEntity
import com.example.kiwi.data.local.UserDao
import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.model.User
import com.example.kiwi.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val followDao: FollowDao,
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val supabase: SupabaseClient // <-- NEW: Inject Supabase directly
) : ViewModel() {

    private val currentUserId = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _viewedUser = MutableStateFlow<User?>(null)
    val viewedUser: StateFlow<User?> = _viewedUser

    @OptIn(ExperimentalCoroutinesApi::class)
    val followedUsers: StateFlow<List<User>> = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else getFollowingForUser(uid)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val followers: StateFlow<List<User>> = currentUserId.flatMapLatest { uid ->
        if (uid.isBlank()) flowOf(emptyList()) else getFollowersForUser(uid)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            val entity = userDao.getUserByUsername(userId)

            // NEW: Fetch live public playlists for this specific user
            val fetchedPlaylists = try {
                supabase.postgrest["playlists"].select {
                    filter {
                        eq("user_id", userId)
                        eq("is_public", true)
                    }
                }.decodeList<Playlist>()
            } catch (e: Exception) {
                emptyList()
            }

            if (entity != null) {
                _viewedUser.value = User(
                    id = entity.username,
                    username = entity.username,
                    displayName = entity.username,
                    followerCount = 0,
                    isFollowing = false,
                    profilePicUri = entity.profilePicUri,
                    isPremium = entity.isPremium,
                    publicPlaylists = fetchedPlaylists // <-- Bind live data here
                )
            } else {
                _viewedUser.value = null
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            val uid = currentUserId.value
            userDao.searchUsers(query, uid).collect { entities ->
                _searchResults.value = entities.map { entity ->
                    User(
                        id = entity.username,
                        username = entity.username,
                        displayName = entity.username,
                        followerCount = 0,
                        isFollowing = false,
                        profilePicUri = entity.profilePicUri,
                        isPremium = entity.isPremium
                    )
                }
            }
        }
    }

    fun toggleFollow(targetUserId: String, isCurrentlyFollowing: Boolean) {
        viewModelScope.launch {
            val uid = userPreferencesRepository.userSettingsFlow.first().username ?: ""
            if (uid.isNotBlank()) {
                if (isCurrentlyFollowing) {
                    followDao.unfollowUser(followerId = uid, followedId = targetUserId)
                } else {
                    followDao.followUser(FollowEntity(followerId = uid, followedId = targetUserId))
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFollowStatus(targetUserId: String) = userPreferencesRepository.userSettingsFlow.flatMapLatest { settings ->
        val uid = settings.username ?: ""
        if (uid.isNotBlank()) followDao.isFollowing(followerId = uid, followedId = targetUserId) else flowOf(false)
    }

    fun getFollowerCount(targetUserId: String) = followDao.getFollowersCount(targetUserId)

    fun getFollowingCount(targetUserId: String) = followDao.getFollowingCount(targetUserId)

    fun getFollowersForUser(userId: String): Flow<List<User>> = userDao.getFollowersList(userId).map { entities ->
        entities.map { entity ->
            User(
                id = entity.username,
                username = entity.username,
                displayName = entity.username,
                followerCount = 0,
                isFollowing = false,
                profilePicUri = entity.profilePicUri,
                isPremium = entity.isPremium
            )
        }
    }

    fun getFollowingForUser(userId: String): Flow<List<User>> = userDao.getFollowingList(userId).map { entities ->
        entities.map { entity ->
            User(
                id = entity.username,
                username = entity.username,
                displayName = entity.username,
                followerCount = 0,
                isFollowing = true,
                profilePicUri = entity.profilePicUri,
                isPremium = entity.isPremium
            )
        }
    }
}