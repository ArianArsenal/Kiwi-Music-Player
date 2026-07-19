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
    val giant: Dp = 64.dp,

    // Component specific sizes
    val iconSize: Dp = 24.dp,
    val avatarSize: Dp = 120.dp,
    val miniPlayerHeight: Dp = 64.dp,
    val bottomNavPadding: Dp = 80.dp,
    val largeIcon: Dp = 48.dp,
    val fabSize: Dp = 72.dp,
)

// This allows us to access dimens anywhere in Compose using LocalDimens.current
val LocalDimens = compositionLocalOf { Dimens() }