package com.example.kiwi.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun AudioVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val barCount = 7
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")

    // Create an animated float for each bar with random durations for an organic look
    val animations = List(barCount) { index ->
        val duration = (400..800).random()
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / (barCount * 2f)
        val spacing = barWidth

        for (i in 0 until barCount) {
            // If playing, use the animation multiplier; if paused, shrink to 20% height
            val heightMultiplier = if (isPlaying) animations[i].value else 0.2f
            val barHeight = canvasHeight * heightMultiplier

            val startX = (i * (barWidth + spacing)) + (canvasWidth - ((barCount * barWidth) + ((barCount - 1) * spacing))) / 2f
            val startY = canvasHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(startX, startY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}