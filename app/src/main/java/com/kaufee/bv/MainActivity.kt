package com.kaufee.bv

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaufee.bv.data.manager.TtsManager
import com.kaufee.bv.data.manager.SttManager
import com.kaufee.bv.ui.components.*
import com.kaufee.bv.ui.screens.HistoryScreen
import com.kaufee.bv.ui.screens.PhrasebookScreen
import com.kaufee.bv.ui.screens.SettingsScreen
import com.kaufee.bv.ui.theme.BVTheme
import com.kaufee.bv.ui.theme.BV_Primary
import com.kaufee.bv.ui.theme.BV_Secondary
import com.kaufee.bv.viewmodel.TranslationViewModel
import com.kaufee.bv.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Translate : Screen("translate", "Translate", Icons.Default.Translate)
    object History : Screen("history", "History", Icons.Default.History)
    object Saved : Screen("saved", "Saved", Icons.Default.BookmarkBorder)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject lateinit var ttsManager: TtsManager
    @Inject lateinit var sttManager: SttManager

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceInput()
        else Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            
            BVTheme(themeMode = settingsState.themeMode) {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val sttState by sttManager.state.collectAsStateWithLifecycle()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                // Show STT errors
                LaunchedEffect(sttState.error) {
                    sttState.error?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        sttManager.clearError()
                    }
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            val screens = listOf(Screen.Translate, Screen.History, Screen.Saved)
                            
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                                    label = { Text(screen.label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BV_Primary,
                                        selectedTextColor = BV_Primary,
                                        indicatorColor = BV_Secondary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Translate.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Translate.route) {
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
                                    Toast.makeText(this@MainActivity, "Copied!", Toast.LENGTH_SHORT).show()
                                },
                                onShareClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, uiState.translatedText)
                                    }
                                    startActivity(Intent.createChooser(intent, "Share translation via"))
                                },
                                onTtsClick = { 
                                    window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                                    ttsManager.speak(uiState.translatedText, uiState.targetLanguage) 
                                },
                                onSourceTtsClick = {
                                    window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                                    ttsManager.speak(uiState.sourceText, uiState.sourceLanguage)
                                },
                                onMicClick = { requestMicAndListen() },
                                onSettingsClick = { navController.navigate("settings") },
                                onHistoryClick = { navController.navigate(Screen.History.route) },
                                onPhrasebookClick = { navController.navigate(Screen.Saved.route) },
                                onDismissError = { viewModel.clearError() }
                            )
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(
                                history = uiState.history,
                                onBack = { navController.popBackStack() },
                                onDelete = { viewModel.deleteHistoryItem(it) },
                                onClearAll = { viewModel.clearAllHistory() }
                            )
                        }
                        composable(Screen.Saved.route) {
                            PhrasebookScreen(
                                onBack = { navController.popBackStack() },
                                onTranslate = { text ->
                                    viewModel.setSourceText(text)
                                    navController.navigate(Screen.Translate.route) {
                                        popUpTo(navController.graph.findStartDestination().id)
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                viewModel = settingsViewModel
                            )
                        }
                    }
                }
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
            window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            sttManager.startListening(sourceLanguage) { result ->
                viewModel.appendSourceText(result)
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
    onShareClick: () -> Unit,
    onTtsClick: () -> Unit,
    onSourceTtsClick: () -> Unit,
    onMicClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPhrasebookClick: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = BV_Primary
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "BharatVaani",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BV_Primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isOfflineReady) Color(0xFF4CAF50) else Color(0xFFFFC107))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isOfflineReady) "OFFLINE READY" else "MODELS NEEDED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isOfflineReady) Color(0xFF4CAF50) else Color(0xFFFFC107)
                        )
                    }
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
            }
        }

        // Language Selector
        LanguageSelectorRow(
            sourceLanguage = uiState.sourceLanguage,
            targetLanguage = uiState.targetLanguage,
            onSourceLanguageChange = onSourceLanguageChange,
            onTargetLanguageChange = onTargetLanguageChange,
            onSwapClick = onSwap,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Input Card
        TranslationCard(
            text = if (isListening && partialSttText.isNotBlank()) partialSttText else uiState.sourceText,
            onTextChange = onSourceTextChange,
            placeholder = if (isListening) "Listening..." else "Enter text to translate...",
            isSource = true,
            onMicClick = onMicClick,
            onClearClick = { onSourceTextChange("") },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Output Card
        TranslationCard(
            text = uiState.translatedText,
            onTextChange = {},
            placeholder = "Translation...",
            isSource = false,
            onTtsClick = onTtsClick,
            onCopyClick = onCopyClick,
            isTranslating = uiState.isTranslating,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Translate Button
        Button(
            onClick = onTranslateClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BV_Secondary),
            enabled = uiState.sourceText.isNotBlank() && !uiState.isTranslating
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Translate, contentDescription = null, tint = BV_Primary)
                Spacer(Modifier.width(8.dp))
                Text("Translate", color = BV_Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Quick Actions",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        QuickActionsGrid(
            onHistoryClick = onHistoryClick,
            onPhrasebookClick = onPhrasebookClick,
            onOfflinePackClick = onSettingsClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Bottom Banner
        TranslationBanner(
            onMicClick = onMicClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(32.dp))
    }
}
