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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.presentation.common.ChatViewModel
import com.saurabh.onecornersystem.presentation.components.*
import com.saurabh.onecornersystem.presentation.navigation.Screen
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopChatListScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    shopId: String  // NavGraph guarantees this is always non-blank before rendering
) {
    val chatsState by viewModel.chatsState.collectAsStateWithLifecycle()

    // Safe recompose log — SideEffect does NOT write to Compose state,
    // so it cannot trigger recompositions (unlike mutableIntStateOf.intValue++).
    SideEffect {
        Log.d("ShopChatListScreen", "🔄 Recompose | chatsState=${chatsState::class.simpleName} | shopId='$shopId'")
    }

    // Single one-time initialization. shopId is guaranteed non-blank by NavGraph.
    // loadShopChats is idempotent (skips if already listening to same shopId).
    LaunchedEffect(Unit) {
        Log.d("ShopChatListScreen", "🚀 LaunchedEffect(Unit) — starting chat listener for shopId='$shopId'")
        viewModel.loadShopChats(shopId)
    }

    Box(modifier = Modifier.fillMaxSize().background(ChatColors.DeepBlack)) {
        ChatLiquidBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // ========== TOP BAR ==========
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ChatColors.DeepBlack.copy(alpha = 0.9f),
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
                        text = "Customer Messages",
                        color = ChatColors.TextWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(
                color = ChatColors.OutlineWhite,
                thickness = 1.dp
            )

            // ========== CHAT LIST ==========
            when (val state = chatsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ChatColors.AmberOrange)
                    }
                }
                is Resource.Success -> {
                    Log.d("ShopChatListScreen", "✅ Render: Success with ${state.data.size} chats")
                    if (state.data.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                tint = ChatColors.TextGray.copy(alpha = 0.4f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "No Customer Messages",
                                color = ChatColors.TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Customer messages will appear here",
                                color = ChatColors.TextGray,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data, key = { it.chatId }) { chat ->
                                ChatListItem(
                                    chat = chat,
                                    isShopOwner = true,
                                    onClick = {
                                        Log.d("ShopChatListScreen", "👆 Clicked chat: ${chat.chatId} for booking: ${chat.bookingId}")
                                        viewModel.setCurrentChat(chat)
                                        navController.navigate(
                                            Screen.ShopChat.passArgs(
                                                shopId       = chat.shopId,
                                                shopName     = chat.shopName,
                                                customerId   = chat.userId,
                                                customerName = chat.userName,
                                                bookingId    = chat.bookingId
                                            )
                                        ) { launchSingleTop = true }
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e("ShopChatListScreen", "\u274c Render: Error - ${state.message}")
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "\u274c Failed to load chats",
                            color = ChatColors.TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message ?: "Unknown error",
                            color = ChatColors.TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                Log.d("ShopChatListScreen", "🔁 Retry tapped for shopId=$shopId")
                                viewModel.loadShopChats(shopId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChatColors.AmberOrange)
                        ) {
                            Text("Retry", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    // Resource.Idle — shopId resolving, show spinner
                    Log.d("ShopChatListScreen", "\uD83D\uDCA4 State is Idle | shopId='$shopId'")
                    var showRetry by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(5_000)
                        showRetry = true
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = ChatColors.AmberOrange)
                        if (showRetry) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Taking longer than expected...",
                                color = ChatColors.TextGray,
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
                                Text("Retry", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}