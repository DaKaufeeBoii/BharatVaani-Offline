package com.kaufee.bv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaufee.bv.ui.theme.BrandOrange
import com.kaufee.bv.util.TranslationConstants
import com.kaufee.bv.viewmodel.SettingsViewModel
import com.kaufee.bv.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            item {
                Text("Appearance", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BrandOrange)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ThemeOption("System Default", ThemeMode.SYSTEM, uiState.themeMode) { viewModel.setThemeMode(it) }
                        ThemeOption("Light Mode", ThemeMode.LIGHT, uiState.themeMode) { viewModel.setThemeMode(it) }
                        ThemeOption("Dark Mode", ThemeMode.DARK, uiState.themeMode) { viewModel.setThemeMode(it) }
                    }
                }
            }

            // Models Section
            item {
                Text("Language Models", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BrandOrange)
                Text("Offline translation requires downloading models (~30MB each).", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(TranslationConstants.SUPPORTED_LANGUAGES) { language ->
                val isDownloaded = uiState.modelStatus[language.code] ?: false
                val isDownloading = uiState.isDownloading[language.code] ?: false

                LanguageModelItem(
                    language = language,
                    isDownloaded = isDownloaded,
                    isDownloading = isDownloading,
                    onDownload = { viewModel.downloadModel(language.code) },
                    onDelete = { viewModel.deleteModel(language.code) }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    mode: ThemeMode,
    selectedMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        RadioButton(
            selected = mode == selectedMode,
            onClick = { onSelect(mode) }
        )
    }
}

@Composable
fun LanguageModelItem(
    language: TranslationConstants.Language,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(language.flag, fontSize = 24.sp)
            Column {
                Text(language.displayName, fontWeight = FontWeight.Medium)
                Text(language.nativeName, fontSize = 12.sp, color = Color.Gray)
            }
        }

        if (isDownloading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else if (isDownloaded) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
            }
        } else {
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.CloudDownload, contentDescription = "Download", tint = BrandOrange)
            }
        }
    }
}
