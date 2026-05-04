package com.kaufee.bv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaufee.bv.domain.repository.TranslationRepository
import com.kaufee.bv.util.TranslationConstants
import com.kaufee.bv.util.TranslationException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranslationUiState(
    val sourceLanguage: String = "en",
    val targetLanguage: String = "hi",
    val sourceText: String = "",
    val translatedText: String = "",
    val isLoading: Boolean = false,
    val isTranslating: Boolean = false,
    val error: String? = null,
    val modelsDownloaded: Map<String, Boolean> = emptyMap(),
    val isOfflineReady: Boolean = false
)

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    // Debounce job — cancels previous call if user keeps typing
    private var translationJob: Job? = null

    init {
        checkDownloadedModels()
    }

    private fun checkDownloadedModels() {
        viewModelScope.launch {
            val models = mutableMapOf<String, Boolean>()
            TranslationConstants.SUPPORTED_LANGUAGES.forEach { language ->
                models[language.code] = translationRepository.isModelDownloaded(language.code)
            }
            val allReady = models.values.all { it }
            _uiState.value = _uiState.value.copy(
                modelsDownloaded = models,
                isOfflineReady = allReady
            )
        }
    }

    fun setSourceLanguage(language: String) {
        _uiState.value = _uiState.value.copy(sourceLanguage = language)
        scheduleTranslation()
    }

    fun setTargetLanguage(language: String) {
        _uiState.value = _uiState.value.copy(targetLanguage = language)
        scheduleTranslation()
    }

    fun setSourceText(text: String) {
        // Update text immediately so the keyboard is never blocked
        _uiState.value = _uiState.value.copy(sourceText = text)
        if (text.isBlank()) {
            translationJob?.cancel()
            _uiState.value = _uiState.value.copy(translatedText = "", isTranslating = false)
            return
        }
        scheduleTranslation()
    }

    /** Debounced: only translates 500 ms after the user stops typing */
    private fun scheduleTranslation() {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            delay(500L)
            performTranslation()
        }
    }

    fun translateNow() {
        translationJob?.cancel()
        viewModelScope.launch { performTranslation() }
    }

    fun swapLanguages() {
        val current = _uiState.value
        _uiState.value = current.copy(
            sourceLanguage = current.targetLanguage,
            targetLanguage = current.sourceLanguage,
            sourceText = current.translatedText,
            translatedText = current.sourceText
        )
        if (_uiState.value.sourceText.isNotBlank()) scheduleTranslation()
    }

    private suspend fun performTranslation() {
        val state = _uiState.value
        if (state.sourceText.isBlank()) return

        try {
            _uiState.value = _uiState.value.copy(isTranslating = true, error = null)
            val result = translationRepository.translate(
                state.sourceLanguage,
                state.targetLanguage,
                state.sourceText
            )
            _uiState.value = _uiState.value.copy(translatedText = result, isTranslating = false)
        } catch (e: TranslationException.TranslationFailedException) {
            _uiState.value = _uiState.value.copy(
                isTranslating = false,
                error = "Please download the language models first."
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isTranslating = false,
                error = "Translation failed: ${e.message}"
            )
        }
    }

    fun downloadLanguageModels() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val languagesToDownload = listOf(
                    _uiState.value.sourceLanguage,
                    _uiState.value.targetLanguage
                ).distinct()
                for (language in languagesToDownload) {
                    translationRepository.downloadLanguageModel(language).collect {}
                }
                checkDownloadedModels()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: TranslationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = when (e) {
                        is TranslationException.InsufficientStorageException -> "Not enough storage. Free up 500 MB."
                        is TranslationException.ModelDownloadException -> "Failed to download: ${e.message}"
                        else -> "Download failed: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Download failed: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}