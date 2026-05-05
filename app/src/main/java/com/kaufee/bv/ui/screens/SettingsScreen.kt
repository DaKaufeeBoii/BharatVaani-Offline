package com.kaufee.bv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaufee.bv.ui.theme.BV_Primary
import com.kaufee.bv.util.TranslationConstants
import com.kaufee.bv.viewmodel.SettingsViewModel
import com.kaufee.bv.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModelManager by remember { mutableStateOf(false) }

    if (showModelManager) {
        ModelManagerDialog(
            uiState = uiState,
            onDismiss = { showModelManager = false },
            onDownload = { viewModel.downloadModel(it) },
            onDelete = { viewModel.deleteModel(it) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF8E8FFA), Color(0xFF4C4DDC))
                    )
                )
        ) {
            Text(
                "B",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White.copy(alpha = 0.1f),
                fontSize = 140.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    "Settings",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Personalize your translation experience",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection("OFFLINE TRANSLATION") {
                    val downloadedCount = uiState.modelStatus.values.count { it }
                    SettingsRow(
                        icon = Icons.Outlined.CloudDownload,
                        title = "Manage downloaded languages",
                        subtitle = "$downloadedCount languages",
                        onClick = { showModelManager = true }
                    )
                }
            }

            item {
                SettingsSection("VOICE & AUDIO") {
                    SettingsToggleRow(
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        title = "Speak translated text",
                        checked = uiState.speakTranslatedText,
                        onCheckedChange = { viewModel.setSpeakTranslatedText(it) }
                    )
                    SettingsToggleRow(
                        icon = Icons.Outlined.Mic,
                        title = "Auto-detect language",
                        checked = uiState.autoDetectLanguage,
                        onCheckedChange = { viewModel.setAutoDetectLanguage(it) }
                    )
                }
            }

            item {
                SettingsSection("DISPLAY") {
                    SettingsToggleRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        checked = uiState.themeMode == ThemeMode.DARK,
                        onCheckedChange = { 
                            viewModel.setThemeMode(if (it) ThemeMode.DARK else ThemeMode.LIGHT) 
                        }
                    )
                }
            }

            item {
                SettingsSection("ACCOUNT") {
                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        title = "Profile settings",
                        onClick = { /* TODO */ }
                    )
                }
            }

            item {
                SettingsSection("ABOUT") {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        subtitle = "2.4.1-stable"
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "Help & Feedback",
                        onClick = { /* TODO */ }
                    )
                }
            }
            
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.padding(12.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Enjoying BharatVaani?", fontWeight = FontWeight.Bold)
                        Text("Rate us on the store", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ModelManagerDialog(
    uiState: com.kaufee.bv.viewmodel.SettingsUiState,
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Offline Languages", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(TranslationConstants.SUPPORTED_LANGUAGES) { language ->
                    val isDownloaded = uiState.modelStatus[language.code] ?: false
                    val isDownloading = uiState.isDownloading[language.code] ?: false

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${language.flag} ${language.displayName}", fontWeight = FontWeight.Medium)
                            Text(language.nativeName, fontSize = 12.sp, color = Color.Gray)
                        }

                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else if (isDownloaded) {
                            IconButton(onClick = { onDelete(language.code) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        } else {
                            IconButton(onClick = { onDownload(language.code) }) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Download", tint = BV_Primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = BV_Primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BV_Primary)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = if (subtitle.contains("languages")) Color(0xFF4CAF50) else Color.Gray)
            }
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BV_Primary)
        Spacer(Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2E7D32)
            )
        )
    }
}
