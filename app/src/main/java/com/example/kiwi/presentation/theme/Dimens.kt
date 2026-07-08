package com.example.kiwi.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimens(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val coverImageSize: Dp = 200.dp, // For your Now Playing screen
    val miniPlayerHeight: Dp = 64.dp
)

val LocalDimens = compositionLocalOf { Dimens() }