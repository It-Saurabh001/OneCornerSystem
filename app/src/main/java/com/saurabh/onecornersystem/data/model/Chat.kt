package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Chat(
    val chatId: String = "",
    val userId: String = "", // Customer ID
    val shopId: String = "", // Shop ID
    val userName: String = "",
    val shopName: String = "",
    val userProfileImage: String = "",
    val shopProfileImage: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val lastMessageSenderId: String = "",
    val unreadCount: Int = 0,
    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var active: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "", // userId or shopId
    val senderType: String = "customer", // "customer" or "shop_owner"
    val senderName: String = "",
    val text: String = "",
    val attachmentUrl: String = "",
    val attachmentType: String = "", // image, document, etc.
    val isRead: Boolean = false,
    val timeSent: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class ChatParticipant(
    val participantId: String = "",
    val participantType: String = "", // "customer" or "shop_owner"
    val name: String = "",
    val profileImage: String = ""
)

