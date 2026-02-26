package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /**
     * Get all active chats for a shop
     */
    fun getShopChats(shopId: String): Flow<Resource<List<Chat>>>

    /**
     * Get specific chat details
     */
    fun getChatDetails(chatId: String): Flow<Resource<Chat>>

    /**
     * Get messages from a chat
     */
    fun getChatMessages(chatId: String, limit: Int = 50): Flow<Resource<List<Message>>>

    /**
     * Get paginated messages
     */
    fun getChatMessagesPaginated(chatId: String, pageSize: Int, lastMessage: Any? = null): Flow<Resource<Pair<List<Message>, Any?>>>

    /**
     * Send a message
     */
    fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>>

    /**
     * Mark message as read
     */
    fun markMessageAsRead(chatId: String, messageId: String): Flow<Resource<Boolean>>

    /**
     * Mark all messages as read in chat
     */
    fun markAllMessagesAsRead(chatId: String): Flow<Resource<Boolean>>

    /**
     * Get unread message count for a chat
     */
    fun getUnreadMessageCount(chatId: String): Flow<Resource<Int>>

    /**
     * Real-time listener for chat messages
     */
    fun listenToChatMessages(chatId: String, limit: Int = 50): Flow<List<Message>>

    /**
     * Real-time listener for shop chats
     */
    fun listenToShopChats(shopId: String): Flow<List<Chat>>

    /**
     * Real-time listener for specific chat
     */
    fun listenToChat(chatId: String): Flow<Chat?>

    /**
     * Delete chat (archive)
     */
    fun deleteChat(chatId: String): Flow<Resource<Boolean>>

    /**
     * Block customer
     */
    fun blockCustomer(chatId: String, customerId: String): Flow<Resource<Boolean>>

    /**
     * Unblock customer
     */
    fun unblockCustomer(chatId: String, customerId: String): Flow<Resource<Boolean>>

    /**
     * Search messages in a chat
     */
    fun searchMessages(chatId: String, query: String): Flow<Resource<List<Message>>>

    /**
     * Get chat by customer ID for shop
     */
    fun getChatByCustomer(shopId: String, customerId: String): Flow<Resource<Chat?>>

    /**
     * Create or get chat between customer and shop
     */
    fun createOrGetChat(shopId: String, customerId: String, userName: String, shopName: String): Flow<Resource<Chat>>
}

