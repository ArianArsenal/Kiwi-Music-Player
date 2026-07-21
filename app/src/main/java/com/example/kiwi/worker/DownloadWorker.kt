package com.example.kiwi.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kiwi.R
import com.example.kiwi.data.local.KiwiDatabase
import com.example.kiwi.data.local.SongEntity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadWorkerEntryPoint {
        fun getDatabase(): KiwiDatabase
    }

    override suspend fun doWork(): Result {
        val songId = inputData.getString("SONG_ID") ?: return Result.failure()
        val audioUrl = inputData.getString("AUDIO_URL") ?: return Result.failure()
        val title = inputData.getString("SONG_TITLE") ?: "Unknown Song"
        val artist = inputData.getString("ARTIST_NAME") ?: "Unknown Artist"
        val coverUrl = inputData.getString("COVER_URL") ?: ""

        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "$songId.mp3")

                // HIGH SPEED BUFFER BUFFERING: Using a 8KB buffer size optimizes disk writes
                URL(audioUrl).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                val entryPoint = EntryPointAccessors.fromApplication(context, DownloadWorkerEntryPoint::class.java)
                val database = entryPoint.getDatabase()

                val downloadedSong = SongEntity(
                    id = songId,
                    title = title,
                    artistName = artist,
                    coverImageUrl = coverUrl,
                    audioUrl = file.absolutePath // Local storage file pathway
                )
                database.songDao.insertSong(downloadedSong)

                // Dispatch System Alert Notification upon successful completion
                showNotification("Download Finished", "$title is now available offline.")

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                showNotification("Download Interrupted", "Failed to download $title.")
                Result.failure()
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "kiwi_offline_downloads"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use your application's icon asset package prefix
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}