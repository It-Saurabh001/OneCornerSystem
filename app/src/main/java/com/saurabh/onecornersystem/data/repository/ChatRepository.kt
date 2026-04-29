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
        shopProfileImage: String
    ): Flow<Resource<Chat>>

    // Send message
    fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>>

    // Get chat messages (real-time)
    fun listenToMessages(chatId: String): Flow<Resource<List<Message>>>

    // Get all chats for a user
    fun listenToUserChats(userId: String): Flow<Resource<List<Chat>>>

    // Get all chats for a shop
    fun listenToShopChats(shopId: String): Flow<Resource<List<Chat>>>

    // Mark messages as read
    fun markMessagesAsRead(chatId: String, readerId: String): Flow<Resource<Boolean>>

    // Get unread count for a user
    fun getUnreadCount(userId: String): Flow<Resource<Int>>

    // Get unread count for a shop
    fun getShopUnreadCount(shopId: String): Flow<Resource<Int>>
}