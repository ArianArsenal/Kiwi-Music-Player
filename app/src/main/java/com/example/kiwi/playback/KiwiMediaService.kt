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
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.datasource.DefaultDataSource

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

        // Use DefaultDataSource so ExoPlayer can read BOTH web links (http) AND local files (file://)
        val dataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(this)

        val cacheDataSourceFactory = androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(dataSourceFactory) // UPDATED THIS LINE
            .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // NEW: Force ExoPlayer to start playing after only 1.5 seconds of buffered data instead of 5+
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500,  // Minimum buffer required to start playback (down from 2500ms)
                50000, // Maximum buffer size
                500,   // Buffer required to resume after user pause
                1000   // Buffer required to resume after network re-buffer
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(cacheDataSourceFactory))
            .setLoadControl(loadControl) // Inject the custom load control here
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
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