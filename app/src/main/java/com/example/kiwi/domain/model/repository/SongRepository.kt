package com.example.kiwi.domain.repository

import com.example.kiwi.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getFeaturedSongs(): Flow<List<Song>>
}