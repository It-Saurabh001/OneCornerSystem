package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.onecornersystem.data.model.Chat

// ========== CHAT LIST ITEM ==========
@Composable
fun ChatListItem(
    chat: Chat,
    isShopOwner: Boolean,
    onClick: () -> Unit
) {
    val displayName = if (isShopOwner) chat.userName else chat.shopName
    val displayImage = if (isShopOwner) chat.userProfileImage else chat.shopProfileImage
    val isUnread = chat.unreadCount > 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.linearGradient(listOf(ChatColors.OutlineWhite, Color.Transparent)),
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick),
        color = if (isUnread) ChatColors.AmberOrange.copy(alpha = 0.08f) else ChatColors.GlassWhite,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ChatColors.AmberOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (displayImage.isNotBlank()) {
                    Base64Image(
                        imageSource = displayImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = ChatColors.AmberOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
            }

            // Unread badge
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = (-6).dp, y = (-20).dp)
                        .background(ChatColors.AmberOrange, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        color = ChatColors.TextWhite,
                        fontWeight = if (isUnread) FontWeight.Black else FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (chat.lastMessageTime != null) {
                        Text(
                            text = formatChatTime(chat.lastMessageTime),
                            color = if (isUnread) ChatColors.AmberOrange else ChatColors.TextGray,
                            fontSize = 11.sp,
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.lastMessage.ifBlank { "No messages yet" },
                        color = if (isUnread) ChatColors.TextWhite else ChatColors.TextGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(ChatColors.AmberOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
