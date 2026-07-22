package com.example.kiwi.data.remote

import android.util.Log
import com.example.kiwi.domain.model.ChatMessage
import com.example.kiwi.domain.model.MessageStatus
import com.example.kiwi.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketManager @Inject constructor(
    private val chatRepository: ChatRepository,
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // TODO: In the future, read this from UserPreferencesRepository so you can change it
    // dynamically from the Settings screen without rebuilding the app!
    private val WEBSOCKET_URL = "ws://192.168.1.3:8080/chat"

    var onTypingStatusChange: ((Boolean) -> Unit)? = null

    fun connect(userId: String) {
        val request = Request.Builder()
            .url("$WEBSOCKET_URL?userId=$userId")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("KiwiWS", "WebSocket Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    handleIncomingMessage(text)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("KiwiWS", "WebSocket Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("KiwiWS", "WebSocket Error", t)
            }
        })
    }

    private suspend fun handleIncomingMessage(jsonText: String) {
        try {
            val json = JSONObject(jsonText)
            when (json.getString("type")) {
                "new_message" -> {
                    // 1. Receive message from network and extract embedded song data
                    val message = ChatMessage(
                        id = json.getString("id"),
                        senderId = json.getString("senderId"),
                        receiverId = json.getString("receiverId"),
                        text = json.getString("text"),
                        timestamp = json.getLong("timestamp"),
                        status = MessageStatus.SENT,

                        // NEW: Extract all song metadata so the receiver sees it immediately
                        sharedSongId = if (json.has("sharedSongId")) json.getString("sharedSongId") else null,
                        sharedSongTitle = if (json.has("sharedSongTitle")) json.getString("sharedSongTitle") else null,
                        sharedSongArtist = if (json.has("sharedSongArtist")) json.getString("sharedSongArtist") else null,
                        sharedSongCoverUrl = if (json.has("sharedSongCoverUrl")) json.getString("sharedSongCoverUrl") else null,
                        sharedSongAudioUrl = if (json.has("sharedSongAudioUrl")) json.getString("sharedSongAudioUrl") else null
                    )

                    // 2. Save to Room database
                    chatRepository.saveMessage(message)

                    // 3. Immediately fire back a read receipt over the stream
                    sendReadReceipt(message.id, message.senderId)
                }
                "receipt" -> {
                    val messageId = json.getString("messageId")
                    val newStatus = MessageStatus.valueOf(json.getString("status"))
                    chatRepository.updateMessageStatus(messageId, newStatus)
                }
                "typing" -> {
                    val isTyping = json.getBoolean("isTyping")
                    onTypingStatusChange?.invoke(isTyping)
                }
            }
        } catch (e: Exception) {
            Log.e("KiwiWS", "Failed to parse incoming WebSocket message", e)
        }
    }

    fun sendMessage(message: ChatMessage) {
        scope.launch {
            val json = JSONObject().apply {
                put("type", "chat_message")
                put("id", message.id)
                put("senderId", message.senderId)
                put("receiverId", message.receiverId)
                put("text", message.text)
                put("timestamp", message.timestamp)

                // NEW: Push the embedded song data to the JSON payload
                message.sharedSongId?.let { put("sharedSongId", it) }
                message.sharedSongTitle?.let { put("sharedSongTitle", it) }
                message.sharedSongArtist?.let { put("sharedSongArtist", it) }
                message.sharedSongCoverUrl?.let { put("sharedSongCoverUrl", it) }
                message.sharedSongAudioUrl?.let { put("sharedSongAudioUrl", it) }
            }

            val isPushed = webSocket?.send(json.toString()) == true

            if (!isPushed) {
                Log.w("KiwiWS", "No connection. Message remains in SENDING state in Room.")
            }
        }
    }

    private fun sendReadReceipt(messageId: String, senderId: String) {
        val json = JSONObject().apply {
            put("type", "receipt") // Changed to match Python bot expectation
            put("messageId", messageId)
            put("status", "READ")
        }
        webSocket?.send(json.toString())
    }

    fun sendTypingStatus(senderId: String, receiverId: String, isTyping: Boolean) {
        val json = JSONObject().apply {
            put("type", "typing") // Changed to match Python bot expectation
            put("senderId", senderId)
            put("receiverId", receiverId)
            put("isTyping", isTyping)
        }
        webSocket?.send(json.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "User closed chat")
        webSocket = null
    }
}