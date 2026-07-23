package com.example.kiwi.data.local

import androidx.room.Entity

@Entity(tableName = "follows", primaryKeys = ["followerId", "followedId"])
data class FollowEntity(
    val followerId: String,
    val followedId: String
)