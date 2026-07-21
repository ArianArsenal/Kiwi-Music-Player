package com.example.kiwi.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.kiwi.data.local.SongDao
import com.example.kiwi.data.local.toDomain
import com.example.kiwi.data.local.toEntity
import com.example.kiwi.data.paging.PlaylistPagingSource
import com.example.kiwi.data.paging.SongPagingSource
import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dao: SongDao
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
                    filter {
                        or {
                            ilike("title", "%$query%")
                            ilike("artist_name", "%$query%")
                        }
                    }
                }.decodeList<Song>()
            }
            emit(results)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Returns Flow directly from Room, mapping the inner List entities to Domain models
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
            val joinRows = withContext(Dispatchers.IO) {
                supabase.from("playlist_songs").select {
                    filter { eq("playlist_id", playlistId) }
                }.decodeList<PlaylistSongDTO>()
            }

            val songIds = joinRows.map { it.song_id }

            if (songIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            val songs = withContext(Dispatchers.IO) {
                supabase.from("songs").select {
                    filter { isIn("id", songIds) }
                }.decodeList<Song>()
            }

            emit(songs)
        } catch (e: Exception) {
            Log.e("SupabaseError", "Failed to fetch playlist songs: ${e.message}", e)
            emit(emptyList())
        }
    }

    // Returns Flow directly from Room
    override fun getDownloadedSongCount(): Flow<Int> {
        return dao.getDownloadedSongCount()
    }

    override fun getPagedFeaturedSongs(): Flow<PagingData<Song>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SongPagingSource { limit, offset ->
                    val response = supabase.postgrest["songs"]
                        .select {
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                        .decodeList<Song>()

                    response
                }
            }
        ).flow
    }

    override fun getPagedSearchSongs(query: String, filter: String): Flow<PagingData<Song>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = {
                SongPagingSource { limit, offset ->
                    if (query.isBlank()) return@SongPagingSource emptyList()

                    // If the filter is Playlists, this specific endpoint shouldn't return songs.
                    if (filter == "Playlists") return@SongPagingSource emptyList()

                    val response = supabase.postgrest["songs"]
                        .select {
                            filter {
                                // Apply the specific backend filter based on the UI chip
                                when (filter) {
                                    "Songs" -> {
                                        ilike("title", "%$query%")
                                    }
                                    "Artists" -> {
                                        ilike("artist_name", "%$query%")
                                    }
                                    else -> {
                                        // "All", "Users", or any unmapped state searches both
                                        or {
                                            ilike("title", "%$query%")
                                            ilike("artist_name", "%$query%")
                                        }
                                    }
                                }
                            }
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                        .decodeList<Song>()

                    response
                }
            }
        ).flow
    }

    override fun getPagedPlaylists(): Flow<PagingData<Playlist>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = {
                PlaylistPagingSource { limit, offset ->
                    supabase.postgrest["playlists"]
                        .select {
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                        .decodeList<Playlist>()
                }
            }
        ).flow
    }

    override fun getPagedPlaylistSongs(playlistId: String): Flow<PagingData<Song>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = {
                SongPagingSource { limit, offset ->
                    val response = supabase.postgrest["playlist_songs"]
                        .select(columns = Columns.raw("songs(*)")) {
                            filter {
                                eq("playlist_id", playlistId)
                            }
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                        .decodeList<PlaylistSongWrapper>()
                        .map { it.songs }

                    response
                }
            }
        ).flow
    }
}

@Serializable
data class PlaylistSongWrapper(
    val songs: Song
)