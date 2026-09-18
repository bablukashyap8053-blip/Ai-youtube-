package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class VoiceReplyManager(context: Context) {
    private val TAG = "VoiceReplyManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var currentPitch: Float = 1.0f
    private var currentSpeed: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            } else {
                Log.e(TAG, "TTS Initialization failed")
            }
        }
    }

    fun setPitchAndSpeed(pitch: Float, speed: Float) {
        currentPitch = pitch
        currentSpeed = speed
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) return

        stop()

        // Clean markdown symbols for natural speech
        val cleanText = text
            .replace(Regex("```[a-zA-Z]*"), "")
            .replace("```", "")
            .replace(Regex("[*#_~`]"), "")
            .replace("\n\n", ". ")
            .trim()

        if (cleanText.isBlank()) return

        // Auto-detect Hindi vs English
        val hasHindiCharacters = cleanText.any { it.code in 0x0900..0x097F }
        val targetLocale = if (hasHindiCharacters) Locale("hi", "IN") else Locale.US

        tts?.let { engine ->
            val langResult = engine.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default
                engine.setLanguage(Locale.getDefault())
            }
            engine.setPitch(currentPitch)
            engine.setSpeechRate(currentSpeed)

            val utteranceId = UUID.randomUUID().toString()
            engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            _isSpeaking.value = true
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
