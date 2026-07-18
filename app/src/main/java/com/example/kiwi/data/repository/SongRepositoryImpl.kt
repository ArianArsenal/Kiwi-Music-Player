package com.example.kiwi.data.repository

import android.util.Log
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : SongRepository {

    override fun getFeaturedSongs(): Flow<List<Song>> = flow {
        try {
            // Force the network call onto the Background I/O thread
            val songs = withContext(Dispatchers.IO) {
                supabase.from("songs").select().decodeList<Song>()
            }
            emit(songs)
        } catch (e: Exception) {
            // This will print a bright red error in Logcat so we can see EXACTLY what failed
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
                    filter {
                        ilike("title", "%$query%")
                    }
                }.decodeList<Song>()
            }
            emit(results)
        } catch (e: Exception) {
            Log.e("SupabaseError", "Failed to search songs: ${e.message}", e)
            emit(emptyList())
        }
    }
}