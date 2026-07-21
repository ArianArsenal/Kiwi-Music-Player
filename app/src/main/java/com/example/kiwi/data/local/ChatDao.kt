package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // Fetches the entire conversation history with a specific user, sorted by time
    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId OR receiverId = :userId) ORDER BY timestamp ASC")
    fun getChatHistoryWithUser(userId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    // Used when the WebSocket tells us a message went from SENT to READ
    @Query("UPDATE chat_messages SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: String)
}