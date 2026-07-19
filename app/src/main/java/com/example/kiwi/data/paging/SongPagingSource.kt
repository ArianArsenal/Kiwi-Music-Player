package com.example.kiwi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kiwi.domain.model.Song

// We pass a function that knows how to fetch data, making this highly reusable!
class SongPagingSource(
    private val fetchSongs: suspend (limit: Int, offset: Int) -> List<Song>
) : PagingSource<Int, Song>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        val page = params.key ?: 0
        val limit = params.loadSize
        val offset = page * limit

        return try {
            // Call Supabase (or any API) with our offset and limit
            val response = fetchSongs(limit, offset)

            LoadResult.Page(
                data = response,
                prevKey = if (page == 0) null else page - 1,
                // If we got fewer items than requested, we've hit the end of the database
                nextKey = if (response.size < limit) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}