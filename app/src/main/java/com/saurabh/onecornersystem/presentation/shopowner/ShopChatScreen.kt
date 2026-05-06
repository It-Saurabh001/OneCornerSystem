package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.presentation.common.ChatViewModel
import com.saurabh.onecornersystem.presentation.components.ChatColors
import com.saurabh.onecornersystem.presentation.components.ChatInputBar
import com.saurabh.onecornersystem.presentation.components.ChatLiquidBackground
import com.saurabh.onecornersystem.presentation.components.ChatMessagesList
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopChatScreen(
    navController: NavController,
    shopId: String,
    shopName: String,
    customerId: String,
    customerName: String,
    bookingId: String = "",   // Each booking gets its own chat room
    viewModel: ChatViewModel
) {
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val currentChat by viewModel.currentChat.collectAsStateWithLifecycle()

    // Initialise chat once on the correct shared VM instance
    LaunchedEffect(Unit) {
        Log.d("ShopChatScreen", "🟢 Opened — customer=$customerName bookingId=$bookingId")
        viewModel.startChatAsShopOwner(
            shopId       = shopId,
            shopName     = shopName,
            shopImage    = "",
            customerId   = customerId,
            customerName = customerName,
            customerImage = "",
            bookingId    = bookingId.ifBlank { null }
        )
    }

    // Reset state when leaving so next chat opens fresh
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ShopChatScreen", "🧹 Disposed — resetting chat states")
            viewModel.resetChatStates()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ChatColors.DeepBlack)) {
        ChatLiquidBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // 👇 TOP BAR (always visible)
            TopBar(currentChat, customerName, navController)

            HorizontalDivider(color = ChatColors.OutlineWhite, thickness = 1.dp)

            // 👇 MESSAGES AREA
            Box(modifier = Modifier.weight(1f)) {
                when (val state = messagesState) {
                    // 👇 LOADING - Messages abhi Firebase se aa rahi hain
                    is Resource.Loading -> {
                        LoadingMessagesView()
                    }

                    // 👇 SUCCESS - Messages mil gayi!
                    is Resource.Success -> {
                        if (state.data.isEmpty()) {
                            EmptyChatView()
                        } else {
                            ChatMessagesList(
                                messages = state.data,
                                currentUserId = viewModel.currentUserId
                            )
                        }
                    }

                    // 👇 ERROR - Kuch problem ho gayi
                    is Resource.Error -> {
                        ErrorLoadingView(
                            message = state.message ?: "Failed to load messages",
                            onRetry = {
                                currentChat?.let { viewModel.listenToMessages(it.chatId) }
                            }
                        )
                    }

                    // 👇 IDLE - Initializing the chat in the background
                    is Resource.Idle -> {
                        LoadingMessagesView() // We can show loading here too since LaunchedEffect runs immediately
                    }
                }
            }

            // 👇 INPUT BAR (message bhejne ke liye)
            ChatInputBar(
                onSendMessage = { text -> viewModel.sendMessage(text) }
            )
        }
    }
}

// ============= TOP BAR =============
@Composable
private fun TopBar(currentChat: Chat?, fallbackName: String, navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ChatColors.DeepBlack.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ChatColors.TextWhite)
            }

            val displayName = currentChat?.userName ?: fallbackName

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ChatColors.AmberOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    color = ChatColors.AmberOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name & Status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = ChatColors.TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text("Online", color = ChatColors.TextGray, fontSize = 11.sp)
            }
        }
    }
}

// ============= LOADING VIEW =============
@Composable
fun LoadingMessagesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = ChatColors.AmberOrange,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Loading messages...",
                color = ChatColors.TextGray,
                fontSize = 14.sp
            )
        }
    }
}

// ============= ERROR VIEW =============
@Composable
fun ErrorLoadingView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                null,
                tint = ChatColors.TextGray,
                modifier = Modifier.size(48.dp)
            )
            Text(
                message,
                color = ChatColors.TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = ChatColors.AmberOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETRY", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============= EMPTY CHAT VIEW =============
@Composable
fun EmptyChatView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("💬", fontSize = 48.sp)
            Text(
                "No messages yet",
                color = ChatColors.TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                "Send a message to start the conversation",
                color = ChatColors.TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}