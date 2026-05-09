package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    // Create or get existing chat
    fun getOrCreateChat(
        userId: String,
        shopId: String,
        userName: String,
        shopName: String,
        userProfileImage: String,
        shopProfileImage: String,
        bookingId: String = "",
        serviceName: String = ""
    ): Flow<Resource<Chat>>

    // Send message
    fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>>

    // Get chat messages (real-time)
    fun listenToMessages(chatId: String): Flow<Resource<List<Message>>>

    // Get all chats for a user (excludes deletedForCustomer)
    fun listenToUserChats(userId: String): Flow<Resource<List<Chat>>>

    // Get all chats for a shop (excludes deletedForShop)
    fun listenToShopChats(shopId: String): Flow<Resource<List<Chat>>>

    // Mark messages as read and reset the appropriate per-role unread counter
    fun markMessagesAsRead(chatId: String, readerId: String, readerRole: String): Flow<Resource<Boolean>>

    // Get unread count for a user (one-shot, not real-time)
    fun getUnreadCount(userId: String): Flow<Resource<Int>>

    // Get unread count for a shop (one-shot, not real-time)
    fun getShopUnreadCount(shopId: String): Flow<Resource<Int>>

    // Real-time total shopUnreadCount across all non-deleted chats (for bottom nav badge)
    fun listenToShopTotalUnread(shopId: String): Flow<Resource<Int>>

    // Soft-delete a chat for the shop owner side
    fun deleteChatForShop(chatId: String): Flow<Resource<Boolean>>

    // Soft-delete a chat for the customer side
    fun deleteChatForCustomer(chatId: String): Flow<Resource<Boolean>>

    // Undo soft-delete for shop owner side
    fun undoDeleteChatForShop(chatId: String): Flow<Resource<Boolean>>

    // Undo soft-delete for customer side
    fun undoDeleteChatForCustomer(chatId: String): Flow<Resource<Boolean>>
}