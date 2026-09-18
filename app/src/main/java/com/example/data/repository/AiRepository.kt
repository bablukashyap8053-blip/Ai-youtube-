package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.GeneratedMediaEntity
import com.example.data.remote.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AiRepository(private val database: AppDatabase) {
    private val dao: ChatDao = database.chatDao()

    val allSessions: Flow<List<ChatSessionEntity>> = dao.getAllSessions()
    val allMedia: Flow<List<GeneratedMediaEntity>> = dao.getAllGeneratedMedia()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return dao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(initialTitle: String = "नई AI बातचीत"): Long = withContext(Dispatchers.IO) {
        val session = ChatSessionEntity(
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastMessagePreview = "वार्तालाप शुरू हुआ...",
            messageCount = 0
        )
        dao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        dao.deleteMessagesBySessionId(sessionId)
        dao.deleteSessionById(sessionId)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        dao.clearAllMessages()
        dao.clearAllSessions()
    }

    suspend fun sendMessage(
        sessionId: Long,
        userText: String,
        customApiKey: String? = null,
        preferredModel: String = "gemini-3.5-flash"
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
        // 1. Save user message
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = userText,
            timestamp = System.currentTimeMillis(),
            messageType = "text"
        )
        dao.insertMessage(userMsg)

        // 2. Fetch recent conversation history for context
        val existingMessages = dao.getMessagesForSessionSync(sessionId)
        val historyTurns = existingMessages.takeLast(10).map { msg ->
            Pair(msg.role, msg.content)
        }

        // 3. Call Gemini
        val result = GeminiService.generateChatResponse(
            prompt = userText,
            history = historyTurns,
            model = preferredModel,
            customApiKey = customApiKey
        )
        val aiReplyText = result.getOrElse { "उत्तर प्राप्त करने में समस्या आई: ${it.localizedMessage}" }

        // 4. Save AI reply
        val assistantMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "assistant",
            content = aiReplyText,
            timestamp = System.currentTimeMillis(),
            messageType = "text"
        )
        val assistantId = dao.insertMessage(assistantMsg)

        // 5. Update session metadata
        val session = dao.getSessionById(sessionId)
        if (session != null) {
            val title = if (session.messageCount == 0 && userText.isNotBlank()) {
                userText.take(30)
            } else session.title

            dao.updateSession(
                session.copy(
                    title = title,
                    updatedAt = System.currentTimeMillis(),
                    lastMessagePreview = aiReplyText.take(60),
                    messageCount = session.messageCount + 2
                )
            )
        }

        assistantMsg.copy(id = assistantId)
    }

    suspend fun generateAndSaveImage(
        prompt: String,
        style: String,
        aspectRatio: String,
        customApiKey: String? = null
    ): GeneratedMediaEntity = withContext(Dispatchers.IO) {
        val result = GeminiService.generateImageContent(prompt, aspectRatio, style, customApiKey)
        val uriOrData = result.getOrDefault("")

        val media = GeneratedMediaEntity(
            prompt = prompt,
            mediaType = "image",
            mediaUriOrData = uriOrData,
            style = style,
            aspectRatio = aspectRatio,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertGeneratedMedia(media)
        media.copy(id = id)
    }

    suspend fun generateAndSaveVideoStoryboard(
        prompt: String,
        genre: String,
        duration: String,
        customApiKey: String? = null
    ): Pair<String, GeneratedMediaEntity> = withContext(Dispatchers.IO) {
        val result = GeminiService.generateVideoStoryboard(prompt, genre, duration, customApiKey)
        val storyboardText = result.getOrDefault("AI Video Script Generated.")

        val media = GeneratedMediaEntity(
            prompt = prompt,
            mediaType = "video",
            mediaUriOrData = storyboardText,
            style = genre,
            aspectRatio = "16:9",
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertGeneratedMedia(media)
        Pair(storyboardText, media.copy(id = id))
    }

    suspend fun deleteMedia(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteGeneratedMediaById(id)
    }
}
