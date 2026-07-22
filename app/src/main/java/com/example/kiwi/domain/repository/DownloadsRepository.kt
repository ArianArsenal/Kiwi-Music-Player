package com.example.kiwi.domain.repository

import com.example.kiwi.data.local.DownloadedFileEntity
import com.example.kiwi.data.local.DownloadedFilesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadsRepository @Inject constructor(
    private val downloadedFilesDao: DownloadedFilesDao
) {
    fun getAllDownloadedFiles(accountId: String): Flow<List<DownloadedFileEntity>> {
        return downloadedFilesDao.getAllDownloadedFiles(accountId)
    }

    suspend fun saveDownloadRecord(
        songId: String,
        title: String,
        artistName: String,
        localFilePath: String,
        accountId: String
    ) {
        val entity = DownloadedFileEntity(
            songId = songId,
            title = title,
            artistName = artistName,
            localFilePath = localFilePath,
            downloadedAt = System.currentTimeMillis(),
            accountId = accountId
        )
        downloadedFilesDao.saveDownloadedFile(entity)
    }

    suspend fun removeDownloadRecord(songId: String, accountId: String) {
        downloadedFilesDao.removeDownloadedFile(songId, accountId)
    }
}