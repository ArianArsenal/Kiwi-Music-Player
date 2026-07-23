package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followUser(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followedId = :followedId")
    suspend fun unfollowUser(followerId: String, followedId: String)

    @Query("SELECT COUNT(*) FROM follows WHERE followedId = :userId")
    fun getFollowersCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCount(userId: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followedId = :followedId)")
    fun isFollowing(followerId: String, followedId: String): Flow<Boolean>
}