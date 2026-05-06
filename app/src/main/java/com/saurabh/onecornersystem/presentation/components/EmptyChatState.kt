package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyChatState(
    isShopOwner: Boolean,
    accent: Color = ChatColors.AmberOrange
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(accent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💬",
                fontSize = 40.sp
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "No messages yet",
            color = ChatColors.TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
        Text(
            text = if (isShopOwner) "Start conversation from bookings" else "Tap chat button on a shop to start",
            color = ChatColors.TextGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
