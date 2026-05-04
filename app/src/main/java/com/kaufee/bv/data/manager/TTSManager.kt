package com.kaufee.bv.data.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(   // ← FIXED NAME
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val languageLocaleMap = mapOf(
        "en" to Locale.ENGLISH,
        "hi" to Locale("hi", "IN"),
        "te" to Locale("te", "IN"),
        "ta" to Locale("ta", "IN"),
        "mr" to Locale("mr", "IN")
    )

    init {
        tts = TextToSpeech(context) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (!isReady) Log.e("TtsManager", "TTS init failed with status $status")
        }
    }

    fun speak(text: String, languageCode: String) {
        if (!isReady || text.isBlank()) return
        val locale = languageLocaleMap[languageCode] ?: Locale.ENGLISH
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("TtsManager", "Language $languageCode not supported, falling back to English")
            tts?.setLanguage(Locale.ENGLISH)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bv_tts")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}