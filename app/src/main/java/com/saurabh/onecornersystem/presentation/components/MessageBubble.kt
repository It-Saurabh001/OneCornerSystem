package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.onecornersystem.data.model.Message


@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isCurrentUser) ChatColors.SentBubble else ChatColors.ReceivedBubble
    val textColor = ChatColors.TextWhite
    val timeColor = if (isCurrentUser) ChatColors.AmberOrange.copy(alpha = 0.7f) else ChatColors.TextGray
    val shape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isCurrentUser) 20.dp else 4.dp,
        bottomEnd = if (isCurrentUser) 4.dp else 20.dp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Sender name (only for received messages)
        if (!isCurrentUser && message.senderName.isNotBlank()) {
            Text(
                text = message.senderName,
                color = ChatColors.AmberOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
            )
        }

        // If it's an image attachment
        if (message.attachmentType == "image" && message.attachmentUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(ChatColors.OutlineWhite, Color.Transparent)),
                        shape
                    )
                    .background(bubbleColor)
            ) {
                Base64Image(
                    imageSource = message.attachmentUrl,
                    contentDescription = "Image attachment",
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .heightIn(max = 280.dp)
                )
                // Time overlay on image
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.timeSent),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                    if (isCurrentUser) {
                        Icon(
                            if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (message.isRead) ChatColors.AmberOrange else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        } else {
            // Text message bubble
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(shape)
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(ChatColors.OutlineWhite, Color.Transparent)),
                        shape
                    )
                    .background(bubbleColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatMessageTime(message.timeSent),
                            color = timeColor,
                            fontSize = 10.sp
                        )
                        if (isCurrentUser) {
                            Icon(
                                if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (message.isRead) ChatColors.AmberOrange else timeColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
