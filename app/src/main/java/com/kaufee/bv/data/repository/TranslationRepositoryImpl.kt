package com.kaufee.bv.data.repository

import com.google.mlkit.nl.translate.*
import com.kaufee.bv.data.local.dao.HistoryDao
import com.kaufee.bv.data.local.entity.HistoryEntity
import com.kaufee.bv.domain.repository.TranslationRepository
import com.kaufee.bv.util.TranslationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : TranslationRepository {

    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String
    ): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(getCode(sourceLanguage))
            .setTargetLanguage(getCode(targetLanguage))
            .build()

        val translator = Translation.getClient(options)

        return suspendCancellableCoroutine { cont ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    private fun getCode(code: String) = when (code) {
        "en" -> TranslateLanguage.ENGLISH
        "hi" -> TranslateLanguage.HINDI
        "te" -> TranslateLanguage.TELUGU
        "ta" -> TranslateLanguage.TAMIL
        "mr" -> TranslateLanguage.MARATHI
        else -> throw TranslationException.InvalidLanguageException(code)
    }

    override fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    override suspend fun saveToHistory(history: HistoryEntity) = historyDao.insertHistory(history)
    override suspend fun deleteHistory(id: Long) = historyDao.deleteHistory(id)
    override suspend fun clearHistory() = historyDao.clearAllHistory()
    override fun downloadLanguageModel(language: String) = throw NotImplementedError()
    override suspend fun isModelDownloaded(language: String) = true
    override suspend fun deleteLanguageModel(language: String) = true
}