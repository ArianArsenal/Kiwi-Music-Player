package com.example.kiwi.data.repository

import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor() : SongRepository {

    override fun getFeaturedSongs(): Flow<List<Song>> = flow {
        val fakeSongs = listOf(
            Song(
                id = "1",
                title = "Starboy",
                artistName = "The Weeknd",
                coverImageUrl = "https://example.com/starboy_cover.jpg",
                audioUrl = ""
            ),
            Song(
                id = "2",
                title = "Bohemian Rhapsody",
                artistName = "Queen",
                coverImageUrl = "https://example.com/queen_cover.jpg",
                audioUrl = ""
            ),
            Song(
                id = "3",
                title = "Shape of You",
                artistName = "Ed Sheeran",
                coverImageUrl = "https://example.com/shape_cover.jpg",
                audioUrl = ""
            )
        )
        emit(fakeSongs)
    }
}