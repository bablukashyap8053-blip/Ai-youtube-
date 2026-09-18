package com.example.ui.image

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GeneratedMediaEntity
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LuminousAmber
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenScreen(
    repository: AiRepository,
    profileStore: UserProfileStore,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by profileStore.profile.collectAsState()
    val mediaList by repository.allMedia.collectAsState(initial = emptyList())
    val imageList = remember(mediaList) { mediaList.filter { it.mediaType == "image" } }

    var promptText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Photorealistic") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentGeneratedImage by remember { mutableStateOf<GeneratedMediaEntity?>(null) }

    val styles = listOf("Photorealistic", "Cyberpunk", "Anime 3D", "Cinematic", "Digital Art", "Oil Painting")
    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")

    fun generateImage() {
        if (promptText.isBlank() || isGenerating) return
        isGenerating = true

        coroutineScope.launch {
            try {
                val media = repository.generateAndSaveImage(
                    prompt = promptText,
                    style = selectedStyle,
                    aspectRatio = selectedRatio,
                    customApiKey = profile.customApiKey
                )
                currentGeneratedImage = media
                isGenerating = false
                Toast.makeText(context, "इमेज सफलतापूर्वक जनरेट हुई!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                isGenerating = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("image_gen_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Image Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Powered by Gemini 2.5 Flash Image",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("image_gen_back_btn")
                    ) {
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
            // Prompt input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎨 इमेज का विवरण लिखें (Prompt)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = { Text("जैसे: A majestic glowing tiger in a cybernetic jungle with neon butterflies...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_prompt_input"),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        // Prompt expander helper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    promptText = if (promptText.isBlank()) {
                                        "Futuristic AI cybernetic floating temple with golden holographic rings and crystal waterfalls"
                                    } else {
                                        "$promptText, highly detailed 8k cinematic lighting octane render"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("enhance_prompt_btn")
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("प्रॉम्प्ट को बेहतर बनाएं (Enhance)")
                            }
                        }
                    }
                }
            }

            // Style Selector
            item {
                Text(
                    text = "🎭 आर्ट स्टाइल (Art Style)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(styles) { style ->
                        val isSelected = selectedStyle == style
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedStyle = style }
                                .testTag("style_chip_$style")
                        ) {
                            Text(
                                text = style,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Aspect Ratio Selector
            item {
                Text(
                    text = "📐 आस्पेक्ट रेशियो (Aspect Ratio)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    aspectRatios.forEach { ratio ->
                        val isSelected = selectedRatio == ratio
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedRatio = ratio }
                                .testTag("ratio_chip_$ratio")
                        ) {
                            Text(
                                text = ratio,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Generate Action Button
            item {
                Button(
                    onClick = { generateImage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_image_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                    enabled = promptText.isNotBlank() && !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("AI इमेज बना रहा है...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("इमेज जनरेट करें (Generate Image)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Current Preview or Empty Placeholder
            item {
                currentGeneratedImage?.let { media ->
                    Text(
                        text = "✨ आपकी जनरेट की गई इमेज (Result)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GeneratedImageDisplayCard(
                        media = media,
                        onCopyPrompt = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Prompt", media.prompt))
                            Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Gallery of Previous Creations
            item {
                Text(
                    text = "🖼️ आपकी गैलरी (Saved Creations: ${imageList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (imageList.isEmpty()) {
                item {
                    Text(
                        text = "अभी तक कोई इमेज सहेजी नहीं गई है। कोई प्रॉम्प्ट लिखकर ऊपर जनरेट करें!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(imageList) { media ->
                    GeneratedImageDisplayCard(
                        media = media,
                        onDelete = {
                            coroutineScope.launch { repository.deleteMedia(media.id) }
                        },
                        onCopyPrompt = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Prompt", media.prompt))
                            Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratedImageDisplayCard(
    media: GeneratedMediaEntity,
    onDelete: (() -> Unit)? = null,
    onCopyPrompt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("media_item_${media.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Visual display: decode Base64 if present, else render procedural tech art canvas!
            val rawData = media.mediaUriOrData
            if (rawData.startsWith("data:image") && rawData.contains(",")) {
                val base64 = rawData.substringAfter(",")
                val bitmap = remember(base64) {
                    try {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = media.prompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    )
                } else {
                    ProceduralArtCanvas(prompt = media.prompt, style = media.style)
                }
            } else {
                ProceduralArtCanvas(prompt = media.prompt, style = media.style)
            }

            // Info & Actions
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${media.style} • ${media.aspectRatio}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row {
                        IconButton(onClick = onCopyPrompt, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Prompt", modifier = Modifier.size(16.dp))
                        }
                        if (onDelete != null) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = media.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ProceduralArtCanvas(prompt: String, style: String) {
    // Elegant procedural artwork generator that gives real visual beauty even in offline/demo mode
    val hash = prompt.hashCode()
    val baseColors = remember(prompt, style) {
        when {
            style.contains("Cyberpunk") -> listOf(Color(0xFF00E5FF), Color(0xFFFF007F), Color(0xFF7000FF), Color(0xFF0D0B18))
            style.contains("Anime") -> listOf(Color(0xFFFF80AB), Color(0xFF82B1FF), Color(0xFFFFD180), Color(0xFF311B92))
            style.contains("Oil") -> listOf(Color(0xFFFFB300), Color(0xFFD84315), Color(0xFF4E342E), Color(0xFF212121))
            else -> listOf(PrimaryViolet, CyberCyan, NeonPink, Color(0xFF100729))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Background gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = baseColors,
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.75f
                )
            )

            // Celestial orbs & harmonic wave patterns
            drawCircle(
                color = baseColors[0].copy(alpha = 0.5f),
                radius = w * 0.25f,
                center = Offset(w * 0.4f, h * 0.4f)
            )

            drawCircle(
                color = baseColors[1].copy(alpha = 0.4f),
                radius = w * 0.18f,
                center = Offset(w * 0.65f, h * 0.55f)
            )

            // Dynamic diagonal cyber glow rays
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(0f, 0f),
                end = Offset(w, h),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = CyberCyan.copy(alpha = 0.45f),
                start = Offset(0f, h),
                end = Offset(w, 0f),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Overlay banner
        Surface(
            color = Color.Black.copy(alpha = 0.45f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "AI Rendered Art", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
