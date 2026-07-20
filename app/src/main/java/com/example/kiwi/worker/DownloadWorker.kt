package com.example.kiwi.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val songId = inputData.getString("SONG_ID") ?: return Result.failure()
        val audioUrl = inputData.getString("AUDIO_URL") ?: return Result.failure()
        val songTitle = inputData.getString("SONG_TITLE") ?: "Unknown Song"

        return withContext(Dispatchers.IO) {
            try {
                // TODO in Batch 2:
                // 1. Open HTTP connection to audioUrl
                // 2. Stream bytes to context.filesDir
                // 3. Save the new file path to the local database

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}