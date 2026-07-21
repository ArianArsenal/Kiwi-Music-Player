package com.example.kiwi.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.UserRepository
import com.example.kiwi.playback.KiwiMediaService
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()
    val isPremium = userRepository.isPremium
    private var currentPlaylist: List<Song> = emptyList()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    init {
        initializeController()
        startProgressUpdater()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, KiwiMediaService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            setupPlayerListener()
        }, ContextCompat.getMainExecutor(context)) // Runs on the main thread
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Find the original song object that contains the real audioUrl!
                val songId = mediaItem?.mediaId
                val originalSong = currentPlaylist.find { it.id == songId }

                if (originalSong != null) {
                    _uiState.update { it.copy(currentSong = originalSong) }
                }
            }
        })
    }

    // Coroutine to smoothly update the progress bar while playing
    private fun startProgressUpdater() {
        viewModelScope.launch {
            while (isActive) {
                mediaController?.let { player ->
                    if (player.isPlaying && player.duration > 0) {
                        val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                        _uiState.update { it.copy(progress = progress) }
                    }
                }
                delay(500L)
            }
        }
    }

    fun playSongList(songs: List<Song>, startIndex: Int) {
        // Sanitize data payload: Map original song collections to direct local files if cached
        val localizedSongs = songs.map { song ->
            val localFile = File(context.filesDir, "${song.id}.mp3")
            if (localFile.exists()) {
                // Re-point the internal domain object mapping direct to local disk path URI strings
                song.copy(audioUrl = Uri.fromFile(localFile).toString())
            } else {
                song
            }
        }

        currentPlaylist = localizedSongs

        val mediaItems = localizedSongs.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artistName)
                .setArtworkUri(Uri.parse(song.coverImageUrl))
                .build()

            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(Uri.parse(song.audioUrl)) // Safely parses local file paths or web links
                .setMediaMetadata(metadata)
                .build()
        }

        mediaController?.setMediaItems(mediaItems, startIndex, 0L)
        mediaController?.prepare()
        mediaController?.play()
    }

    override fun onCleared() {
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }

    private var sleepTimerJob: Job? = null

    fun processIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.PlaySongList -> playSongList(intent.songs, intent.startIndex)
            is PlayerIntent.TogglePlayPause -> {
                if (mediaController?.isPlaying == true) mediaController?.pause()
                else mediaController?.play()
            }
            is PlayerIntent.SkipNext -> mediaController?.seekToNextMediaItem()
            is PlayerIntent.SkipPrevious -> mediaController?.seekToPreviousMediaItem()
            is PlayerIntent.SetPlaybackSpeed -> setSpeed(intent.speed)
            is PlayerIntent.SetSleepTimer -> startSleepTimer(intent.minutes)
            is PlayerIntent.UpdateDominantColor -> _uiState.update { it.copy(dominantColor = intent.color) }
        }
    }

    private fun setSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    private fun startSleepTimer(minutes: Int) {
        // Cancel any existing timer before starting a new one
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L) // Convert minutes to milliseconds
                mediaController?.pause()
            }
        }
    }
}