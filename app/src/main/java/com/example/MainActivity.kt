package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.local.UserProfileStore
import com.example.data.repository.AiRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.util.VoiceInputManager
import com.example.util.VoiceReplyManager

class MainActivity : ComponentActivity() {

    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var voiceReplyManager: VoiceReplyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AiRepository(database)
        val profileStore = UserProfileStore(applicationContext)
        voiceInputManager = VoiceInputManager(applicationContext)
        voiceReplyManager = VoiceReplyManager(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        repository = repository,
                        profileStore = profileStore,
                        voiceInputManager = voiceInputManager,
                        voiceReplyManager = voiceReplyManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceInputManager.destroy()
        voiceReplyManager.destroy()
    }
}
