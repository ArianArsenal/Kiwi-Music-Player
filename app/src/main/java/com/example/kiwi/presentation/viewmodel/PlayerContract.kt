package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.domain.model.Song

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val playbackSpeed: Float = 1.0f,
    val dominantColor: Int? = null // Holds the Palette API color
)

sealed class PlayerIntent {
    data class PlaySongList(val songs: List<Song>, val startIndex: Int) : PlayerIntent()
    object TogglePlayPause : PlayerIntent()
    object SkipNext : PlayerIntent()
    object SkipPrevious : PlayerIntent()
    data class SetPlaybackSpeed(val speed: Float) : PlayerIntent()
    data class SetSleepTimer(val minutes: Int) : PlayerIntent()
    data class UpdateDominantColor(val color: Int) : PlayerIntent()
}
