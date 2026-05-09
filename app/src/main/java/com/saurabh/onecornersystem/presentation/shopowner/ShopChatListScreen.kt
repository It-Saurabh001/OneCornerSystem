package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.presentation.common.ChatViewModel
import com.saurabh.onecornersystem.presentation.components.*
import com.saurabh.onecornersystem.presentation.navigation.Screen
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopChatListScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    shopId: String
) {
    val chatsState  by viewModel.chatsState.collectAsStateWithLifecycle()
    val deletedChat by viewModel.deletedChat.collectAsStateWithLifecycle()

    // ── Local state ───────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }

    // ── Snackbar with Undo (triggered whenever a chat is soft-deleted) ────────
    LaunchedEffect(deletedChat) {
        val deleted = deletedChat ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message     = "Conversation deleted",
            actionLabel = "Undo",
            duration    = SnackbarDuration.Short   // ~5 seconds
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDeleteForShop()
        }
    }

    SideEffect {
        Log.d("ShopChatListScreen", "🔄 Recompose | state=${chatsState::class.simpleName} shopId='$shopId'")
    }

    // ── Start real-time listener once ─────────────────────────────────────────
    LaunchedEffect(Unit) {
        Log.d("ShopChatListScreen", "🚀 LaunchedEffect — starting listener for shopId='$shopId'")
        viewModel.loadShopChats(shopId)
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    chatToDelete?.let { chat ->
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            containerColor   = Color(0xFF1A1A1A),
            title = {
                Text(
                    "Delete Conversation?",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val label = if (chat.serviceName.isNotBlank())
                    "${chat.userName} – ${chat.serviceName}"
                else
                    chat.userName
                Text(
                    "Delete your copy of \"$label\"?\nThe customer's copy is not affected.",
                    color    = Color(0xFF9E9E9E),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChatForShop(chat)
                    chatToDelete = null
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) {
                    Text("Cancel", color = ChatColors.AmberOrange)
                }
            }
        )
    }

    // ── Root scaffold (provides snackbar host) ────────────────────────────────
    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatColors.DeepBlack)
                .padding(innerPadding)
        ) {
            ChatLiquidBackground()

            Column(modifier = Modifier.fillMaxSize()) {

                // ── TOP BAR ───────────────────────────────────────────────────
                Surface(
                    modifier       = Modifier.fillMaxWidth(),
                    color          = ChatColors.DeepBlack.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ChatColors.TextWhite
                            )
                        }
                        Text(
                            text       = "Customer Messages",
                            color      = ChatColors.TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize   = 22.sp,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = ChatColors.OutlineWhite, thickness = 1.dp)

                // ── CHAT LIST ─────────────────────────────────────────────────
                when (val state = chatsState) {

                    is Resource.Loading -> {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ChatColors.AmberOrange)
                        }
                    }

                    is Resource.Success -> {
                        Log.d("ShopChatListScreen", "✅ Render: ${state.data.size} chats")
                        if (state.data.isEmpty()) {
                            EmptyShopChatsView()
                        } else {
                            LazyColumn(
                                modifier            = Modifier.fillMaxSize(),
                                contentPadding      = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.data, key = { it.chatId }) { chat ->
                                    ChatListItem(
                                        chat        = chat,
                                        isShopOwner = true,
                                        onClick = {
                                            Log.d("ShopChatListScreen", "👆 Clicked chatId=${chat.chatId}")
                                            viewModel.setCurrentChat(chat)
                                            navController.navigate(
                                                Screen.ShopChat.passArgs(
                                                    shopId       = chat.shopId,
                                                    shopName     = chat.shopName,
                                                    customerId   = chat.userId,
                                                    customerName = chat.userName,
                                                    bookingId    = chat.bookingId
                                                )
                                            )
                                        },
                                        onLongClick = {
                                            Log.d("ShopChatListScreen", "🗑️ Long-pressed chatId=${chat.chatId}")
                                            chatToDelete = chat
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        Log.e("ShopChatListScreen", "❌ Error: ${state.message}")
                        Column(
                            modifier            = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "❌ Failed to load chats",
                                color      = ChatColors.TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                state.message ?: "Unknown error",
                                color     = ChatColors.TextGray,
                                fontSize  = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    Log.d("ShopChatListScreen", "🔁 Retry for shopId=$shopId")
                                    viewModel.loadShopChats(shopId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ChatColors.AmberOrange)
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    else -> {
                        // Resource.Idle — show spinner with delayed retry
                        Log.d("ShopChatListScreen", "💤 Idle | shopId='$shopId'")
                        var showRetry by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(5_000)
                            showRetry = true
                        }
                        Column(
                            modifier            = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = ChatColors.AmberOrange)
                            if (showRetry) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "Taking longer than expected...",
                                    color    = ChatColors.TextGray,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        Log.d("ShopChatListScreen", "🔁 Retry from Idle for shopId=$shopId")
                                        viewModel.loadShopChats(shopId)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChatColors.AmberOrange)
                                ) {
                                    Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyShopChatsView() {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Chat,
            contentDescription = null,
            tint     = ChatColors.TextGray.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "No Customer Messages",
            color      = ChatColors.TextWhite,
            fontWeight = FontWeight.Black,
            fontSize   = 20.sp
        )
        Text(
            "Customer messages will appear here",
            color     = ChatColors.TextGray,
            textAlign = TextAlign.Center,
            fontSize  = 14.sp,
            modifier  = Modifier.padding(top = 8.dp)
        )
    }
}