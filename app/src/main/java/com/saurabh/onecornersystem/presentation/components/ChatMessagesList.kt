package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saurabh.onecornersystem.data.model.Message

@Composable
fun ChatMessagesList(
    messages: List<Message>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(messages, key = { it.messageId }) { message ->
            MessageBubble(
                message = message,
                isCurrentUser = message.senderId == currentUserId
            )
        }
    }
}