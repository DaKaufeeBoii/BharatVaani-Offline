package com.kaufee.bv.domain.repository

import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translate(sourceLanguage: String, targetLanguage: String, text: String): String
    fun downloadLanguageModel(language: String): Flow<String>
    suspend fun isModelDownloaded(language: String): Boolean
    suspend fun deleteLanguageModel(language: String): Boolean
}

