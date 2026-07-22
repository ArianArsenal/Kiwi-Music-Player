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

    // NEW: Query to check if a username is already taken
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("UPDATE users SET isPremium = :isPremium WHERE email = :email")
    suspend fun updatePremiumStatus(email: String, isPremium: Boolean)

    @Query("UPDATE users SET profilePicUri = :uri WHERE email = :email")
    suspend fun updateProfilePic(email: String, uri: String)

    @Query("UPDATE users SET isDarkMode = :isDark WHERE email = :email")
    suspend fun updateDarkMode(email: String, isDark: Boolean)

    @Query("UPDATE users SET language = :lang WHERE email = :email")
    suspend fun updateLanguage(email: String, lang: String)


    // Add this inside UserDao.kt
    @Query("SELECT * FROM users WHERE username LIKE '%' || :searchQuery || '%' AND username != :currentUsername")
    fun searchUsers(searchQuery: String, currentUsername: String): Flow<List<UserEntity>>
}