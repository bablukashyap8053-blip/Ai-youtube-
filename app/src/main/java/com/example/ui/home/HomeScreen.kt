package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LuminousAmber
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight

@Composable
fun HomeScreen(
    repository: AiRepository,
    profileStore: UserProfileStore,
    onNavigateToChat: (sessionId: Long?) -> Unit,
    onNavigateToImageGen: () -> Unit,
    onNavigateToVideoStudio: () -> Unit,
    onNavigateToAnimation: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by profileStore.profile.collectAsState()
    val sessions by repository.allSessions.collectAsState(initial = emptyList())
    var quickPromptText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Header with User Greeting & Status
        item {
            HeaderSection(
                userName = profile.name,
                avatarEmoji = profile.avatarEmoji,
                isSecure = profile.isSecureBackendActive,
                onProfileClick = onNavigateToProfile
            )
        }

        // 2. Quick AI Ask Bar
        item {
            QuickAskCard(
                prompt = quickPromptText,
                onPromptChange = { quickPromptText = it },
                onSend = {
                    if (quickPromptText.isNotBlank()) {
                        val text = quickPromptText
                        quickPromptText = ""
                        onNavigateToChat(null) // Will start chat with prompt
                    }
                },
                onVoiceClick = onNavigateToVoice
            )
        }

        // 3. Featured Modules Grid
        item {
            Text(
                text = "✨ प्रमुख AI फीचर्स (Features)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "AI Chat & Code",
                        hindiSubtitle = "स्मार्ट बातचीत व कोडिंग",
                        icon = Icons.Default.Chat,
                        gradientColors = listOf(PrimaryViolet, Color(0xFF9C27B0)),
                        testTag = "feature_card_chat",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToChat(null) }
                    )
                    FeatureCard(
                        title = "Image Generator",
                        hindiSubtitle = "टेक्स्ट से आर्ट व फोटो",
                        icon = Icons.Default.Image,
                        gradientColors = listOf(CyberCyan, Color(0xFF00796B)),
                        testTag = "feature_card_image",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToImageGen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "AI Long Video",
                        hindiSubtitle = "Veo स्क्रिप्ट व स्टोरीबोर्ड",
                        icon = Icons.Default.VideoLibrary,
                        gradientColors = listOf(NeonPink, Color(0xFF880E4F)),
                        testTag = "feature_card_video",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVideoStudio
                    )
                    FeatureCard(
                        title = "2D & 3D Animation",
                        hindiSubtitle = "इंटरएक्टिव 3D फिजिक्स",
                        icon = Icons.Default.ViewInAr,
                        gradientColors = listOf(LuminousAmber, Color(0xFFE65100)),
                        testTag = "feature_card_animation",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAnimation
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Voice Assistant",
                        hindiSubtitle = "वॉयस इनपुट व ऑडियो रिप्लाई",
                        icon = Icons.Default.Mic,
                        gradientColors = listOf(Color(0xFF00C853), Color(0xFF1B5E20)),
                        testTag = "feature_card_voice",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVoice
                    )
                    FeatureCard(
                        title = "Chat History",
                        hindiSubtitle = "सहेजी गई बातचीत",
                        icon = Icons.Default.History,
                        gradientColors = listOf(Color(0xFF3F51B5), Color(0xFF1A237E)),
                        testTag = "feature_card_history",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                }
            }
        }

        // 4. Quick Prompts Horizontal Carousel
        item {
            Text(
                text = "💡 तुरंत पूछें (Quick Suggestions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            val promptChips = listOf(
                "🌌 भविष्य का 3D शहर कैसा दिखेगा?",
                "📱 Jetpack Compose में 3D रोटेशन कैसे करें?",
                "🎬 एक 60-सेकंड की AI वीडियो स्क्रिप्ट तैयार करें",
                "🤖 Gemini 3.5 Flash के नए फीचर्स क्या हैं?",
                "🖼️ Cyberpunk Samurai in Neon Rain (16:9)"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(promptChips) { chipText ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToChat(null) }
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Recent Sessions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💾 हालिया बातचीत (Recent Chats)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "सभी देखें (${sessions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .testTag("view_all_history_btn")
                        .clickable { onNavigateToHistory() }
                )
            }
        }

        if (sessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "अभी तक कोई चैट नहीं है",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "AI Chat शुरू करने के लिए ऊपर दिए गए कार्ड पर टैप करें!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(sessions.take(3)) { session ->
                RecentSessionCard(session = session, onClick = { onNavigateToChat(session.id) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    avatarEmoji: String,
    isSecure: Boolean,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onProfileClick() }
                .testTag("profile_header_btn")
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(PrimaryViolet, CyberCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = avatarEmoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "नमस्ते / Hello,",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSecure) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.clickable { onProfileClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSecure) "सुरक्षित (Secure)" else "असुरक्षित",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun QuickAskCard(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_ask_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryVioletLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI से कुछ भी पूछें (Ask Omni AI)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("प्रश्न लिखें या वॉयस बटन दबाएँ...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_ask_input"),
                trailingIcon = {
                    Row(
                        modifier = Modifier.padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onVoiceClick() }
                                .testTag("quick_voice_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (prompt.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { onSend() }
                                    .testTag("quick_send_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    hindiSubtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = hindiSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun RecentSessionCard(
    session: ChatSessionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("recent_session_${session.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.lastMessagePreview.ifEmpty { "चैट जारी रखें..." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
