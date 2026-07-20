package com.example.kiwi.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val songId = inputData.getString("SONG_ID") ?: return Result.failure()
        val audioUrl = inputData.getString("AUDIO_URL") ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                // 1. Create a reference to a local file named "{songId}.mp3"
                val file = File(context.filesDir, "$songId.mp3")

                // 2. Open a connection to the remote URL and stream the bytes
                URL(audioUrl).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                // (Optional) Here you could also insert a record into a local Room database
                // to list out "Downloaded Songs" in your Downloads Screen.

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}