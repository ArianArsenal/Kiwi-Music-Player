package com.example.kiwi.domain.repository

import com.example.kiwi.data.local.SearchHistoryDao
import com.example.kiwi.data.local.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepository @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) {
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentSearches()
    }

    suspend fun saveSearchQuery(query: String) {
        val entity = SearchHistoryEntity(
            query = query,
            timestamp = System.currentTimeMillis()
        )
        searchHistoryDao.insertSearchQuery(entity)
    }

    suspend fun clearHistory() {
        searchHistoryDao.clearHistory()
    }
}