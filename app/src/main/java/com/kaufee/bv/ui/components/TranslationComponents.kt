package com.kaufee.bv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaufee.bv.util.TranslationConstants

private val Orange = Color(0xFFEA580C)

/**
 * Clean pill-style language row matching the BharatVaani design:
 *   [ English ]  🔀  [ Hindi ]
 */
@Composable
fun LanguageChipRow(
    sourceLanguage: String,
    targetLanguage: String,
    onSourceLanguageChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LanguageChip(
            selectedLanguage = sourceLanguage,
            onLanguageSelected = onSourceLanguageChange,
            modifier = Modifier.weight(1f)
        )

        FloatingActionButton(
            onClick = onSwapClick,
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            containerColor = Orange,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Outlined.SwapHoriz, contentDescription = "Swap languages", modifier = Modifier.size(22.dp))
        }

        LanguageChip(
            selectedLanguage = targetLanguage,
            onLanguageSelected = onTargetLanguageChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LanguageChip(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val lang = TranslationConstants.getLanguageByCode(selectedLanguage)

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded.value = !expanded.value },
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    lang?.displayName ?: "Select",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            TranslationConstants.SUPPORTED_LANGUAGES.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(language.flag, fontSize = 18.sp)
                            Column {
                                Text(language.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(language.nativeName, fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }
                    },
                    onClick = {
                        onLanguageSelected(language.code)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = message.isNotEmpty(), enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                Text(text = message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Dismiss", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}