package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChatRepository"

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private val chatsCollection = firestore.collection("chats")

    // ─────────────────────────────────────────────────────────────────────────
    // Deterministic Chat ID
    // ─────────────────────────────────────────────────────────────────────────
    private fun buildChatId(userId: String, shopId: String, bookingId: String): String =
        if (bookingId.isNotBlank()) "${userId}_${shopId}_${bookingId}"
        else "${userId}_${shopId}"

    // ─────────────────────────────────────────────────────────────────────────
    // getOrCreateChat
    // ─────────────────────────────────────────────────────────────────────────
    override fun getOrCreateChat(
        userId: String,
        shopId: String,
        userName: String,
        shopName: String,
        userProfileImage: String,
        shopProfileImage: String,
        bookingId: String,
        serviceName: String
    ): Flow<Resource<Chat>> = flow {
        emit(Resource.Loading)
        try {
            val chatId = buildChatId(userId, shopId, bookingId)
            Log.d(TAG, "🔍 getOrCreateChat — chatId=$chatId serviceName=$serviceName")

            val docRef = chatsCollection.document(chatId)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val chat = snapshot.toObject(Chat::class.java)
                if (chat != null) {
                    val withId = chat.copy(chatId = snapshot.id)
                    Log.d(TAG, "✅ Existing chat — chatId=${snapshot.id}")
                    emit(Resource.Success(withId))
                    return@flow
                }
            }

            val newChat = Chat(
                chatId           = chatId,
                userId           = userId,
                shopId           = shopId,
                bookingId        = bookingId,
                serviceName      = serviceName,
                userName         = userName,
                shopName         = shopName,
                userProfileImage = userProfileImage,
                shopProfileImage = shopProfileImage,
                createdAt        = Timestamp.now(),
                updatedAt        = Timestamp.now()
            )
            docRef.set(newChat).await()
            Log.d(TAG, "✅ New chat created — chatId=$chatId serviceName=$serviceName")
            emit(Resource.Success(newChat))
        } catch (e: Exception) {
            Log.e(TAG, "❌ getOrCreateChat error: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Failed to create chat"))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // sendMessage — increments the OPPOSITE side's unread counter
    //   Customer sends → shopUnreadCount++
    //   Shop owner sends → customerUnreadCount++
    // Also resets soft-delete flag for the receiving side so the chat
    // reappears if they had previously deleted it.
    // ─────────────────────────────────────────────────────────────────────────
    override fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>> = flow {
        emit(Resource.Loading)
        try {
            val messageId = chatsCollection
                .document(chatId).collection("messages").document().id

            val messageWithId = message.copy(
                messageId = messageId,
                timeSent  = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            chatsCollection.document(chatId)
                .collection("messages").document(messageId)
                .set(messageWithId).await()

            // Determine which unread counter to increment and which delete flag to reset
            val isCustomerSending = message.senderType == "customer"
            val unreadField  = if (isCustomerSending) "shopUnreadCount"     else "customerUnreadCount"
            val resetDelFlag = if (isCustomerSending) "deletedForShop"      else "deletedForCustomer"

            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage"         to messageWithId.text.take(100),
                    "lastMessageTime"     to messageWithId.timeSent,
                    "lastMessageSenderId" to messageWithId.senderId,
                    unreadField           to FieldValue.increment(1),
                    resetDelFlag          to false,  // reappear for the other side
                    "updatedAt"           to Timestamp.now()
                )
            ).await()

            emit(Resource.Success(messageWithId))
            Log.d(TAG, "✅ Message sent: $messageId ($unreadField incremented)")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send message"))
            Log.e(TAG, "❌ sendMessage error", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listenToMessages — ordered by timeSent, NO senderType filter
    // ─────────────────────────────────────────────────────────────────────────
    override fun listenToMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection.document(chatId)
            .collection("messages")
            .orderBy("timeSent", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ listenToMessages error: ${error.message}")
                    trySend(Resource.Error(error.message ?: "Failed to listen messages"))
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.let {
                        if (it.messageId.isBlank()) it.copy(messageId = doc.id) else it
                    }
                } ?: emptyList()
                Log.d(TAG, "📩 ${messages.size} messages in chatId=$chatId")
                trySend(Resource.Success(messages))
            }

        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listenToUserChats — excludes chats deleted for customer
    // ─────────────────────────────────────────────────────────────────────────
    override fun listenToUserChats(userId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("deletedForCustomer", false)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ listenToUserChats error: ${error.message}")
                    trySend(Resource.Error(error.message ?: "Failed to listen chats"))
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.copy(chatId = doc.id)
                } ?: emptyList()
                Log.d(TAG, "📋 ${chats.size} chats for userId=$userId")
                trySend(Resource.Success(chats))
            }

        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listenToShopChats — excludes chats deleted for shop, deduped by chatId
    // ─────────────────────────────────────────────────────────────────────────
    override fun listenToShopChats(shopId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)
        Log.d(TAG, "🔍 Starting shop chat listener for shopId=$shopId")

        val listener = chatsCollection
            .whereEqualTo("shopId", shopId)
            .whereEqualTo("deletedForShop", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ listenToShopChats error: ${error.message}", error)
                    trySend(Resource.Error(error.message ?: "Failed to listen shop chats"))
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.copy(chatId = doc.id)
                } ?: emptyList()

                val dedupedChats = chats.distinctBy { it.chatId }
                    .sortedByDescending { it.updatedAt }

                Log.d(TAG, "✅ ${dedupedChats.size} unique chats for shopId=$shopId")
                trySend(Resource.Success(dedupedChats))
            }

        awaitClose {
            Log.d(TAG, "🛑 Stopped shop chat listener for shopId=$shopId")
            listener.remove()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // markMessagesAsRead — resets the caller's per-role unread counter
    //   readerRole = "customer"   → reset customerUnreadCount
    //   readerRole = "shop_owner" → reset shopUnreadCount
    // ─────────────────────────────────────────────────────────────────────────
    override fun markMessagesAsRead(
        chatId: String,
        readerId: String,
        readerRole: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val unreadMessages = chatsCollection.document(chatId)
                .collection("messages")
                .whereEqualTo("read", false)
                .whereNotEqualTo("senderId", readerId)
                .get().await()

            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, mapOf("read" to true, "updatedAt" to Timestamp.now()))
            }
            if (unreadMessages.documents.isNotEmpty()) batch.commit().await()

            // Reset the correct per-role counter
            val unreadField = if (readerRole == "shop_owner") "shopUnreadCount" else "customerUnreadCount"
            chatsCollection.document(chatId).update(unreadField, 0).await()

            emit(Resource.Success(true))
            Log.d(TAG, "✅ Marked ${unreadMessages.size()} messages read in chatId=$chatId ($unreadField reset)")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark messages as read"))
            Log.e(TAG, "❌ markMessagesAsRead error", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUnreadCount (one-shot, customer)
    // ─────────────────────────────────────────────────────────────────────────
    override fun getUnreadCount(userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val docs = chatsCollection.whereEqualTo("userId", userId).get().await()
            val total = docs.documents.sumOf { (it.getLong("customerUnreadCount") ?: 0).toInt() }
            emit(Resource.Success(total))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get unread count"))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getShopUnreadCount (one-shot, shop owner)
    // ─────────────────────────────────────────────────────────────────────────
    override fun getShopUnreadCount(shopId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val docs = chatsCollection.whereEqualTo("shopId", shopId).get().await()
            val total = docs.documents.sumOf { (it.getLong("shopUnreadCount") ?: 0).toInt() }
            emit(Resource.Success(total))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop unread count"))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listenToShopTotalUnread — REAL-TIME sum of shopUnreadCount for the badge
    // Only counts chats where deletedForShop == false
    // ─────────────────────────────────────────────────────────────────────────
    override fun listenToShopTotalUnread(shopId: String): Flow<Resource<Int>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection
            .whereEqualTo("shopId", shopId)
            .whereEqualTo("deletedForShop", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ listenToShopTotalUnread error: ${error.message}")
                    trySend(Resource.Error(error.message ?: "Failed to listen total unread"))
                    return@addSnapshotListener
                }
                val total = snapshot?.documents?.sumOf {
                    (it.getLong("shopUnreadCount") ?: 0).toInt()
                } ?: 0
                Log.d(TAG, "🔔 Total shopUnread for $shopId = $total")
                trySend(Resource.Success(total))
            }

        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Soft-delete helpers
    // ─────────────────────────────────────────────────────────────────────────

    override fun deleteChatForShop(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            chatsCollection.document(chatId).update("deletedForShop", true).await()
            emit(Resource.Success(true))
            Log.d(TAG, "🗑️ Chat soft-deleted for shop: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete chat"))
            Log.e(TAG, "❌ deleteChatForShop error", e)
        }
    }

    override fun deleteChatForCustomer(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            chatsCollection.document(chatId).update("deletedForCustomer", true).await()
            emit(Resource.Success(true))
            Log.d(TAG, "🗑️ Chat soft-deleted for customer: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete chat"))
            Log.e(TAG, "❌ deleteChatForCustomer error", e)
        }
    }

    override fun undoDeleteChatForShop(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            chatsCollection.document(chatId).update("deletedForShop", false).await()
            emit(Resource.Success(true))
            Log.d(TAG, "↩️ Undo delete for shop: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to undo delete"))
        }
    }

    override fun undoDeleteChatForCustomer(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            chatsCollection.document(chatId).update("deletedForCustomer", false).await()
            emit(Resource.Success(true))
            Log.d(TAG, "↩️ Undo delete for customer: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to undo delete"))
        }
    }
}