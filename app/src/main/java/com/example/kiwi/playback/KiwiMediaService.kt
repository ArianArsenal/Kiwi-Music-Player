package com.example.kiwi.playback

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.*

@OptIn(UnstableApi::class)
class KiwiMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var crossfadeJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        // 1. Audio Focus (Ducking/Pausing for calls & notifications)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // 2. Smart Cache setup
        val cache = MediaCache.getInstance(this)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // 3. Build ExoPlayer
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(audioAttributes, true) // 'true' enables automatic Audio Focus!
            .setHandleAudioBecomingNoisy(true) // Pauses when headphones disconnect
            .build()

        mediaSession = MediaSession.Builder(this, player).build()

        // 4. Crossfade Logic Listener
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startCrossfadeMonitor()
                } else {
                    crossfadeJob?.cancel()
                }
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                // Instantly reset volume when a new track starts playing
                player.volume = 1f
            }
        })
    }

    // Coroutine that monitors the last 3 seconds of a song and fades it out
    private fun startCrossfadeMonitor() {
        crossfadeJob?.cancel()
        crossfadeJob = serviceScope.launch {
            while (isActive && player.isPlaying) {
                val duration = player.duration
                val currentPosition = player.currentPosition

                if (duration > 0 && duration - currentPosition <= 3000L) {
                    // We are in the last 3 seconds. Calculate fade volume.
                    val remainingTime = duration - currentPosition
                    val fadeVolume = (remainingTime.toFloat() / 3000f).coerceIn(0f, 1f)
                    player.volume = fadeVolume
                }
                delay(100L) // Check every 100ms
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        crossfadeJob?.cancel()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
