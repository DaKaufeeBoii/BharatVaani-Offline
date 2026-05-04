package com.kaufee.bv.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaufee.bv.MainActivity
import com.kaufee.bv.ui.theme.BVTheme
import com.kaufee.bv.ui.theme.BrandOrange
import com.kaufee.bv.viewmodel.TranslationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SplashScreenActivity : ComponentActivity() {
    private val viewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BVTheme {
                SplashScreenContent(onNavigateToMain = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreenContent(onNavigateToMain: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(1000))
        alpha.animateTo(1f, tween(1000))
        delay(2500)
        onNavigateToMain()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.alpha(alpha.value).scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandOrange),
                contentAlignment = Alignment.Center
            ) {
                Text("B", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "BharatVaani",
                    color = BrandOrange,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
                Text(
                    "Indian Language Translator",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(Modifier.height(24.dp))

            CircularProgressIndicator(color = BrandOrange, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)

            Text("Initializing...", color = Color(0xFFBDBDBD), fontSize = 12.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("English • Hindi • Telugu • Tamil • Marathi", color = Color(0xFFD1D5DB), fontSize = 11.sp)
        }
    }
}