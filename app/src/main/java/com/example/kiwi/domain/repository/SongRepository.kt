package com.example.kiwi.domain.repository

import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getFeaturedSongs(): Flow<List<Song>>

    // NEW: Function to search for specific tracks
    fun searchSongs(query: String): Flow<List<Song>>
    fun getDownloadedSongs(): Flow<List<Song>>
    suspend fun saveSongLocally(song: Song)

    fun getPlaylists(): Flow<List<Playlist>>

    fun getPlaylistSongs(playlistId: String): Flow<List<Song>>
}