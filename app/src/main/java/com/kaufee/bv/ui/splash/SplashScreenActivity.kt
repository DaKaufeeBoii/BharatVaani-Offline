package com.kaufee.bv.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kaufee.bv.MainActivity
import com.kaufee.bv.R
import com.kaufee.bv.ui.theme.BVTheme
import com.kaufee.bv.ui.theme.BrandOrange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Modern Splash Screen API to bridge the gap and prevent the "white flash"
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            BVTheme {
                SplashScreenContent(onNavigateToMain = {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    // Activity cross-fade transition
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreenContent(onNavigateToMain: () -> Unit) {
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0f) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        // High-end entrance animation
        scale.animateTo(1f, tween(800, easing = LinearEasing))
        alpha.animateTo(1f, tween(600))
        delay(2000)
        onNavigateToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isDark) 
                                listOf(Color(0xFF1E1E1E), Color(0xFF121212)) 
                            else 
                                listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5))
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (isDark) R.drawable.bharatvaani_logo_dark else R.drawable.bharatvaani_logo_light
                    ),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BharatVaani",
                    color = BrandOrange,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Indian Language Translator",
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(64.dp))

            CircularProgressIndicator(
                color = BrandOrange,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "English • Hindi • Telugu • Tamil • Marathi",
                color = if (isDark) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Made in India with ❤️",
                color = if (isDark) Color(0xFF374151) else Color(0xFFD1D5DB),
                fontSize = 10.sp
            )
        }
    }
}
