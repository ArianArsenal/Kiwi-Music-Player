package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    // NEW: Query for unified login (checks both email and username)
    @Query("SELECT * FROM users WHERE email = :identifier OR username = :identifier LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("UPDATE users SET isPremium = :isPremium WHERE email = :email")
    suspend fun updatePremiumStatus(email: String, isPremium: Boolean)

    @Query("UPDATE users SET profilePicUri = :uri WHERE email = :email")
    suspend fun updateProfilePic(email: String, uri: String)

    @Query("UPDATE users SET isDarkMode = :isDark WHERE email = :email")
    suspend fun updateDarkMode(email: String, isDark: Boolean)

    @Query("UPDATE users SET language = :lang WHERE email = :email")
    suspend fun updateLanguage(email: String, lang: String)

    @Query("SELECT * FROM users WHERE username LIKE '%' || :searchQuery || '%' AND username != :currentUsername")
    fun searchUsers(searchQuery: String, currentUsername: String): Flow<List<UserEntity>>

    @Query("""
        SELECT users.* FROM users 
        INNER JOIN follows ON users.username = follows.followedId 
        WHERE follows.followerId = :userId
    """)
    fun getFollowingList(userId: String): Flow<List<UserEntity>>

    @Query("""
        SELECT users.* FROM users 
        INNER JOIN follows ON users.username = follows.followerId 
        WHERE follows.followedId = :userId
    """)
    fun getFollowersList(userId: String): Flow<List<UserEntity>>
}