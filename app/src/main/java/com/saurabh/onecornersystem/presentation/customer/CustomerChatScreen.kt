package com.saurabh.onecornersystem.presentation.customer

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val currentChat by viewModel.currentChat.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(ChatColors.DeepBlack)) {
        ChatLiquidBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
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
                            text = "Online",
                            color = ChatColors.TextGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = ChatColors.OutlineWhite, thickness = 1.dp)

            // Messages
            Box(modifier = Modifier.weight(1f)) {
                when (val state = messagesState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message ?: "Error", color = ChatColors.TextGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = {
                                    currentChat?.let { viewModel.listenToMessages(it.chatId) }
                                }) {
                                    Text("RETRY", color = ChatColors.AmberOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Input Bar
            ChatInputBar(
                onSendMessage = { text -> viewModel.sendMessage(text) }
            )
        }
    }
}