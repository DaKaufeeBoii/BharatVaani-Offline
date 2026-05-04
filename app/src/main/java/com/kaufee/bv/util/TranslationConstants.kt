package com.kaufee.bv.util

import com.google.mlkit.nl.translate.TranslateLanguage

object TranslationConstants {
    // Supported languages
    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String,
        val flag: String
    )

    val SUPPORTED_LANGUAGES = listOf(
        Language("en", "English", "English", "🇬🇧"),
        Language("hi", "Hindi", "हिन्दी", "🇮🇳"),
        Language("te", "Telugu", "తెలుగు", "🇮🇳"),
        Language("ta", "Tamil", "தமிழ்", "🇮🇳"),
        Language("mr", "Marathi", "मराठी", "🇮🇳")
    )

    // ML Kit language codes mapping
    val MLKIT_LANGUAGE_CODES = mapOf(
        "en" to TranslateLanguage.ENGLISH,
        "hi" to TranslateLanguage.HINDI,
        "te" to TranslateLanguage.TELUGU,
        "ta" to TranslateLanguage.TAMIL,
        "mr" to TranslateLanguage.MARATHI
    )

    fun getLanguageByCode(code: String): Language? {
        return SUPPORTED_LANGUAGES.find { it.code == code }
    }

    fun getMLKitLanguageCode(code: String): String? {
        return MLKIT_LANGUAGE_CODES[code]
    }
}

