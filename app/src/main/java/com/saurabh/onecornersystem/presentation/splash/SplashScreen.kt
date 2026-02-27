package com.saurabh.onecornersystem.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    // Animation for fade-in effect
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade in animation
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 1500))

        // Show splash screen for 2 seconds
        delay(2000)

        // Navigate to home
        onNavigateToHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title with animation
        Text(
            text = "OneCorner",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.alpha(alpha.value)
        )

        Text(
            text = "System",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.alpha(alpha.value)
        )

        // Tagline
        Text(
            text = "Everything at One Corner",
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.alpha(alpha.value)
        )
    }
}

