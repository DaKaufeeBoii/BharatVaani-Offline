package com.kaufee.bv.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
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
import com.kaufee.bv.ui.theme.*
import com.kaufee.bv.util.TranslationConstants

@Composable
fun LanguageSelectorRow(
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LanguagePill(
            languageCode = sourceLanguage,
            onLanguageSelected = onSourceLanguageChange,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable { onSwapClick() },
            shape = CircleShape,
            color = BV_Primary,
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Swap",
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        LanguagePill(
            languageCode = targetLanguage,
            onLanguageSelected = onTargetLanguageChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LanguagePill(
    languageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val language = TranslationConstants.getLanguageByCode(languageCode)

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language?.displayName ?: "Select",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            TranslationConstants.SUPPORTED_LANGUAGES.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.displayName) },
                    onClick = {
                        onLanguageSelected(lang.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TranslationCard(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    isSource: Boolean,
    modifier: Modifier = Modifier,
    onMicClick: (() -> Unit)? = null,
    onClearClick: (() -> Unit)? = null,
    onTtsClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    isTranslating: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSource) MaterialTheme.colorScheme.surface else Color(0xFFF5F6FF),
        tonalElevation = if (isSource) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(min = 160.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (text.isEmpty() && !isTranslating) {
                    Text(
                        text = placeholder,
                        color = Color.Gray.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (isSource) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                } else {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = BV_Primary)
                    } else {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSource) {
                    IconButton(onClick = { /* TODO: Camera */ }) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = "Camera", tint = Color.Gray)
                    }
                    IconButton(onClick = { onMicClick?.invoke() }) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.Gray)
                    }
                    Spacer(Modifier.weight(1f))
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { onClearClick?.invoke() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                } else {
                    IconButton(onClick = { onTtsClick?.invoke() }) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = "Speaker", tint = Color.Gray)
                    }
                    IconButton(onClick = { /* TODO: Bookmark */ }) {
                        Icon(Icons.Outlined.StarOutline, contentDescription = "Save", tint = Color.Gray)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onCopyClick?.invoke() }) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onHistoryClick: () -> Unit,
    onPhrasebookClick: () -> Unit,
    onOfflinePackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            QuickActionCard(
                label = "History", 
                icon = Icons.Default.History, 
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            QuickActionCard(
                label = "Phrasebook", 
                icon = Icons.Default.LibraryBooks, 
                onClick = onPhrasebookClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            QuickActionCard(
                label = "Offline Pak", 
                icon = Icons.Default.Language, 
                onClick = onOfflinePackClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            QuickActionCard(
                label = "Magic Key", 
                icon = Icons.Default.AutoAwesome, 
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    label: String, 
    icon: ImageVector, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = BV_Primary)
            Spacer(Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TranslationBanner(
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF8E8FFA), Color(0xFF4C4DDC))
                )
            )
    ) {
        Text(
            text = "B",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-10).dp),
            color = Color.White.copy(alpha = 0.1f),
            fontSize = 140.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                "Break language barriers instantly",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Real-time voice and text translation with BharatVaani.",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 24.dp)
                .size(56.dp)
                .clickable { onMicClick() },
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Voice",
                tint = Color.White,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
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
                Icon(Icons.Outlined.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer)
                Text(text = message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}
