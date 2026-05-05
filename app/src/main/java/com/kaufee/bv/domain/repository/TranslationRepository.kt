package com.kaufee.bv.domain.repository

import com.kaufee.bv.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translate(sourceLanguage: String, targetLanguage: String, text: String): String
    fun downloadLanguageModel(language: String): Flow<String>
    suspend fun isModelDownloaded(language: String): Boolean
    suspend fun deleteLanguageModel(language: String): Boolean
    
    // History methods
    fun getAllHistory(): Flow<List<HistoryEntity>>
    suspend fun saveToHistory(history: HistoryEntity)
    suspend fun deleteHistory(id: Long)
    suspend fun clearHistory()
}
