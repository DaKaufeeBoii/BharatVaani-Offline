package com.kaufee.bv.ui.splash

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        super.onCreate(savedInstanceState)
        setContent {
            BVTheme {
                SplashScreenContent(onNavigateToMain = {
                    val intent = Intent(this, MainActivity::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val options = ActivityOptions.makeCustomAnimation(
                            this,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        startActivity(intent, options.toBundle())
                    } else {
                        startActivity(intent)
                        @Suppress("DEPRECATION")
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreenContent(onNavigateToMain: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        // Animation sequence for a more professional feel
        scale.animateTo(1f, tween(1000))
        alpha.animateTo(1f, tween(1000))
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
            // Logo selection based on theme
            Image(
                painter = painterResource(
                    id = if (isDark) R.drawable.bharatvaani_logo_dark else R.drawable.bharatvaani_logo_light
                ),
                contentDescription = "BharatVaani Logo",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 16.dp)
            )

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
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(48.dp))

            CircularProgressIndicator(
                color = BrandOrange,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )

            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Initializing...",
                color = Color(0xFFBDBDBD),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }

        // Bottom tagline or supported languages
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "English • Hindi • Telugu • Tamil • Marathi",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Made in India with ❤️",
                color = Color(0xFFD1D5DB),
                fontSize = 10.sp
            )
        }
    }
}
