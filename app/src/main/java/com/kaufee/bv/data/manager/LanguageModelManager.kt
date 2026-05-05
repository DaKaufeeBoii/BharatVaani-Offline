package com.kaufee.bv.data.manager

import android.content.Context
import android.os.StatFs
import com.google.mlkit.nl.translate.*
import com.kaufee.bv.util.TranslationException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LanguageModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MINIMUM_FREE_SPACE = 500 * 1024 * 1024L
    }

    private val translatorCache = mutableMapOf<String, Translator>()

    private fun getTranslator(languageCode: String): Translator =
        translatorCache.getOrPut(languageCode) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(getMlKitLanguageCode(languageCode))
                    .build()
            )
        }

    fun downloadLanguageModel(languageCode: String): Flow<String> = flow {
        if (!hasEnoughSpace()) throw TranslationException.InsufficientStorageException()

        emit("Downloading $languageCode model...")

        suspendCancellableCoroutine { cont ->
            getTranslator(languageCode)
                .downloadModelIfNeeded()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener {
                    cont.resumeWithException(
                        TranslationException.ModelDownloadException(languageCode, it.message ?: "Error")
                    )
                }
        }

        emit("$languageCode ready")
    }

    suspend fun isModelDownloaded(languageCode: String): Boolean =
        suspendCancellableCoroutine { cont ->
            getTranslator(languageCode)
                .downloadModelIfNeeded()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    private fun getMlKitLanguageCode(code: String): String =
        when (code.lowercase()) {
            "en" -> TranslateLanguage.ENGLISH
            "hi" -> TranslateLanguage.HINDI
            "te" -> TranslateLanguage.TELUGU
            "ta" -> TranslateLanguage.TAMIL
            "mr" -> TranslateLanguage.MARATHI
            else -> throw TranslationException.InvalidLanguageException(code)
        }

    private fun hasEnoughSpace(): Boolean {
        val stat = StatFs(context.cacheDir.absolutePath)
        return stat.availableBlocksLong * stat.blockSizeLong > MINIMUM_FREE_SPACE
    }
}