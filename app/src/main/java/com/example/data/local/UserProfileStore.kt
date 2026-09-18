package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val name: String = "Bablu Kashyap",
    val email: String = "bablukashyap8053@gmail.com",
    val avatarEmoji: String = "⚡",
    val customApiKey: String = "",
    val autoVoiceReply: Boolean = true,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val defaultLanguage: String = "Hindi & English",
    val preferredModel: String = "gemini-3.5-flash",
    val isSecureBackendActive: Boolean = true,
    val biometricLockEnabled: Boolean = false
)

class UserProfileStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private fun loadProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("name", "Bablu Kashyap") ?: "Bablu Kashyap",
            email = prefs.getString("email", "bablukashyap8053@gmail.com") ?: "bablukashyap8053@gmail.com",
            avatarEmoji = prefs.getString("avatarEmoji", "⚡") ?: "⚡",
            customApiKey = prefs.getString("customApiKey", "") ?: "",
            autoVoiceReply = prefs.getBoolean("autoVoiceReply", true),
            voiceSpeed = prefs.getFloat("voiceSpeed", 1.0f),
            voicePitch = prefs.getFloat("voicePitch", 1.0f),
            defaultLanguage = prefs.getString("defaultLanguage", "Hindi & English") ?: "Hindi & English",
            preferredModel = prefs.getString("preferredModel", "gemini-3.5-flash") ?: "gemini-3.5-flash",
            isSecureBackendActive = prefs.getBoolean("isSecureBackendActive", true),
            biometricLockEnabled = prefs.getBoolean("biometricLockEnabled", false)
        )
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        avatarEmoji: String? = null,
        customApiKey: String? = null,
        autoVoiceReply: Boolean? = null,
        voiceSpeed: Float? = null,
        voicePitch: Float? = null,
        defaultLanguage: String? = null,
        preferredModel: String? = null,
        isSecureBackendActive: Boolean? = null,
        biometricLockEnabled: Boolean? = null
    ) {
        val current = _profile.value
        val updated = current.copy(
            name = name ?: current.name,
            email = email ?: current.email,
            avatarEmoji = avatarEmoji ?: current.avatarEmoji,
            customApiKey = customApiKey ?: current.customApiKey,
            autoVoiceReply = autoVoiceReply ?: current.autoVoiceReply,
            voiceSpeed = voiceSpeed ?: current.voiceSpeed,
            voicePitch = voicePitch ?: current.voicePitch,
            defaultLanguage = defaultLanguage ?: current.defaultLanguage,
            preferredModel = preferredModel ?: current.preferredModel,
            isSecureBackendActive = isSecureBackendActive ?: current.isSecureBackendActive,
            biometricLockEnabled = biometricLockEnabled ?: current.biometricLockEnabled
        )

        prefs.edit().apply {
            putString("name", updated.name)
            putString("email", updated.email)
            putString("avatarEmoji", updated.avatarEmoji)
            putString("customApiKey", updated.customApiKey)
            putBoolean("autoVoiceReply", updated.autoVoiceReply)
            putFloat("voiceSpeed", updated.voiceSpeed)
            putFloat("voicePitch", updated.voicePitch)
            putString("defaultLanguage", updated.defaultLanguage)
            putString("preferredModel", updated.preferredModel)
            putBoolean("isSecureBackendActive", updated.isSecureBackendActive)
            putBoolean("biometricLockEnabled", updated.biometricLockEnabled)
            apply()
        }
        _profile.value = updated
    }
}
