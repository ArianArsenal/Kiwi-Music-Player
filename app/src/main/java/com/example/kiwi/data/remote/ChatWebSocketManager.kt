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
    private val okHttpClient: OkHttpClient // Injected from NetworkModule
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Connects to your local backend (Python running on your PC)
    // 10.0.2.2 is the Android emulator's alias for your computer's localhost.
    private val WEBSOCKET_URL = "ws://192.168.1.3:8080/chat"

    // Callback to push live typing data to the ViewModel
    var onTypingStatusChange: ((Boolean) -> Unit)? = null

    fun connect(userId: String) {
        val request = Request.Builder()
            .url("$WEBSOCKET_URL?userId=$userId")
            .build()

        // Opens the persistent Real-time data stream
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
                    // 1. Receive message from network
                    val message = ChatMessage(
                        id = json.getString("id"),
                        senderId = json.getString("senderId"),
                        receiverId = json.getString("receiverId"),
                        text = json.getString("text"),
                        timestamp = json.getLong("timestamp"),
                        status = MessageStatus.SENT, // Arrived successfully
                        sharedSongId = if (json.has("sharedSongId")) json.getString("sharedSongId") else null
                    )

                    // 2. Save to Room database (satisfies Offline Management)
                    chatRepository.saveMessage(message)

                    // 3. Immediately fire back a read receipt over the stream
                    sendReadReceipt(message.id, message.senderId)
                }
                "receipt" -> {
                    // Upgrades the status from SENDING -> SENT -> READ based on server confirmation
                    val messageId = json.getString("messageId")
                    val newStatus = MessageStatus.valueOf(json.getString("status")) // e.g., "READ"

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
            // 1. Save locally as SENDING first (Clock Icon)
            chatRepository.saveMessage(message.copy(status = MessageStatus.SENDING))

            // 2. Format payload
            val json = JSONObject().apply {
                put("type", "chat_message")
                put("id", message.id)
                put("senderId", message.senderId)
                put("receiverId", message.receiverId)
                put("text", message.text)
                put("timestamp", message.timestamp)
                message.sharedSongId?.let { put("sharedSongId", it) }
            }

            // 3. Push to stream
            val isPushed = webSocket?.send(json.toString()) == true

            if (!isPushed) {
                Log.w("KiwiWS", "No connection. Message remains in SENDING state in Room.")
            }
        }
    }

    private fun sendReadReceipt(messageId: String, senderId: String) {
        val json = JSONObject().apply {
            put("type", "read_receipt")
            put("messageId", messageId)
            put("senderId", senderId) // The person who sent the message to you
        }
        webSocket?.send(json.toString())
    }

    fun sendTypingStatus(senderId: String, receiverId: String, isTyping: Boolean) {
        val json = JSONObject().apply {
            put("type", "typing_status")
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