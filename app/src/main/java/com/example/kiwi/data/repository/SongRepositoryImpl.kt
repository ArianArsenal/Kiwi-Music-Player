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

    // NEW: Inserts the song into Room SQLite
    override suspend fun saveSongLocally(song: Song) {
        withContext(Dispatchers.IO) {
            dao.insertSong(song.toEntity())
        }
    }
}