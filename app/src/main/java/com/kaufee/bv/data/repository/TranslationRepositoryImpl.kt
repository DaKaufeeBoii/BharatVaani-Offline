package com.kaufee.bv.data.repository

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.kaufee.bv.data.manager.LanguageModelManager
import com.kaufee.bv.domain.repository.TranslationRepository
import com.kaufee.bv.util.TranslationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val modelManager: LanguageModelManager
) : TranslationRepository {

    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String
    ): String {
        if (text.isBlank()) return ""

        return try {
            val sourceCode = getMlKitLanguageCode(sourceLanguage)
            val targetCode = getMlKitLanguageCode(targetLanguage)

            if (!modelManager.isModelDownloaded(sourceLanguage) ||
                !modelManager.isModelDownloaded(targetLanguage)
            ) {
                throw TranslationException.TranslationFailedException(
                    sourceLanguage,
                    targetLanguage,
                    "Translation models not downloaded"
                )
            }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()

            val translator = Translation.getClient(options)

            suspendCancellableCoroutine<String> { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        continuation.resume(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWith(
                            Result.failure(
                                TranslationException.TranslationFailedException(
                                    sourceLanguage,
                                    targetLanguage,
                                    exception.message ?: "Unknown translation error"
                                )
                            )
                        )
                    }
            }
        } catch (e: Exception) {
            when (e) {
                is TranslationException -> throw e
                else -> throw TranslationException.TranslationFailedException(
                    sourceLanguage,
                    targetLanguage,
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    override fun downloadLanguageModel(language: String): Flow<String> {
        return modelManager.downloadLanguageModel(language)
    }

    override suspend fun isModelDownloaded(language: String): Boolean {
        return modelManager.isModelDownloaded(language)
    }

    override suspend fun deleteLanguageModel(language: String): Boolean {
        return try {
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getMlKitLanguageCode(languageCode: String): String {
        return when (languageCode.lowercase()) {
            "en" -> TranslateLanguage.ENGLISH
            "hi" -> TranslateLanguage.HINDI
            "te" -> TranslateLanguage.TELUGU
            "ta" -> TranslateLanguage.TAMIL
            "mr" -> TranslateLanguage.MARATHI
            else -> throw TranslationException.InvalidLanguageException(languageCode)
        }
    }
}