package com.example.kiwi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kiwi.data.local.room.SearchHistoryDao
import com.example.kiwi.data.local.room.SearchHistoryEntity
// Make sure to increase the version number (e.g., from 1 to 2)
// since we are modifying the database schema!
@Database(
    entities = [
        SongEntity::class, // (Assuming this is your existing entity name)
        SearchHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KiwiDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val searchHistoryDao: SearchHistoryDao // NEW
}