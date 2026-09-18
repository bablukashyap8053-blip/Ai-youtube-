package com.example.ui.chat

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.util.VoiceInputManager
import com.example.util.VoiceReplyManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: Long?,
    repository: AiRepository,
    profileStore: UserProfileStore,
    voiceInputManager: VoiceInputManager,
    voiceReplyManager: VoiceReplyManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by profileStore.profile.collectAsState()

    var activeSessionId by remember { mutableStateOf(sessionId ?: -1L) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val isListening by voiceInputManager.isListening.collectAsState()
    val soundLevel by voiceInputManager.soundLevel.collectAsState()
    val isSpeaking by voiceReplyManager.isSpeaking.collectAsState()

    // Initialize session if needed
    LaunchedEffect(sessionId) {
        if (sessionId == null || sessionId <= 0L) {
            val newId = repository.createNewSession()
            activeSessionId = newId
        } else {
            activeSessionId = sessionId
        }
    }

    val messagesFlow = remember(activeSessionId) {
        repository.getMessagesForSession(activeSessionId)
    }
    val messages by messagesFlow.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceInputManager.startListening(
                languageCode = if (profile.defaultLanguage.contains("Hindi")) "hi-IN" else "en-US"
            ) { recognized ->
                inputText = recognized
            }
        } else {
            Toast.makeText(context, "Voice input requires Audio permission", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSend(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank() || isGenerating) return
        inputText = ""
        isGenerating = true

        coroutineScope.launch {
            try {
                val assistantMsg = repository.sendMessage(
                    sessionId = activeSessionId,
                    userText = trimmed,
                    customApiKey = profile.customApiKey,
                    preferredModel = profile.preferredModel
                )
                isGenerating = false

                // Auto speak if enabled
                if (profile.autoVoiceReply && assistantMsg.content.isNotBlank()) {
                    voiceReplyManager.speak(assistantMsg.content)
                }
            } catch (e: Exception) {
                isGenerating = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Omni AI Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isGenerating) Color.Yellow else Color.Green)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGenerating) "AI उत्तर बना रहा है..." else "Gemini 3.5 Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            voiceInputManager.stopListening()
                            voiceReplyManager.stop()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // TTS Stop / Active indicator
                    if (isSpeaking) {
                        IconButton(
                            onClick = { voiceReplyManager.stop() },
                            modifier = Modifier.testTag("stop_speaking_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speech",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    // Toggle Auto-voice
                    IconButton(
                        onClick = {
                            profileStore.updateProfile(autoVoiceReply = !profile.autoVoiceReply)
                        },
                        modifier = Modifier.testTag("toggle_tts_btn")
                    ) {
                        Icon(
                            imageVector = if (profile.autoVoiceReply) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Auto Voice Reply",
                            tint = if (profile.autoVoiceReply) PrimaryVioletLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Messages List
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyChatGreeting(onPromptClick = { prompt -> handleSend(prompt) })
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onSpeakClick = { text -> voiceReplyManager.speak(text) },
                            onCopyClick = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("AI Message", text))
                                Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isGenerating) {
                        item {
                            AiTypingIndicator()
                        }
                    }
                }
            }

            // Quick Hindi / English Topic Chips
            QuickTopicChipsRow(onChipClick = { prompt -> handleSend(prompt) })

            // Live Voice Listening Waveform
            AnimatedVisibility(visible = isListening) {
                VoiceListeningWaveform(
                    soundLevel = soundLevel,
                    onStopListening = { voiceInputManager.stopListening() }
                )
            }

            // Bottom Input Bar
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = { handleSend(inputText) },
                isListening = isListening,
                onMicClick = {
                    if (isListening) {
                        voiceInputManager.stopListening()
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            voiceInputManager.startListening(
                                languageCode = if (profile.defaultLanguage.contains("Hindi")) "hi-IN" else "en-US"
                            ) { recognized ->
                                inputText = recognized
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                isGenerating = isGenerating
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    onSpeakClick: (String) -> Unit,
    onCopyClick: (String) -> Unit
) {
    val isUser = message.role == "user"
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_bubble_${message.id}" else "assistant_bubble_${message.id}"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryViolet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PrimaryViolet else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Formatted content (handles code blocks cleanly)
                if (message.content.contains("```")) {
                    FormattedMarkdownContent(
                        content = message.content,
                        textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Row {
                        IconButton(
                            onClick = { onCopyClick(message.content) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        if (!isUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onSpeakClick(message.content) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read Aloud",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownContent(content: String, textColor: Color) {
    val parts = content.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1E2E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = part.trim(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CyberCyan,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (part.isNotBlank()) {
                Text(
                    text = part.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun AiTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 40.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryVioletLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI उत्तर सोच रहा है...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickTopicChipsRow(onChipClick: (String) -> Unit) {
    val chips = listOf(
        "🧠 सरल शब्दों में समझाइए",
        "💻 Kotlin Compose Code",
        "📖 कहानी लिखें",
        "🎯 AI प्रॉम्प्ट सुधारें"
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips) { chip ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onChipClick(chip) }
            ) {
                Text(
                    text = chip,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VoiceListeningWaveform(
    soundLevel: Float,
    onStopListening: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(pulseScale * (1f + soundLevel * 0.4f))
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "वॉयस सुन रहे हैं... (Listening)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "बोलना समाप्त होने पर यह अपने आप इनपुट हो जाएगा",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(
                onClick = onStopListening,
                modifier = Modifier.testTag("stop_listening_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyChatGreeting(onPromptClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PrimaryViolet, CyberCyan))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Omni AI चैट में आपका स्वागत है!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "हिंदी या अंग्रेजी में कुछ भी पूछें, वॉयस इनपुट का उपयोग करें या नीचे दिए गए किसी भी प्रॉम्प्ट पर टैप करें:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val starterPrompts = listOf(
                "🚀 Android ऐप में AI को कैसे जोड़ें?",
                "🎬 Veo से 3D वीडियो कैसे जनरेट करते हैं?",
                "🇮🇳 भारत की समृद्ध संस्कृति पर एक सुंदर निबंध लिखें"
            )
            starterPrompts.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPromptClick(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit,
    isGenerating: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("संदेश लिखें... (Type message)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Mic Button
            Surface(
                shape = CircleShape,
                color = if (isListening) Color.Red else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onMicClick() }
                    .testTag("chat_mic_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button
            Surface(
                shape = CircleShape,
                color = if (text.isNotBlank() && !isGenerating) PrimaryViolet else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(enabled = text.isNotBlank() && !isGenerating) { onSend() }
                    .testTag("chat_send_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank() && !isGenerating) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
