package com.example.kiwi.data.repository

import android.util.Log
import com.example.kiwi.data.local.SongDao
import com.example.kiwi.data.local.toDomain
import com.example.kiwi.data.local.toEntity
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.kiwi.domain.model.Playlist

class SongRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dao: SongDao // NEW: Inject the local Room DAO
) : SongRepository {

    override fun getFeaturedSongs(): Flow<List<Song>> = flow {
        try {
            val songs = withContext(Dispatchers.IO) {
                supabase.from("songs").select().decodeList<Song>()
            }
            emit(songs)
        } catch (e: Exception) {
            Log.e("SupabaseError", "Failed to fetch songs: ${e.message}", e)
            emit(emptyList())
        }
    }

    override fun searchSongs(query: String): Flow<List<Song>> = flow {
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }
        try {
            val results = withContext(Dispatchers.IO) {
                supabase.from("songs").select {
                    filter { ilike("title", "%$query%") }
                }.decodeList<Song>()
            }
            emit(results)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // NEW: Fetches automatically updating data directly from Room SQLite
    override fun getDownloadedSongs(): Flow<List<Song>> {
        return dao.getAllDownloadedSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSongLocally(song: Song) {
        withContext(Dispatchers.IO) {
            dao.insertSong(song.toEntity())
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> = flow {
        try {
            val playlists = withContext(Dispatchers.IO) {
                supabase.from("playlists").select().decodeList<Playlist>()
            }
            emit(playlists)
        } catch (e: Exception) {
            Log.e("SupabaseError", "Failed to fetch playlists: ${e.message}", e)
            emit(emptyList())
        }
    }

    override fun getPlaylistSongs(playlistId: String): Flow<List<Song>> = flow {
        try {
            // 1. Find all song IDs linked to this playlist from the join table
            val joinRows = withContext(Dispatchers.IO) {
                supabase.from("playlist_songs").select {
                    filter { eq("playlist_id", playlistId) }
                }.decodeList<PlaylistSongDTO>()
            }

            val songIds = joinRows.map { it.song_id }

            // If the playlist is empty, emit nothing and exit
            if (songIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // 2. Fetch the actual song metadata using those IDs
            val songs = withContext(Dispatchers.IO) {
                supabase.from("songs").select {
                    filter { isIn("id", songIds) } // Only grab matching IDs
                }.decodeList<Song>()
            }

            emit(songs)
        } catch (e: Exception) {
            Log.e("SupabaseError", "Failed to fetch playlist songs: ${e.message}", e)
            emit(emptyList())
        }
    }
}