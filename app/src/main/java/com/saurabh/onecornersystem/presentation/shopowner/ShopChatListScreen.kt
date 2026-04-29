package com.saurabh.onecornersystem.presentation.shopowner

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
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatsState by viewModel.chatsState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadUserChats()  // Load shop chats
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
                                        viewModel.setCurrentChat(chat)
                                        navController.navigate(Screen.ShopChat.route){
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message ?: "Error", color = ChatColors.TextGray)
                    }
                }
                else -> {}
            }
        }
    }
}