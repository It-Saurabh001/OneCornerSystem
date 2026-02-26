package com.saurabh.onecornersystem.presentation.state

import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.data.model.StockAlert

/**
 * Sealed class representing different states for inventory operations
 */
sealed class InventoryState {
    object Idle : InventoryState()
    object Loading : InventoryState()
    data class Success(val alerts: List<StockAlert>) : InventoryState()
    data class Error(val message: String) : InventoryState()
}

/**
 * Inventory UI state holding all inventory-related state flows
 */
data class InventoryUiState(
    val inventoryState: InventoryState = InventoryState.Idle,
    val isUpdating: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val stockAlerts: List<StockAlert> = emptyList()
)

/**
 * Sealed class representing different states for chat operations
 */
sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Success(val messages: List<Chat>) : ChatState()
    data class Error(val message: String) : ChatState()
}

/**
 * Chat UI state holding all chat-related state flows
 */
data class ChatUiState(
    val chatState: ChatState = ChatState.Idle,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<Chat> = emptyList(),
    val currentChatId: String? = null
)

