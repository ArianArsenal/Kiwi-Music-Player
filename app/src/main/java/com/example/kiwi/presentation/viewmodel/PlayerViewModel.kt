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
import com.example.kiwi.domain.repository.LikedSongsRepository
import com.example.kiwi.domain.repository.UserPreferencesRepository
import com.example.kiwi.domain.repository.UserRepository
import com.example.kiwi.playback.KiwiMediaService
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val likedSongsRepository: LikedSongsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    val isPremium: StateFlow<Boolean> = userPreferencesRepository.userSettingsFlow
        .map { it.isPremium }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    // Extracted current user ID to track likes
    val currentUserId: StateFlow<String> = userPreferencesRepository.userSettingsFlow
        .map { it.username ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private var currentPlaylist: List<Song> = emptyList()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var observeLikeStatusJob: Job? = null

    init {
        initializeController()
        startProgressUpdater()

        viewModelScope.launch {
            currentUserId.collect { uid ->
                if (uid.isBlank()) {
                    mediaController?.pause()
                    _uiState.update { it.copy(currentSong = null, isPlaying = false) }
                }
            }
        }
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, KiwiMediaService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            setupPlayerListener()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun checkIfCurrentSongIsLiked(songId: String) {
        observeLikeStatusJob?.cancel()
        observeLikeStatusJob = viewModelScope.launch {
            val uid = currentUserId.value
            if (uid.isNotBlank()) {
                likedSongsRepository.isSongLiked(songId, uid).collect { isLiked ->
                    _uiState.update { it.copy(isCurrentSongLiked = isLiked) }
                }
            }
        }
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songId = mediaItem?.mediaId
                val originalSong = currentPlaylist.find { it.id == songId }

                if (originalSong != null) {
                    _uiState.update { it.copy(currentSong = originalSong) }
                    checkIfCurrentSongIsLiked(originalSong.id)
                }
            }
        })
    }

    private fun startProgressUpdater() {
        viewModelScope.launch {
            while (isActive) {
                mediaController?.let { player ->
                    // Remove the isPlaying check so the slider updates even when paused
                    if (player.duration > 0) {
                        val currentPosition = player.currentPosition
                        val duration = player.duration
                        val progress = currentPosition.toFloat() / duration.toFloat()
                        _uiState.update {
                            it.copy(
                                progress = progress,
                                currentPositionMs = currentPosition,
                                durationMs = duration
                            )
                        }
                    }
                }
                delay(500L)
            }
        }
    }

    fun playSongList(songs: List<Song>, startIndex: Int) {
        val localizedSongs = songs.map { song ->
            val localFile = File(context.filesDir, "${song.id}.mp3")
            if (localFile.exists()) {
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
                .setUri(Uri.parse(song.audioUrl))
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

            is PlayerIntent.SeekToFraction -> {
                mediaController?.let { player ->
                    val newPosition = (player.duration * intent.fraction).toLong()
                    player.seekTo(newPosition)
                    _uiState.update {
                        it.copy(
                            progress = intent.fraction,
                            currentPositionMs = newPosition
                        )
                    }
                }
            }

            is PlayerIntent.ToggleLikeCurrentSong -> {
                val currentSong = _uiState.value.currentSong
                val uid = currentUserId.value
                if (currentSong != null && uid.isNotBlank()) {
                    viewModelScope.launch {
                        likedSongsRepository.toggleLikeState(
                            songId = currentSong.id,
                            title = currentSong.title,
                            artistName = currentSong.artistName,
                            coverImageUrl = currentSong.coverImageUrl,
                            isCurrentlyLiked = _uiState.value.isCurrentSongLiked,
                            accountId = uid // Added active user ID
                        )
                    }
                }
            }
        }
    }

    private fun setSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    private fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                mediaController?.pause()
            }
        }
    }
}