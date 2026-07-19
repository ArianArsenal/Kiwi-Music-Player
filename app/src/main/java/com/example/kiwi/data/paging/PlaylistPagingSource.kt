package com.example.kiwi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kiwi.domain.model.Playlist

class PlaylistPagingSource(
    private val fetchPlaylists: suspend (limit: Int, offset: Int) -> List<Playlist>
) : PagingSource<Int, Playlist>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Playlist> {
        val page = params.key ?: 0
        val limit = params.loadSize
        val offset = page * limit

        return try {
            val response = fetchPlaylists(limit, offset)
            LoadResult.Page(
                data = response,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.size < limit) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Playlist>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}