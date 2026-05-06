package com.saurabh.onecornersystem.presentation.customer

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.presentation.common.ChatViewModel
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.components.ChatColors
import com.saurabh.onecornersystem.presentation.components.ChatInputBar
import com.saurabh.onecornersystem.presentation.components.ChatLiquidBackground
import com.saurabh.onecornersystem.presentation.components.ChatMessagesList
import com.saurabh.onecornersystem.utils.Resource

private const val TAG = "CustomerChatScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    // Nav args — populated from NavGraph, drive the LaunchedEffect below
    bookingId: String = "",
    shopId: String = "",
    shopName: String = "",
    shopImage: String = ""
) {
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val currentChat by viewModel.currentChat.collectAsStateWithLifecycle()
    val createChatState by viewModel.createChatState.collectAsStateWithLifecycle()

    // Kick off chat initialisation once, on the correct VM instance.
    // bookingId/shopId/shopName come from nav args so they are stable across recompositions.
    LaunchedEffect(bookingId, shopId) {
        Log.d(TAG, "🚀 LaunchedEffect — shopId=$shopId bookingId=$bookingId shopName=$shopName")
        if (shopId.isNotBlank()) {
            if (bookingId.isNotBlank()) {
                viewModel.startChatFromBooking(
                    shopId    = shopId,
                    shopName  = shopName,
                    shopImage = shopImage,
                    bookingId = bookingId
                )
            } else {
                viewModel.startChatFromShop(
                    shopId    = shopId,
                    shopName  = shopName,
                    shopImage = shopImage
                )
            }
        } else {
            Log.e(TAG, "❌ No shopId passed — chat cannot be initialised")
        }
    }

    // Reset state when leaving the screen so the next booking gets a fresh chat
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "🧹 CustomerChatScreen disposed — resetting chat states")
            viewModel.resetChatStates()
        }
    }

    val isChatReady = currentChat != null

    Log.d(TAG, "🔄 RECOMPOSE — currentChat=${currentChat?.chatId} isChatReady=$isChatReady createChatState=${createChatState::class.simpleName}")

    Box(modifier = Modifier.fillMaxSize().background(ChatColors.DeepBlack)) {
        ChatLiquidBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // ========== TOP BAR ==========
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ChatColors.TextWhite
                        )
                    }

                    // Shop Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ChatColors.AmberOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentChat?.shopProfileImage?.isNotBlank() == true) {
                            Base64Image(
                                imageSource = currentChat!!.shopProfileImage,
                                contentDescription = currentChat!!.shopName,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = currentChat?.shopName?.take(1)?.uppercase() ?: "S",
                                color = ChatColors.AmberOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Shop Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentChat?.shopName ?: "Shop",
                            color = ChatColors.TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isChatReady) "Online" else "Connecting...",
                            color = ChatColors.TextGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = ChatColors.OutlineWhite, thickness = 1.dp)

            // ========== MESSAGES AREA ==========
            Box(modifier = Modifier.weight(1f)) {
                // If the chat is still being created (async from startChatFromShop),
                // show loading/error based on createChatState
                if (!isChatReady) {
                    when (val state = createChatState) {
                        is Resource.Loading -> {
                            Log.d(TAG, "⏳ Chat is being created...")
                            ChatInitializingView()
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "❌ Chat creation failed: ${state.message}")
                            ChatInitErrorView(
                                message = state.message ?: "Failed to initialize chat",
                                onBack = { navController.popBackStack() }
                            )
                        }
                        else -> {
                            // Idle or unknown — chat might be loading from setCurrentChat path
                            Log.d(TAG, "⏳ Waiting for chat to become ready (state=$state)")
                            ChatInitializingView()
                        }
                    }
                } else {
                    // Chat is ready — show messages
                    when (val state = messagesState) {
                        is Resource.Loading -> {
                            Log.d(TAG, "⏳ Messages loading...")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ChatColors.AmberOrange)
                            }
                        }
                        is Resource.Success -> {
                            if (state.data.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("💬", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Start Conversation",
                                        color = ChatColors.TextWhite,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Ask about services, pricing, or availability",
                                        color = ChatColors.TextGray,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                ChatMessagesList(
                                    messages = state.data,
                                    currentUserId = viewModel.currentUserId
                                )
                            }
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "❌ Messages error: ${state.message}")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.message ?: "Error", color = ChatColors.TextGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TextButton(onClick = {
                                        currentChat?.let { viewModel.listenToMessages(it.chatId) }
                                    }) {
                                        Text(
                                            "RETRY",
                                            color = ChatColors.AmberOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            // Idle — messages not yet being listened to
                            Log.d(TAG, "⏳ Messages idle, waiting for listener...")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ChatColors.AmberOrange)
                            }
                        }
                    }
                }
            }

            // ========== INPUT BAR ==========
            // Only enable input when chat is fully ready
            ChatInputBar(
                onSendMessage = { text ->
                    Log.d(TAG, "📤 Sending message: ${text.take(30)}...")
                    viewModel.sendMessage(text)
                },
                enabled = isChatReady
            )
        }
    }
}

// ============= CHAT INITIALIZING VIEW =============
@Composable
private fun ChatInitializingView() {
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
                "Setting up chat...",
                color = ChatColors.TextGray,
                fontSize = 14.sp
            )
        }
    }
}

// ============= CHAT INIT ERROR VIEW =============
@Composable
private fun ChatInitErrorView(message: String, onBack: () -> Unit) {
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
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChatColors.AmberOrange
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GO BACK", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}