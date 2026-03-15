package com.saurabh.onecornersystem.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    // --- THEME COLORS ---
    val electricBlue = Color(0xFF2979FF)
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    // --- ANIMATION STATES ---
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }

    // Liquid Movement Animation
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")
    val blobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset"
    )

    LaunchedEffect(Unit) {
        // Fade in + Scale up animation
        alpha.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))

        delay(2200) // Splash display time
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(deepBlack),
        contentAlignment = Alignment.Center
    ) {
        // --- 1. DUAL LIQUID BLOBS (Blue & Orange Mix) ---
        // Blue Blob (Top Left)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp + blobOffset.dp, y = (-100).dp)
                .blur(120.dp)
                .background(electricBlue.copy(alpha = 0.15f), CircleShape)
        )

        // Orange Blob (Bottom Right)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 50.dp - blobOffset.dp, y = 100.dp)
                .blur(120.dp)
                .background(amberOrange.copy(alpha = 0.15f), CircleShape)
        )

        // --- 2. LOGO CONTENT ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // Gradient Text Logo
            Text(
                text = "OneCorner",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                style = LocalTextStyle.current.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(electricBlue, amberOrange)
                    )
                )
            )

            Text(
                text = "ULTIMATE SYSTEM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 4.sp,
                modifier = Modifier.offset(y = (-4).dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Glassy Tagline
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(0.5.dp, outlineWhite, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(electricBlue, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Everything at One Corner",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(6.dp).background(amberOrange, CircleShape))
                }
            }
        }

        // --- 3. LOADING INDICATOR ---
        LinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .width(150.dp)
                .height(2.dp)
                .alpha(alpha.value * 0.5f),
            color = amberOrange,
            trackColor = electricBlue.copy(alpha = 0.2f)
        )
    }
}

// --- MIXED THEME ERROR CARD ---
@Composable
fun SplashErrorCardMixed(msg: String, onRetry: () -> Unit) {
    val electricBlue = Color(0xFF2979FF)
    val amberOrange = Color(0xFFFF9100)

    Surface(
        modifier = Modifier.padding(24.dp).fillMaxWidth().border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(28.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connection Interrupted", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(msg, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(electricBlue, amberOrange))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RETRY CONNECTION", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun SplashMixedPreview() {
    MaterialTheme {
        SplashScreen(onNavigateToHome = {})
    }
}