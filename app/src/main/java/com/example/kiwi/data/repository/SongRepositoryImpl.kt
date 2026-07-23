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
import com.example.kiwi.domain.repository.UserPreferencesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage // <-- Correct Storage Import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val dao: SongDao,
    private val userPreferencesRepository: UserPreferencesRepository
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDownloadedSongs(): Flow<List<Song>> {
        return userPreferencesRepository.userSettingsFlow
            .map { it.username ?: "" }
            .flatMapLatest { uid ->
                if (uid.isBlank()) {
                    flowOf(emptyList())
                } else {
                    dao.getAllDownloadedSongs(uid).map { entities ->
                        entities.map { it.toDomain() }
                    }
                }
            }
    }

    override suspend fun saveSongLocally(song: Song) {
        withContext(Dispatchers.IO) {
            userPreferencesRepository.userSettingsFlow.collect { settings ->
                val uid = settings.username
                if (!uid.isNullOrBlank()) {
                    dao.insertSong(song.toEntity(accountId = uid))
                }
            }
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

    override suspend fun createPlaylist(name: String, coverBytes: ByteArray?, isPublic: Boolean, userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val newId = java.util.UUID.randomUUID().toString()

                var uploadedUrl = "https://picsum.photos/200"

                if (coverBytes != null) {
                    val path = "$newId.jpg"
                    val bucket = supabase.storage.from("kiwi_covers")

                    // Upload the bytes (UUID guarantees uniqueness)
                    bucket.upload(path, coverBytes)

                    // Retrieve the public URL for the database
                    uploadedUrl = bucket.publicUrl(path)
                }

                val newPlaylist = Playlist(
                    id = newId,
                    name = name,
                    coverImageUrl = uploadedUrl,
                    userId = userId,
                    category = "user",
                    isPublic = isPublic
                )

                supabase.from("playlists").insert(newPlaylist)

            } catch (e: Exception) {
                Log.e("SupabaseError", "Failed to create playlist: ${e.message}", e)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDownloadedSongCount(): Flow<Int> {
        return userPreferencesRepository.userSettingsFlow
            .map { it.username ?: "" }
            .flatMapLatest { uid ->
                if (uid.isBlank()) {
                    flowOf(0)
                } else {
                    dao.getDownloadedSongCount(uid)
                }
            }
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
                    if (filter == "Playlists") return@SongPagingSource emptyList()

                    val response = supabase.postgrest["songs"]
                        .select {
                            filter {
                                when (filter) {
                                    "Songs" -> ilike("title", "%$query%")
                                    "Artists" -> ilike("artist_name", "%$query%")
                                    else -> {
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
                            filter { eq("playlist_id", playlistId) }
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                        .decodeList<PlaylistSongWrapper>()
                        .map { it.songs }

                    response
                }
            }
        ).flow
    }


    override suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        withContext(Dispatchers.IO) {
            try {
                val dto = PlaylistSongInsertDTO(playlistId = playlistId, songId = songId)
                supabase.from("playlist_songs").insert(dto)
            } catch (e: Exception) {
                Log.e("SupabaseError", "Failed to add song to playlist: ${e.message}", e)
            }
        }
    }
}

@Serializable
private data class PlaylistSongInsertDTO(
    @SerialName("playlist_id") val playlistId: String,
    @SerialName("song_id") val songId: String
)

@Serializable
data class PlaylistSongWrapper(
    val songs: Song
)