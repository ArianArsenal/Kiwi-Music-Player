package com.example.kiwi.di

import android.content.Context
import androidx.room.Room
import com.example.kiwi.data.local.ChatDao
import com.example.kiwi.data.local.DownloadedFilesDao
import com.example.kiwi.data.local.KiwiDatabase
import com.example.kiwi.data.local.LikedSongsDao
import com.example.kiwi.data.local.SongDao
import com.example.kiwi.data.local.SearchHistoryDao // FIXED: Corrected import path
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KiwiDatabase {
        return Room.databaseBuilder(
            context,
            KiwiDatabase::class.java,
            "kiwi_local_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: KiwiDatabase): SongDao {
        return database.songDao
    }

    // FIXED: Changed parameter from AppDatabase to KiwiDatabase
    @Provides
    fun provideSearchHistoryDao(database: KiwiDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    // FIXED: Changed parameter from AppDatabase to KiwiDatabase
    @Provides
    fun provideLikedSongsDao(database: KiwiDatabase): LikedSongsDao {
        return database.likedSongsDao()
    }

    // FIXED: Changed parameter from AppDatabase to KiwiDatabase
    @Provides
    fun provideDownloadedFilesDao(database: KiwiDatabase): DownloadedFilesDao {
        return database.downloadedFilesDao()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: KiwiDatabase): ChatDao {
        return database.chatDao
    }
}