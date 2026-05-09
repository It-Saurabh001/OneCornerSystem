package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.onecornersystem.data.model.Chat

/**
 * Chat list card with:
 *  - Service name subtitle ("John Doe – Plumbing Service")
 *  - Per-role unread badge (red number)
 *  - Long-press to trigger delete callback
 *
 * @param isShopOwner  true when rendered in ShopChatListScreen
 * @param onClick      single tap → open chat
 * @param onLongClick  long press → show delete dialog (null = no long-press action)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: Chat,
    isShopOwner: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    // ── Display values ────────────────────────────────────────────────────────
    val displayName  = if (isShopOwner) chat.userName  else chat.shopName
    val displayImage = if (isShopOwner) chat.userProfileImage else chat.shopProfileImage

    // Per-role unread counter: shop owner cares about shopUnreadCount,
    // customer cares about customerUnreadCount.
    val unreadCount = if (isShopOwner) chat.shopUnreadCount else chat.customerUnreadCount
    val isUnread    = unreadCount > 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.linearGradient(listOf(ChatColors.OutlineWhite, Color.Transparent)),
                RoundedCornerShape(24.dp)
            )
            .combinedClickable(
                onClick    = onClick,
                onLongClick = onLongClick ?: {}
            ),
        color  = if (isUnread) ChatColors.AmberOrange.copy(alpha = 0.08f) else ChatColors.GlassWhite,
        shape  = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ChatColors.AmberOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (displayImage.isNotBlank()) {
                    Base64Image(
                        imageSource      = displayImage,
                        contentDescription = null,
                        modifier         = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text       = displayName.take(1).uppercase(),
                        color      = ChatColors.AmberOrange,
                        fontWeight = FontWeight.Black,
                        fontSize   = 22.sp
                    )
                }
            }

            // Small unread dot on avatar (orange dot)
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = (-6).dp, y = (-20).dp)
                        .background(Color.Red, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // ── Content ───────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    // Primary name (customer name or shop name)
                    Text(
                        text       = displayName,
                        color      = ChatColors.TextWhite,
                        fontWeight = if (isUnread) FontWeight.Black else FontWeight.Bold,
                        fontSize   = 16.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    if (chat.lastMessageTime != null) {
                        Text(
                            text       = formatChatTime(chat.lastMessageTime),
                            color      = if (isUnread) ChatColors.AmberOrange else ChatColors.TextGray,
                            fontSize   = 11.sp,
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // ── Service name subtitle ─────────────────────────────────────
                // Shows "Plumbing Service" for shop owner, or "Shop Name – Plumbing Service"
                // for customer, so each card is distinguishable even when same names repeat.
                if (chat.serviceName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val serviceLabel = if (isShopOwner) {
                        chat.serviceName
                    } else {
                        // Customer sees shopName already in displayName, so just show service
                        chat.serviceName
                    }
                    Text(
                        text       = serviceLabel,
                        color      = ChatColors.AmberOrange.copy(alpha = 0.8f),
                        fontSize   = 11.sp,
                        fontStyle  = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                } else if (chat.bookingId.isNotBlank()) {
                    // Fallback: show a booking reference if no serviceName stored
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text     = "Booking #${chat.bookingId.take(8)}",
                        color    = ChatColors.TextGray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = chat.lastMessage.ifBlank { "No messages yet" },
                        color    = if (isUnread) ChatColors.TextWhite else ChatColors.TextGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // ── Red unread count badge ─────────────────────────────────
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color.Red, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = if (unreadCount > 99) "99+" else "$unreadCount",
                                color      = Color.White,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
