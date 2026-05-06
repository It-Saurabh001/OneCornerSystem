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

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private val chatsCollection = firestore.collection("chats")

    override fun getOrCreateChat(
        userId: String,
        shopId: String,
        userName: String,
        shopName: String,
        userProfileImage: String,
        shopProfileImage: String,
        bookingId: String
    ): Flow<Resource<Chat>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("ChatRepository", "🔍 getOrCreateChat — userId=$userId, shopId=$shopId, bookingId=$bookingId")

            // Build query — include bookingId so each booking gets its own chat
            var query = chatsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("shopId", shopId)

            if (bookingId.isNotBlank()) {
                query = query.whereEqualTo("bookingId", bookingId)
            }

            val existingChats = query.get().await()

            if (!existingChats.isEmpty) {
                val doc = existingChats.documents[0]
                val chat = doc.toObject(Chat::class.java)
                if (chat != null) {
                    Log.d("ChatRepository", "✅ Existing chat found — chatId=${doc.id}")
                    emit(Resource.Success(chat.copy(chatId = doc.id)))
                    return@flow
                }
            }

            // Create new chat
            val chatId = chatsCollection.document().id
            val chat = Chat(
                chatId = chatId,
                userId = userId,
                shopId = shopId,
                bookingId = bookingId,
                userName = userName,
                shopName = shopName,
                userProfileImage = userProfileImage,
                shopProfileImage = shopProfileImage,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            chatsCollection.document(chatId).set(chat).await()

            Log.d("ChatRepository", "✅ New chat created — chatId=$chatId, bookingId=$bookingId")
            emit(Resource.Success(chat))
        } catch (e: Exception) {
            Log.e("ChatRepository", "❌ getOrCreateChat error: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Failed to create chat"))
        }
    }

    override fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>> = flow {
        emit(Resource.Loading)
        try {
            val messageId = chatsCollection
                .document(chatId)
                .collection("messages")
                .document()
                .id

            val messageWithId = message.copy(
                messageId = messageId,
                timeSent = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            // Save message to subcollection
            chatsCollection
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageWithId)
                .await()

            // Update chat document
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to messageWithId.text.take(100),
                    "lastMessageTime" to messageWithId.timeSent,
                    "lastMessageSenderId" to messageWithId.senderId,
                    "unreadCount" to FieldValue.increment(1),
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            emit(Resource.Success(messageWithId))
            Log.d("ChatRepository", "Message sent: $messageId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send message"))
            Log.e("ChatRepository", "Send message error", e)
        }
    }

    override fun listenToMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection
            .document(chatId)
            .collection("messages")
            .orderBy("timeSent", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen messages"))
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(Resource.Success(messages))
                Log.d("ChatRepository", "${messages.size} messages in chat $chatId")
            }

        awaitClose { listener.remove() }
    }

    override fun listenToUserChats(userId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection
            .whereEqualTo("userId", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen chats"))
                    return@addSnapshotListener
                }

                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                trySend(Resource.Success(chats))
                Log.d("ChatRepository", " ${chats.size} chats for user $userId")
            }

        awaitClose { listener.remove() }
    }

    override fun listenToShopChats(shopId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)
        Log.d("ChatRepository", "🔍 Starting to listen to chats for shopId: $shopId")

        val listener = chatsCollection
            .whereEqualTo("shopId", shopId)
            // Removed .orderBy("updatedAt", Query.Direction.DESCENDING) to avoid requiring a composite index in Firestore
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "❌ Failed to listen shop chats: ${error.message}", error)
                    trySend(Resource.Error(error.message ?: "Failed to listen shop chats"))
                    return@addSnapshotListener
                }

                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                // Sort locally to avoid index requirements
                val sortedChats = chats.sortedByDescending { it.updatedAt }
                
                Log.d("ChatRepository", "✅ Fetched ${sortedChats.size} chats for shop $shopId")
                sortedChats.forEach { chat ->
                    Log.d("ChatRepository", "Chat info: ID=${chat.chatId}, Customer=${chat.userName}, LastMsg=${chat.lastMessage}")
                }
                
                trySend(Resource.Success(sortedChats))
            }

        awaitClose { 
            Log.d("ChatRepository", "🛑 Stopping chat listener for shopId: $shopId")
            listener.remove() 
        }
    }

    override fun markMessagesAsRead(chatId: String, readerId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            // Get all unread messages not sent by reader
            val unreadMessages = chatsCollection
                .document(chatId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .whereNotEqualTo("senderId", readerId)
                .get()
                .await()

            // Batch update for better performance
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "isRead" to true,
                    "updatedAt" to Timestamp.now()
                ))
            }

            if (unreadMessages.documents.isNotEmpty()) {
                batch.commit().await()
            }

            // Reset unread count on chat document
            chatsCollection.document(chatId).update("unreadCount", 0).await()

            emit(Resource.Success(true))
            Log.d("ChatRepository", "Marked ${unreadMessages.size()} messages as read in chat $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark messages as read"))
            Log.e("ChatRepository", " Mark read error", e)
        }
    }

    override fun getUnreadCount(userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val userChats = chatsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val totalUnread = userChats.documents.sumOf { doc ->
                (doc.getLong("unreadCount") ?: 0).toInt()
            }

            emit(Resource.Success(totalUnread))
            Log.d("ChatRepository", "📊 Total unread for user $userId: $totalUnread")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get unread count"))
            Log.e("ChatRepository", "Unread count error", e)
        }
    }

    override fun getShopUnreadCount(shopId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val shopChats = chatsCollection
                .whereEqualTo("shopId", shopId)
                .get()
                .await()

            val totalUnread = shopChats.documents.sumOf { doc ->
                (doc.getLong("unreadCount") ?: 0).toInt()
            }

            emit(Resource.Success(totalUnread))
            Log.d("ChatRepository", "📊 Total unread for shop $shopId: $totalUnread")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop unread count"))
            Log.e("ChatRepository", "Shop unread count error", e)
        }
    }
}