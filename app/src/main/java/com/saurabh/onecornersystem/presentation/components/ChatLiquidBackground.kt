package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ========== CHAT COLOR PALETTE ==========
object ChatColors {
    val DeepBlack = Color(0xFF0A0A0A)
    val AmberOrange = Color(0xFFFF9100)
    val GlassWhite = Color.White.copy(alpha = 0.05f)
    val OutlineWhite = Color.White.copy(alpha = 0.1f)
    val TextWhite = Color.White
    val TextGray = Color(0xFF9E9E9E)
    val SentBubble = AmberOrange.copy(alpha = 0.15f)
    val ReceivedBubble = Color.White.copy(alpha = 0.08f)
    val TypingIndicator = AmberOrange.copy(alpha = 0.3f)
}

// ========== LIQUID BLOBS BACKGROUND ==========
@Composable
fun ChatLiquidBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top right blob
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 160.dp, y = (-80).dp)
                .blur(100.dp)
                .background(ChatColors.AmberOrange.copy(alpha = 0.10f), CircleShape)
        )
        // Bottom left blob
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-60).dp, y = 500.dp)
                .blur(90.dp)
                .background(ChatColors.AmberOrange.copy(alpha = 0.07f), CircleShape)
        )
        // Center subtle blob
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 100.dp, y = 300.dp)
                .blur(70.dp)
                .background(ChatColors.AmberOrange.copy(alpha = 0.05f), CircleShape)
        )
    }
}