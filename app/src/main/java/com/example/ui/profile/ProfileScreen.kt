package com.example.ui.profile

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LuminousAmber
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.util.VoiceReplyManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileStore: UserProfileStore,
    repository: AiRepository,
    voiceReplyManager: VoiceReplyManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by profileStore.profile.collectAsState()

    var userName by remember(profile.name) { mutableStateOf(profile.name) }
    var userEmail by remember(profile.email) { mutableStateOf(profile.email) }
    var customApiKey by remember(profile.customApiKey) { mutableStateOf(profile.customApiKey) }
    var selectedAvatar by remember(profile.avatarEmoji) { mutableStateOf(profile.avatarEmoji) }
    var autoVoiceReply by remember(profile.autoVoiceReply) { mutableStateOf(profile.autoVoiceReply) }
    var voiceSpeed by remember(profile.voiceSpeed) { mutableFloatStateOf(profile.voiceSpeed) }
    var voicePitch by remember(profile.voicePitch) { mutableFloatStateOf(profile.voicePitch) }
    var isSecureBackend by remember(profile.isSecureBackendActive) { mutableStateOf(profile.isSecureBackendActive) }

    val avatars = listOf("⚡", "🤖", "🚀", "👑", "🌟", "🔥", "🔮", "🎨")
    val languages = listOf("Hindi & English", "Hindi Only (हिन्दी)", "English Only")

    fun saveSettings() {
        profileStore.updateProfile(
            name = userName,
            email = userEmail,
            avatarEmoji = selectedAvatar,
            customApiKey = customApiKey,
            autoVoiceReply = autoVoiceReply,
            voiceSpeed = voiceSpeed,
            voicePitch = voicePitch,
            isSecureBackendActive = isSecureBackend
        )
        voiceReplyManager.setPitchAndSpeed(voicePitch, voiceSpeed)
        Toast.makeText(context, "प्रोफाइल व सेटिंग्स सहेज ली गईं!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "👤 प्रोफाइल व सुरक्षा (Profile & Backend)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("profile_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { saveSettings() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("सहेजें (Save)", fontWeight = FontWeight.Bold)
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
            // User Identity Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryViolet, CyberCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedAvatar, fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "अवतार चुनें (Choose Avatar):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(avatars) { av ->
                                val isSelected = selectedAvatar == av
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) PrimaryVioletLight else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedAvatar = av }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = av, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("पूरा नाम (Name)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text("ईमेल पता (Email)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Secure Backend & API Credentials
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyberCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔐 सुरक्षित बैकएंड (Secure Backend)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("स्थानीय डेटा एन्क्रिप्शन (Local Storage Encryption)", fontWeight = FontWeight.SemiBold)
                                Text("Room Database में चैट और प्रॉम्प्ट सुरक्षित हैं", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isSecureBackend,
                                onCheckedChange = { isSecureBackend = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "🤖 Gemini API कनेक्शन स्थिति (Connection Status):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = PrimaryVioletLight)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "AI Studio Secrets Service: सक्रिय",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "API Key .env व BuildConfig द्वारा सुरक्षित रूप से इंजेक्ट की जाती है",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customApiKey,
                            onValueChange = { customApiKey = it },
                            label = { Text("वैकल्पिक कस्टम API Key (Custom Gemini Key)") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_api_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(imageVector = Icons.Default.Key, contentDescription = null) }
                        )
                    }
                }
            }

            // Voice & TTS Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = LuminousAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔊 AI वॉयस व ऑडियो (Voice & Speech)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ऑटो वॉयस रिप्लाई (Auto Voice Reply)", fontWeight = FontWeight.SemiBold)
                                Text("AI संदेश प्राप्त होते ही बोलकर सुनाएगा", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoVoiceReply,
                                onCheckedChange = { autoVoiceReply = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = LuminousAmber)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("बोलने की गति (Speech Rate): ${String.format("%.1f", voiceSpeed)}x")
                        Slider(
                            value = voiceSpeed,
                            onValueChange = { voiceSpeed = it },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = LuminousAmber, activeTrackColor = LuminousAmber)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("आवाज की पिच (Voice Pitch): ${String.format("%.1f", voicePitch)}x")
                        Slider(
                            value = voicePitch,
                            onValueChange = { voicePitch = it },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryVioletLight, activeTrackColor = PrimaryVioletLight)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                voiceReplyManager.setPitchAndSpeed(voicePitch, voiceSpeed)
                                voiceReplyManager.speak("नमस्ते! यह आपकी AI वॉयस का परीक्षण है।")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("test_voice_btn")
                        ) {
                            Text("आवाज का परीक्षण करें (Test Voice)", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Android APK Package Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📦 Android APK पैकेज (Build Information)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• ऐप का नाम: AI Studio", style = MaterialTheme.typography.bodyMedium)
                        Text("• Application ID: com.aistudio.aistudioapp.vptkqz", style = MaterialTheme.typography.bodyMedium)
                        Text("• Target SDK: Android 15/16 (API 36)", style = MaterialTheme.typography.bodyMedium)
                        Text("• आर्किटेक्चर: Jetpack Compose + Room + Gemini REST API", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "APK बनाने के लिए: आप AI Studio मेनू से सीधे APK या AAB डाउनलोड कर सकते हैं अथवा 'gradle assembleDebug' चला सकते हैं।",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
