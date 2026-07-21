package com.example.kiwi.domain.repository

import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.model.Song
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData



interface SongRepository {
    fun getFeaturedSongs(): Flow<List<Song>>

    // NEW: Function to search for specific tracks
    fun searchSongs(query: String): Flow<List<Song>>
    fun getDownloadedSongs(): Flow<List<Song>>
    suspend fun saveSongLocally(song: Song)

    fun getPlaylists(): Flow<List<Playlist>>

    fun getPlaylistSongs(playlistId: String): Flow<List<Song>>

    fun getDownloadedSongCount(): Flow<Int>

    fun getPagedFeaturedSongs(): Flow<PagingData<Song>>

    fun getPagedSearchSongs(query: String, filter: String): Flow<PagingData<Song>>

    fun getPagedPlaylists(): Flow<PagingData<Playlist>>
    fun getPagedPlaylistSongs(playlistId: String): Flow<PagingData<Song>>
}