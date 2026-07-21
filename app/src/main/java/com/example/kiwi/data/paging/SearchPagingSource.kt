package com.example.kiwi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kiwi.domain.model.Song
import kotlinx.coroutines.delay

class SearchPagingSource(
    private val query: String,
    private val filter: String,
    private val allMockSongs: List<Song> // Pass your 50 mock songs in here
) : PagingSource<Int, Song>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        return try {
            val page = params.key ?: 1
            delay(500) // Simulate network delay so the Shimmer UI shows up

            // 1. Apply the Filtering Logic
            val filteredList = allMockSongs.filter { song ->

                // First check if the text matches at all
                val matchesTitle = song.title.contains(query, ignoreCase = true)
                val matchesArtist = song.artistName.contains(query, ignoreCase = true)
                val matchesAny = matchesTitle || matchesArtist

                if (!matchesAny) return@filter false

                // Then enforce the specific chip filter rules
                when (filter) {
                    "All", "Users" -> true // Users are handled in UI, media shows if it matches
                    "Songs" -> matchesTitle // Strict match: only if the song title matches
                    "Artists" -> matchesArtist // Strict match: only if the artist matches
                    "Playlists" -> false // Since this paging source returns Songs, return false. Playlists should be searched in a different repository endpoint.
                    else -> true
                }
            }

            // 2. Paginate the filtered results
            val pageSize = params.loadSize
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, filteredList.size)

            val pagedData = if (startIndex < filteredList.size) {
                filteredList.subList(startIndex, endIndex)
            } else emptyList()

            LoadResult.Page(
                data = pagedData,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (pagedData.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? {
        return state.anchorPosition
    }
}