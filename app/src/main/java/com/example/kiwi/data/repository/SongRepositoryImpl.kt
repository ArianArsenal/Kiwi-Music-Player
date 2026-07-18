package com.example.kiwi.data.repository

import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor() : SongRepository {

    private val allSongs = listOf(
        Song(
            id = "1",
            title = "Electronic Test Stream",
            artistName = "SoundHelix",
            coverImageUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=500&auto=format&fit=crop",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        ),
        Song(
            id = "2",
            title = "Acoustic Test Stream",
            artistName = "SoundHelix",
            coverImageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=500&auto=format&fit=crop",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        ),
        Song(
            id = "3",
            title = "Cinematic Test Stream",
            artistName = "SoundHelix",
            coverImageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=500&auto=format&fit=crop",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        )
    )

    override fun getFeaturedSongs(): Flow<List<Song>> = flow {
        emit(allSongs)
    }

    override fun searchSongs(query: String): Flow<List<Song>> = flow {
        if (query.isBlank()) {
            emit(emptyList())
        } else {
            val filteredList = allSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artistName.contains(query, ignoreCase = true)
            }
            emit(filteredList)
        }
    }
}