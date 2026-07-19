package com.example.kiwi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.kiwi.presentation.screens.MainScreen
import com.example.kiwi.presentation.theme.KiwiMusicPlayerTheme // Updated Theme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() { // Must be AppCompatActivity for Locales
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiwiMusicPlayerTheme {
                MainScreen()
            }
        }
    }
}