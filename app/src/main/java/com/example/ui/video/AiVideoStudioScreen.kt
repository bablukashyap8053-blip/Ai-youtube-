package com.example.ui.video

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LuminousAmber
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVideoStudioScreen(
    repository: AiRepository,
    profileStore: UserProfileStore,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by profileStore.profile.collectAsState()

    var themeInput by remember { mutableStateOf("Cyberpunk Odyssey: Rise of Futuristic India 2077") }
    var selectedGenre by remember { mutableStateOf("Cinematic Sci-Fi") }
    var selectedDuration by remember { mutableStateOf("60 Seconds") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedStoryboard by remember {
        mutableStateOf(
            """
🎬 **AI Long Video Storyboard & Script**
**शीर्षक:** Cyberpunk Odyssey 2077
**शैली:** Cinematic Sci-Fi (4K 60fps)

---
📍 **दृश्य 1 (00:00 - 00:15) - भव्य उद्घाटन**
• **कैमरा:** हाई-एंगल ड्रोन पैन शॉट
• **Veo प्रॉम्प्ट:** Ultra-realistic drone shot descending into a rain-slicked neon metropolis with holographic advertisements in Hindi & English.
• **ऑडियो:** Synthwave ambient beat & thunder roar.
• **डायलॉग:** "भविष्य अब कोई सपना नहीं है... यह हमारी हकीकत है।"

📍 **दृश्य 2 (00:15 - 00:35) - मुख्य पात्र व गति**
• **कैमरा:** डॉली ट्रैक मोशन
• **Veo प्रॉम्प्ट:** Cyber warrior riding a glowing hover-bike across elevated skyways at sunset.
• **डायलॉग:** "The journey into tomorrow begins now."

📍 **दृश्य 3 (00:35 - 00:60) - क्लाइमेक्स व समापन**
• **कैमरा:** 360 ऑर्बिट शॉट
• **Veo प्रॉम्प्ट:** Grand cyber palace glowing with neural cosmic energy, illuminating the horizon.
• **अंतिम संदेश:** "Omni AI Studio - Creating New Worlds"
            """.trimIndent()
        )
    }

    // Video Player state
    var isPlaying by remember { mutableStateOf(true) }
    var currentProgress by remember { mutableFloatStateOf(0.25f) }
    var currentSeconds by remember { mutableIntStateOf(15) }

    // Auto player ticker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            currentSeconds = (currentSeconds + 1) % 60
            currentProgress = currentSeconds / 60f
        }
    }

    val genres = listOf("Cinematic Sci-Fi", "Mythological Epic", "Action Anime", "Nature Documentary", "Tech Explainer")
    val durations = listOf("30 Seconds", "60 Seconds", "2 Minutes", "5 Minutes")

    fun generateVideoScript() {
        if (themeInput.isBlank() || isGenerating) return
        isGenerating = true

        coroutineScope.launch {
            try {
                val (script, _) = repository.generateAndSaveVideoStoryboard(
                    prompt = themeInput,
                    genre = selectedGenre,
                    duration = selectedDuration,
                    customApiKey = profile.customApiKey
                )
                generatedStoryboard = script
                isGenerating = false
                currentSeconds = 0
                currentProgress = 0f
                isPlaying = true
                Toast.makeText(context, "AI वीडियो स्क्रिप्ट व स्टोरीबोर्ड तैयार!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                isGenerating = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("video_studio_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Long Video Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Veo 3.1 & Storyboard Director",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("video_studio_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Video Player Preview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_preview_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Column {
                        // Animated Canvas Player Simulation
                        InteractiveCinematicCanvas(
                            genre = selectedGenre,
                            progress = currentProgress,
                            isPlaying = isPlaying
                        )

                        // Player Controls Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF14121F))
                                .padding(12.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { currentProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyberCyan,
                                trackColor = Color(0xFF332D4F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("00:%02d / 01:00", currentSeconds),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            currentSeconds = (currentSeconds - 10).coerceAtLeast(0)
                                            currentProgress = currentSeconds / 60f
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind", tint = Color.White)
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = CyberCyan,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable { isPlaying = !isPlaying }
                                            .testTag("video_play_pause_btn")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = Color.Black,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            currentSeconds = (currentSeconds + 10).coerceAtMost(59)
                                            currentProgress = currentSeconds / 60f
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Fast Forward", tint = Color.White)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF261E3E)
                                ) {
                                    Text(
                                        text = "4K 60FPS",
                                        color = CyberCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Video Creation Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎬 AI लॉन्ग वीडियो थीम (Video Theme)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = themeInput,
                            onValueChange = { themeInput = it },
                            placeholder = { Text("जैसे: Mystery in the Himalayas, AI Robot Friendship...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("video_theme_input"),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🎭 जॉनर (Genre)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(genres) { genre ->
                                val isSelected = selectedGenre == genre
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NeonPink else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedGenre = genre }
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⏱️ अवधि (Target Duration)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            durations.forEach { dur ->
                                val isSelected = selectedDuration == dur
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) LuminousAmber else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedDuration = dur }
                                ) {
                                    Text(
                                        text = dur,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { generateVideoScript() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_video_script_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            enabled = themeInput.isNotBlank() && !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI वीडियो स्टोरीबोर्ड तैयार कर रहा है...")
                            } else {
                                Icon(imageVector = Icons.Default.Movie, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("वीडियो स्टोरीबोर्ड व स्क्रिप्ट बनाएं (Generate)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Generated Storyboard Display
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📑 Veo स्टोरीबोर्ड व दृश्य विवरण",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Video Script", generatedStoryboard))
                                    Toast.makeText(context, "Script copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_video_script_btn")
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generatedStoryboard,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Default,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveCinematicCanvas(
    genre: String,
    progress: Float,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "camera")
    val cameraOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Starfield / Cyberpunk cityscape horizon
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF070014), Color(0xFF190D38), Color(0xFF380E52)),
                    startY = 0f,
                    endY = h
                )
            )

            // Dynamic grid perspective lines
            val horizonY = h * 0.55f
            val camX = if (isPlaying) cameraOffset else 0f

            for (i in -10..10) {
                val startX = w * 0.5f + camX
                val endX = w * 0.5f + (i * 70f) + (camX * 2.5f)
                drawLine(
                    color = CyberCyan.copy(alpha = 0.25f),
                    start = Offset(startX, horizonY),
                    end = Offset(endX, h),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Glowing celestial energy core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink, PrimaryViolet, Color.Transparent),
                    center = Offset(w * 0.5f + camX * 0.5f, horizonY * 0.8f),
                    radius = w * 0.28f
                ),
                radius = w * 0.28f,
                center = Offset(w * 0.5f + camX * 0.5f, horizonY * 0.8f)
            )

            // Moving cinematic particle rays
            for (p in 1..20) {
                val px = ((p * 97 + progress * 500) % w)
                val py = ((p * 61 + progress * 200) % horizonY)
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 2.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Live scene indicator watermark
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color.Red else Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI VEO SIMULATOR • ${if (progress < 0.33f) "SCENE 1" else if (progress < 0.66f) "SCENE 2" else "SCENE 3"}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
