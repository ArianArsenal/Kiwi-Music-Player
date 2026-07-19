package com.example.kiwi.di

import android.content.Context
import androidx.room.Room
import com.example.kiwi.data.local.KiwiDatabase
import com.example.kiwi.data.local.SongDao
import com.example.kiwi.data.local.room.SearchHistoryDao
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: KiwiDatabase): SongDao {
        return database.songDao
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: KiwiDatabase): SearchHistoryDao {
        return database.searchHistoryDao
    }
}