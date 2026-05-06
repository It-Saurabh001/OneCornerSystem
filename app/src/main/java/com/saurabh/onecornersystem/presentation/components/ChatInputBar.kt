package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatInputBar(
    onSendMessage: (String) -> Unit,
    onAttachImage: () -> Unit = {},
    enabled: Boolean = true
) {
    var messageText by remember { mutableStateOf("") }

    val canSend = messageText.isNotBlank() && enabled

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ChatColors.DeepBlack.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(ChatColors.OutlineWhite, Color.Transparent)),
                    RoundedCornerShape(28.dp)
                )
                .background(ChatColors.GlassWhite, RoundedCornerShape(28.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attach button
            IconButton(
                onClick = onAttachImage,
                enabled = enabled,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Check, // Replace with attachment icon
                    contentDescription = "Attach",
                    tint = if (enabled) ChatColors.TextGray else ChatColors.TextGray.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text field
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                placeholder = {
                    Text(
                        if (enabled) "Type a message..." else "Connecting...",
                        color = ChatColors.TextGray.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = ChatColors.AmberOrange,
                    focusedTextColor = ChatColors.TextWhite,
                    unfocusedTextColor = ChatColors.TextWhite,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = ChatColors.TextGray.copy(alpha = 0.4f),
                    disabledContainerColor = Color.Transparent
                ),
                maxLines = 4,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )

            // Send button
            IconButton(
                onClick = {
                    if (canSend) {
                        onSendMessage(messageText.trim())
                        messageText = ""
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (canSend) ChatColors.AmberOrange else ChatColors.OutlineWhite,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (canSend) Color.Black else ChatColors.TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}