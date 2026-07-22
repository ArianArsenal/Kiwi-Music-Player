package com.example.kiwi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String, // Email is the unique identifier
    val username: String,
    val passwordHash: String,
    val isPremium: Boolean,
    val profilePicUri: String?,
    val isDarkMode: Boolean = true,
    val language: String = "en"
)