package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessagePreview: String = "",
    val messageCount: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUrl: String? = null,
    val messageType: String = "text", // "text", "image", "voice", "code"
    val isSpoken: Boolean = false
)

@Entity(tableName = "generated_media")
data class GeneratedMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prompt: String,
    val mediaType: String, // "image", "video"
    val mediaUriOrData: String,
    val style: String = "Realistic",
    val aspectRatio: String = "1:1",
    val timestamp: Long = System.currentTimeMillis()
)
