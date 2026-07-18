package com.example.kiwi.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.kiwi.domain.model.Song
import com.example.kiwi.domain.repository.SongRepository
import com.example.kiwi.service.KiwiPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SongRepository
) : ViewModel() {

    private var mediaController: MediaController? = null

    // NEW: We keep track of the current playlist in memory
    private var currentPlaylist: List<Song> = emptyList()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, KiwiPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                        }
                    }
                    // NEW: Automatically updates the UI when a song naturally finishes or gets skipped
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.let { item ->
                            val matchingSong = currentPlaylist.find { it.id == item.mediaId }
                            _currentSong.value = matchingSong
                        }
                    }
                })
                updateProgress()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun updateProgress() {
        viewModelScope.launch {
            while (true) {
                if (mediaController?.isPlaying == true) {
                    _currentPosition.value = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
                }
                delay(1000L)
            }
        }
    }

    // NEW: Accepts the whole list and the index of the clicked song
    fun playSongList(songs: List<Song>, startIndex: Int) {
        currentPlaylist = songs
        _currentSong.value = songs[startIndex]

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.audioUrl)
                .build()
        }

        // Loads the whole queue and starts at the chosen index
        mediaController?.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
    }

    // NEW: Skip commands for the UI buttons
    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    override fun onCleared() {
        mediaController?.release()
        super.onCleared()
    }

    fun downloadCurrentSong() {
        _currentSong.value?.let { song ->
            viewModelScope.launch {
                repository.saveSongLocally(song)
            }
        }
    }
}