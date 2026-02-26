package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun getShopChats(shopId: String): Flow<Resource<List<Chat>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("chats")
                .whereEqualTo("shopId", shopId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val chats = querySnapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
            emit(Resource.Success(chats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop chats"))
            Log.e("ChatRepository", "Get shop chats error", e)
        }
    }

    override fun getChatDetails(chatId: String): Flow<Resource<Chat>> = flow {
        emit(Resource.Loading)
        try {
            val chatDoc = firestore.collection("chats").document(chatId).get().await()
            val chat = chatDoc.toObject(Chat::class.java)
            if (chat != null) {
                emit(Resource.Success(chat))
            } else {
                emit(Resource.Error("Chat not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get chat details"))
            Log.e("ChatRepository", "Get chat details error", e)
        }
    }

    override fun getChatMessages(chatId: String, limit: Int): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timeSent", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val messages = querySnapshot.documents.mapNotNull { it.toObject(Message::class.java) }.reversed()
            emit(Resource.Success(messages))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get messages"))
            Log.e("ChatRepository", "Get messages error", e)
        }
    }

    override fun getChatMessagesPaginated(
        chatId: String,
        pageSize: Int,
        lastMessage: Any?
    ): Flow<Resource<Pair<List<Message>, Any?>>> = flow {
        emit(Resource.Loading)
        try {
            var query: Query = firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timeSent", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            if (lastMessage != null) {
                query = query.startAfter(lastMessage as com.google.firebase.firestore.DocumentSnapshot)
            }

            val querySnapshot = query.get().await()
            val messages = querySnapshot.documents.mapNotNull { it.toObject(Message::class.java) }.reversed()

            val nextLastDoc = if (messages.size == pageSize) {
                querySnapshot.documents.firstOrNull()
            } else {
                null
            }

            emit(Resource.Success(Pair(messages, nextLastDoc)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get messages paginated"))
            Log.e("ChatRepository", "Get messages paginated error", e)
        }
    }

    override fun sendMessage(chatId: String, message: Message): Flow<Resource<Message>> = flow {
        emit(Resource.Loading)
        try {
            val messageWithId = message.copy(
                messageId = firestore.collection("messages").document().id
            )

            firestore.collection("chats").document(chatId)
                .collection("messages")
                .document(messageWithId.messageId)
                .set(messageWithId)
                .await()

            firestore.collection("chats").document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to messageWithId.text,
                        "lastMessageTime" to messageWithId.timeSent,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(messageWithId))
            Log.d("ChatRepository", "Message sent: ${messageWithId.messageId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send message"))
            Log.e("ChatRepository", "Send message error", e)
        }
    }

    override fun markMessageAsRead(chatId: String, messageId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("chats").document(chatId)
                .collection("messages")
                .document(messageId)
                .update("isRead", true)
                .await()

            emit(Resource.Success(true))
            Log.d("ChatRepository", "Message marked as read: $messageId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark message as read"))
            Log.e("ChatRepository", "Mark message as read error", e)
        }
    }

    override fun markAllMessagesAsRead(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("chats").document(chatId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in querySnapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            firestore.collection("chats").document(chatId).update("unreadCount", 0).await()

            emit(Resource.Success(true))
            Log.d("ChatRepository", "All messages marked as read: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark all as read"))
            Log.e("ChatRepository", "Mark all as read error", e)
        }
    }

    override fun getUnreadMessageCount(chatId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("chats").document(chatId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            emit(Resource.Success(querySnapshot.size()))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get unread count"))
            Log.e("ChatRepository", "Get unread count error", e)
        }
    }

    override fun listenToChatMessages(chatId: String, limit: Int): Flow<List<Message>> = flow {
        try {
            firestore.collection("chats").document(chatId).collection("messages")
                .orderBy("timeSent", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "Listen to messages error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }.reversed()
                    }
                }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Listen to messages error", e)
        }
    }

    override fun listenToShopChats(shopId: String): Flow<List<Chat>> = flow {
        try {
            firestore.collection("chats")
                .whereEqualTo("shopId", shopId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "Listen to chats error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val chats = snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
                    }
                }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Listen to chats error", e)
        }
    }

    override fun listenToChat(chatId: String): Flow<Chat?> = flow {
        try {
            firestore.collection("chats").document(chatId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "Listen to chat error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val chat = snapshot.toObject(Chat::class.java)
                    }
                }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Listen to chat error", e)
        }
    }

    override fun deleteChat(chatId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("chats").document(chatId).update("isActive", false).await()
            emit(Resource.Success(true))
            Log.d("ChatRepository", "Chat deleted: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete chat"))
            Log.e("ChatRepository", "Delete chat error", e)
        }
    }

    override fun blockCustomer(chatId: String, customerId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("chats").document(chatId)
                .update(mapOf("isActive" to false, "blockedAt" to com.google.firebase.Timestamp.now()))
                .await()
            emit(Resource.Success(true))
            Log.d("ChatRepository", "Customer blocked: $customerId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to block customer"))
            Log.e("ChatRepository", "Block customer error", e)
        }
    }

    override fun unblockCustomer(chatId: String, customerId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("chats").document(chatId).update("isActive", true).await()
            emit(Resource.Success(true))
            Log.d("ChatRepository", "Customer unblocked: $customerId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to unblock customer"))
            Log.e("ChatRepository", "Unblock customer error", e)
        }
    }

    override fun searchMessages(chatId: String, query: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading)
        try {
            val allMessages = firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timeSent", Query.Direction.DESCENDING)
                .get()
                .await()

            val messages = allMessages.documents
                .mapNotNull { it.toObject(Message::class.java) }
                .filter { it.text.contains(query, ignoreCase = true) }

            emit(Resource.Success(messages))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to search messages"))
            Log.e("ChatRepository", "Search messages error", e)
        }
    }

    override fun getChatByCustomer(shopId: String, customerId: String): Flow<Resource<Chat?>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("chats")
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("userId", customerId)
                .get()
                .await()

            val chat = if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].toObject(Chat::class.java)
            } else {
                null
            }

            emit(Resource.Success(chat))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get chat by customer"))
            Log.e("ChatRepository", "Get chat by customer error", e)
        }
    }

    override fun createOrGetChat(
        shopId: String,
        customerId: String,
        userName: String,
        shopName: String
    ): Flow<Resource<Chat>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("chats")
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("userId", customerId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val existingChat = querySnapshot.documents[0].toObject(Chat::class.java)
                if (existingChat != null) {
                    emit(Resource.Success(existingChat))
                    return@flow
                }
            }

            val chatId = firestore.collection("chats").document().id
            val newChat = Chat(
                chatId = chatId,
                userId = customerId,
                shopId = shopId,
                userName = userName,
                shopName = shopName,
                createdAt = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )

            firestore.collection("chats").document(chatId).set(newChat).await()

            emit(Resource.Success(newChat))
            Log.d("ChatRepository", "Chat created: $chatId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create or get chat"))
            Log.e("ChatRepository", "Create or get chat error", e)
        }
    }
}

