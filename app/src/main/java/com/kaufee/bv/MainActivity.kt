package com.kaufee.bv

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaufee.bv.data.manager.TtsManager
import com.kaufee.bv.data.manager.SttManager
import com.kaufee.bv.ui.components.LanguageChipRow
import com.kaufee.bv.ui.components.ErrorBanner
import com.kaufee.bv.ui.theme.BVTheme
import com.kaufee.bv.ui.theme.BrandOrange
import com.kaufee.bv.viewmodel.TranslationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()

    @Inject lateinit var ttsManager: TtsManager
    @Inject lateinit var sttManager: SttManager

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceInput()
        else Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BVTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val sttState by sttManager.state.collectAsStateWithLifecycle()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                // Show STT errors via the viewmodel error channel
                LaunchedEffect(sttState.error) {
                    sttState.error?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        sttManager.clearError()
                    }
                }

                TranslationScreen(
                    uiState = uiState,
                    isListening = sttState.isListening,
                    partialSttText = sttState.partialResult,
                    onSourceLanguageChange = { viewModel.setSourceLanguage(it) },
                    onTargetLanguageChange = { viewModel.setTargetLanguage(it) },
                    onSwap = { viewModel.swapLanguages() },
                    onSourceTextChange = { viewModel.setSourceText(it) },
                    onTranslateClick = { viewModel.translateNow() },
                    onCopyClick = {
                        val clip = ClipData.newPlainText("translated", uiState.translatedText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
                    },
                    onTtsClick = { ttsManager.speak(uiState.translatedText, uiState.targetLanguage) },
                    onMicClick = { requestMicAndListen() },
                    onDownloadClick = { viewModel.downloadLanguageModels() },
                    onDismissError = { viewModel.clearError() }
                )
            }
        }
    }

    private fun requestMicAndListen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED -> startVoiceInput()
            else -> micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceInput() {
        val sourceLanguage = viewModel.uiState.value.sourceLanguage
        if (sttManager.state.value.isListening) {
            sttManager.stopListening()
        } else {
            sttManager.startListening(sourceLanguage) { result ->
                viewModel.setSourceText(result)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        sttManager.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(
    uiState: com.kaufee.bv.viewmodel.TranslationUiState,
    isListening: Boolean,
    partialSttText: String,
    onSourceLanguageChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSwap: () -> Unit,
    onSourceTextChange: (String) -> Unit,
    onTranslateClick: () -> Unit,
    onCopyClick: () -> Unit,
    onTtsClick: () -> Unit,
    onMicClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDismissError: () -> Unit
) {
    val orange = BrandOrange

    Scaffold(
        containerColor = Color(0xFFF5F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (androidx.compose.foundation.isSystemInDarkTheme()) 
                                    R.drawable.bharatvaani_logo_dark 
                                else 
                                    R.drawable.bharatvaani_logo_light
                            ),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                "BharatVaani",
                                color = orange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                lineHeight = 20.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isOfflineReady) Color(0xFF22C55E) else Color(0xFFFBBF24))
                                )
                                Text(
                                    if (uiState.isOfflineReady) "OFFLINE READY" else "MODELS NEEDED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.isOfflineReady) Color(0xFF22C55E) else Color(0xFFFBBF24),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* future settings */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color(0xFF6B7280))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error banner
            if (uiState.error != null) {
                ErrorBanner(
                    message = uiState.error ?: "",
                    onDismiss = onDismissError
                )
            }

            // Language selector row
            LanguageChipRow(
                sourceLanguage = uiState.sourceLanguage,
                targetLanguage = uiState.targetLanguage,
                onSourceLanguageChange = onSourceLanguageChange,
                onTargetLanguageChange = onTargetLanguageChange,
                onSwapClick = onSwap
            )

            // Source text card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val displayText = if (isListening && partialSttText.isNotBlank())
                        partialSttText else uiState.sourceText

                    TextField(
                        value = displayText,
                        onValueChange = onSourceTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        placeholder = {
                            Text(
                                if (isListening) "Listening..." else "Enter text to translate...",
                                color = Color(0xFFBDBDBD),
                                fontSize = 16.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Camera icon placeholder
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = "TTS source",
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(22.dp)
                        )
                        if (uiState.sourceText.isNotBlank()) {
                            IconButton(onClick = { onSourceTextChange("") }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Clear",
                                    tint = Color(0xFFBDBDBD),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Translation output card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isTranslating) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = orange)
                            Text("Translating...", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                        }
                    } else {
                        Text(
                            uiState.translatedText.ifBlank { "Translation..." },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            color = if (uiState.translatedText.isBlank()) Color(0xFFBDBDBD) else Color(0xFF1A1A1A),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.translatedText.isNotBlank()) {
                            IconButton(onClick = onCopyClick, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = onTtsClick, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Outlined.VolumeUp, contentDescription = "Speak", tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Download banner
            AnimatedVisibility(!uiState.isOfflineReady, enter = fadeIn(), exit = fadeOut()) {
                Button(
                    onClick = onDownloadClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orange),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Downloading Models...", fontWeight = FontWeight.SemiBold, color = Color.White)
                    } else {
                        Text("Download Language Models", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            // Translate button
            Button(
                onClick = onTranslateClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                shape = RoundedCornerShape(28.dp),
                enabled = uiState.sourceText.isNotBlank() && !uiState.isTranslating
            ) {
                Text("🈶", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("Translate", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            // Mic FAB centered
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    onClick = onMicClick,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    containerColor = orange,
                    contentColor = Color.White
                ) {
                    if (isListening) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Outlined.Mic, contentDescription = "Voice input", modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}