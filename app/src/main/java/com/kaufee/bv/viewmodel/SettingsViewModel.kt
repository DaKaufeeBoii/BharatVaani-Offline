package com.kaufee.bv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaufee.bv.domain.repository.TranslationRepository
import com.kaufee.bv.util.TranslationConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val speakTranslatedText: Boolean = true,
    val autoDetectLanguage: Boolean = true,
    val modelStatus: Map<String, Boolean> = emptyMap(),
    val isDownloading: Map<String, Boolean> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshModelStatus()
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setSpeakTranslatedText(enabled: Boolean) {
        _uiState.update { it.copy(speakTranslatedText = enabled) }
    }

    fun setAutoDetectLanguage(enabled: Boolean) {
        _uiState.update { it.copy(autoDetectLanguage = enabled) }
    }

    fun refreshModelStatus() {
        viewModelScope.launch {
            val status = TranslationConstants.SUPPORTED_LANGUAGES.associate { 
                it.code to translationRepository.isModelDownloaded(it.code)
            }
            _uiState.update { it.copy(modelStatus = status) }
        }
    }

    fun downloadModel(languageCode: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isDownloading = it.isDownloading + (languageCode to true)) }
                translationRepository.downloadLanguageModel(languageCode).collect {}
                refreshModelStatus()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to download $languageCode: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isDownloading = it.isDownloading - languageCode) }
            }
        }
    }

    fun deleteModel(languageCode: String) {
        viewModelScope.launch {
            translationRepository.deleteLanguageModel(languageCode)
            refreshModelStatus()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
