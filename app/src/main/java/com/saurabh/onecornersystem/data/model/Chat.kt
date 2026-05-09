package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Chat(
    val chatId: String = "",
    val userId: String = "",          // Customer ID
    val shopId: String = "",          // Shop ID
    val bookingId: String = "",       // Booking ID — each booking gets its own chat
    val serviceName: String = "",     // Name of the service/booking (e.g. "Plumbing Service")
    val userName: String = "",
    val shopName: String = "",
    val userProfileImage: String = "",
    val shopProfileImage: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val lastMessageSenderId: String = "",

    // ── Per-role unread counters ──────────────────────────────────────────────
    // shopUnreadCount  : incremented when CUSTOMER sends a message
    // customerUnreadCount : incremented when SHOP OWNER sends a message
    val shopUnreadCount: Int = 0,
    val customerUnreadCount: Int = 0,

    // Legacy field — kept for backward-compatibility with existing documents.
    // New code should use shopUnreadCount / customerUnreadCount instead.
    val unreadCount: Int = 0,

    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var active: Boolean = true,

    // ── Soft-delete flags (independent per side) ─────────────────────────────
    // Deleting on one side does NOT affect the other side.
    // When the other person sends a new message the flag is reset to false.
    @get:PropertyName("deletedForShop") @set:PropertyName("deletedForShop")
    var deletedForShop: Boolean = false,

    @get:PropertyName("deletedForCustomer") @set:PropertyName("deletedForCustomer")
    var deletedForCustomer: Boolean = false,

    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",        // userId or shopId
    val senderType: String = "customer", // "customer" or "shop_owner"
    val senderName: String = "",
    val text: String = "",
    val attachmentUrl: String = "",
    val attachmentType: String = "",  // image, document, etc.
    @get:PropertyName("read")
    @set:PropertyName("read")
    var isRead: Boolean = false,
    val timeSent: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class ChatParticipant(
    val participantId: String = "",
    val participantType: String = "", // "customer" or "shop_owner"
    val name: String = "",
    val profileImage: String = ""
)
