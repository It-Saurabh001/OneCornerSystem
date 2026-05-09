package com.saurabh.onecornersystem.presentation.customer

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun CustomerChatListScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatsState  by viewModel.chatsState.collectAsStateWithLifecycle()
    val deletedChat by viewModel.deletedChat.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }

    // ── Snackbar with Undo ────────────────────────────────────────────────────
    LaunchedEffect(deletedChat) {
        val deleted = deletedChat ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message     = "Conversation deleted",
            actionLabel = "Undo",
            duration    = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDeleteForCustomer()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadUserChats() }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    chatToDelete?.let { chat ->
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            containerColor   = Color(0xFF1A1A1A),
            title = {
                Text("Delete Conversation?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                val label = if (chat.serviceName.isNotBlank())
                    "${chat.shopName} – ${chat.serviceName}"
                else
                    chat.shopName
                Text(
                    "Delete your copy of \"$label\"?\nThe shop's copy is not affected.",
                    color    = Color(0xFF9E9E9E),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChatForCustomer(chat)
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
                            text       = "Messages",
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                    tint     = ChatColors.TextGray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "No Messages Yet",
                                    color      = ChatColors.TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize   = 20.sp
                                )
                                Text(
                                    "Chat with shops to discuss your service needs",
                                    color     = ChatColors.TextGray,
                                    textAlign = TextAlign.Center,
                                    fontSize  = 14.sp,
                                    modifier  = Modifier.padding(top = 8.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier            = Modifier.fillMaxSize(),
                                contentPadding      = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.data, key = { it.chatId }) { chat ->
                                    ChatListItem(
                                        chat        = chat,
                                        isShopOwner = false,
                                        onClick = {
                                            viewModel.setCurrentChat(chat)
                                            navController.navigate(
                                                Screen.CustomerChat.navigate(
                                                    shopId    = chat.shopId,
                                                    shopName  = chat.shopName,
                                                    shopImage = chat.shopProfileImage,
                                                    bookingId = chat.bookingId
                                                )
                                            )
                                        },
                                        onLongClick = { chatToDelete = chat }
                                    )
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message ?: "Error loading chats", color = ChatColors.TextGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.loadUserChats() },
                                    colors  = ButtonDefaults.buttonColors(containerColor = ChatColors.AmberOrange)
                                ) {
                                    Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}