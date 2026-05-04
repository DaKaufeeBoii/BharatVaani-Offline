package com.kaufee.bv.util

sealed class TranslationException : Exception() {
    data class ModelDownloadException(val languagePair: String, override val message: String = "") : TranslationException()
    data class TranslationFailedException(val sourceLanguage: String, val targetLanguage: String, override val message: String = "") : TranslationException()
    data class InsufficientStorageException(override val message: String = "Insufficient storage space for language models") : TranslationException()
    data class NetworkException(override val message: String = "Network error while downloading models") : TranslationException()
    data class InvalidLanguageException(val language: String, override val message: String = "") : TranslationException()
}

object ErrorHandler {
    fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is TranslationException.ModelDownloadException -> "Failed to download ${exception.languagePair} model: ${exception.message}"
            is TranslationException.TranslationFailedException -> "Translation failed between ${exception.sourceLanguage} and ${exception.targetLanguage}"
            is TranslationException.InsufficientStorageException -> "Not enough storage space. Please free up at least 500MB."
            is TranslationException.NetworkException -> "Network error. Please check your internet connection."
            is TranslationException.InvalidLanguageException -> "Invalid language: ${exception.language}"
            else -> "An unexpected error occurred: ${exception.message}"
        }
    }

    fun getErrorTitle(exception: Exception): String {
        return when (exception) {
            is TranslationException.ModelDownloadException -> "Download Error"
            is TranslationException.TranslationFailedException -> "Translation Error"
            is TranslationException.InsufficientStorageException -> "Storage Error"
            is TranslationException.NetworkException -> "Network Error"
            is TranslationException.InvalidLanguageException -> "Invalid Language"
            else -> "Error"
        }
    }
}

