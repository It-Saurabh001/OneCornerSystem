package com.saurabh.onecornersystem.presentation.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.local.SessionManager
import com.saurabh.onecornersystem.data.model.Chat
import com.saurabh.onecornersystem.data.model.Message
import com.saurabh.onecornersystem.data.repository.ChatRepository
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChatViewModel"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────────────────
    // Instance-level cache (NOT static — each nav destination gets its own VM)
    // ─────────────────────────────────────────────────────────────────────────
    private var cachedChat: Chat? = null
    private var cachedMessages: List<Message> = emptyList()

    // Guards so startChat* methods don't fire twice on recomposition
    private var isChatInitialized = false

    // Tracks the active message-listener job so we don't open duplicate listeners
    private var messageListenerJob: Job? = null

    // ─────────────────────────────────────────────────────────────────────────
    // Public state flows
    // ─────────────────────────────────────────────────────────────────────────

    private val _chatsState = MutableStateFlow<Resource<List<Chat>>>(Resource.Idle)
    val chatsState: StateFlow<Resource<List<Chat>>> = _chatsState.asStateFlow()

    private val _messagesState = MutableStateFlow<Resource<List<Message>>>(Resource.Idle)
    val messagesState: StateFlow<Resource<List<Message>>> = _messagesState.asStateFlow()

    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()

    private val _sendMessageState = MutableStateFlow<Resource<Message>>(Resource.Idle)
    val sendMessageState: StateFlow<Resource<Message>> = _sendMessageState.asStateFlow()

    private val _createChatState = MutableStateFlow<Resource<Chat>>(Resource.Idle)
    val createChatState: StateFlow<Resource<Chat>> = _createChatState.asStateFlow()

    private val _unreadCount = MutableStateFlow<Resource<Int>>(Resource.Idle)
    val unreadCount: StateFlow<Resource<Int>> = _unreadCount.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // User info (populated once from session — see loadCurrentUser)
    // ─────────────────────────────────────────────────────────────────────────

    var currentUserId: String = ""
        private set
    var currentUserRole: String = ""
        private set
    private var currentUserName: String = ""
    private var currentUserImage: String = ""

    // ─────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────

    init {
        Log.d(TAG, "🆕 ChatViewModel CREATED")
        loadCurrentUser()
    }

    /**
     * Reads user session ONCE using first() so we don't leave dangling collectors.
     * All start-chat functions call ensureUserLoaded() before doing real work,
     * so there is no race condition even if this coroutine hasn't finished yet.
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                currentUserId = sessionManager.userId.first() ?: ""
                currentUserRole = sessionManager.userRole.first() ?: ""
                Log.d(TAG, "👤 Session loaded — userId=$currentUserId role=$currentUserRole")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load session: ${e.message}")
            }
        }
    }

    /**
     * Suspending helper that guarantees userId/role are available before
     * any operation that needs them. Safe to call multiple times.
     */
    private suspend fun ensureUserLoaded() {
        if (currentUserId.isBlank()) {
            currentUserId = sessionManager.userId.first() ?: ""
            currentUserRole = sessionManager.userRole.first() ?: ""
            Log.d(TAG, "👤 ensureUserLoaded — userId=$currentUserId role=$currentUserRole")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User info update (called from UI with display name / avatar)
    // ─────────────────────────────────────────────────────────────────────────

    fun updateUserInfo(name: String, image: String) {
        Log.d(TAG, "👤 updateUserInfo() name=$name hasImage=${image.isNotBlank()}")
        currentUserName = name
        currentUserImage = image
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chat creation — customer side
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens (or reuses) a chat between the current user and a shop.
     * Safe to call from recompositions — subsequent calls are ignored.
     */
    fun startChatFromShop(
        shopId: String,
        shopName: String,
        shopImage: String
    ) {
        if (isChatInitialized) {
            Log.d(TAG, "⚠️ startChatFromShop — already initialized, skipping")
            return
        }
        isChatInitialized = true

        Log.d(TAG, "╔══════════════════════════════════════╗")
        Log.d(TAG, "║ startChatFromShop                   ║")
        Log.d(TAG, "║ shopId=$shopId                      ║")
        Log.d(TAG, "╚══════════════════════════════════════╝")

        viewModelScope.launch {
            ensureUserLoaded()

            if (currentUserId.isBlank()) {
                Log.e(TAG, "❌ User not logged in")
                _createChatState.value = Resource.Error("User not logged in")
                isChatInitialized = false   // allow retry
                return@launch
            }

            val displayName = currentUserName.ifBlank { "Customer" }

            _createChatState.value = Resource.Loading

            chatRepository.getOrCreateChat(
                userId = currentUserId,
                shopId = shopId,
                userName = displayName,
                shopName = shopName,
                userProfileImage = currentUserImage,
                shopProfileImage = shopImage,
                bookingId = "" // general shop chat — no specific booking
            ).collect { result ->
                handleChatResult(result)
                _createChatState.value = result
            }
        }
    }

    /**
     * Opens a chat tied to a specific booking and sends a system message once ready.
     */
    fun startChatFromBooking(
        shopId: String,
        shopName: String,
        shopImage: String,
        bookingId: String
    ) {
        if (isChatInitialized) {
            Log.d(TAG, "⚠️ startChatFromBooking — already initialized, skipping")
            return
        }
        isChatInitialized = true

        Log.d(TAG, "╔══════════════════════════════════════╗")
        Log.d(TAG, "║ startChatFromBooking                ║")
        Log.d(TAG, "║ bookingId=$bookingId                ║")
        Log.d(TAG, "╚══════════════════════════════════════╝")

        viewModelScope.launch {
            ensureUserLoaded()

            if (currentUserId.isBlank()) {
                Log.e(TAG, "❌ User not logged in")
                _createChatState.value = Resource.Error("User not logged in")
                isChatInitialized = false
                return@launch
            }

            val displayName = currentUserName.ifBlank { "Customer" }

            _createChatState.value = Resource.Loading

            chatRepository.getOrCreateChat(
                userId = currentUserId,
                shopId = shopId,
                userName = displayName,
                shopName = shopName,
                userProfileImage = currentUserImage,
                shopProfileImage = shopImage,
                bookingId = bookingId // per-booking unique chat room
            ).collect { result ->
                handleChatResult(result)
                _createChatState.value = result
            }
        }
    }

    /**
     * Shop-owner variant — initiates a chat on behalf of the shop with a customer.
     */
    fun startChatAsShopOwner(
        shopId: String,
        shopName: String,
        shopImage: String,
        customerId: String,
        customerName: String,
        customerImage: String,
        bookingId: String? = null
    ) {
        if (isChatInitialized) {
            Log.d(TAG, "⚠️ startChatAsShopOwner — already initialized, skipping")
            return
        }
        isChatInitialized = true

        Log.d(TAG, "╔══════════════════════════════════════╗")
        Log.d(TAG, "║ startChatAsShopOwner                ║")
        Log.d(TAG, "║ shop=$shopName customer=$customerName ║")
        Log.d(TAG, "║ bookingId=${bookingId ?: "NONE"}     ║")
        Log.d(TAG, "╚══════════════════════════════════════╝")

        viewModelScope.launch {
            ensureUserLoaded()

            _createChatState.value = Resource.Loading

            chatRepository.getOrCreateChat(
                userId = customerId,
                shopId = shopId,
                userName = customerName,
                shopName = shopName,
                userProfileImage = customerImage,
                shopProfileImage = shopImage,
                bookingId = bookingId ?: ""
            ).collect { result ->
                handleChatResult(result)
                _createChatState.value = result
            }
        }
    }

    // Convenience alias kept for backward-compat with call sites that use startChat()
    fun startChat(
        shopId: String,
        shopName: String,
        shopProfileImage: String,
        @Suppress("UNUSED_PARAMETER") userName: String = "",
        @Suppress("UNUSED_PARAMETER") userProfileImage: String = ""
    ) = startChatFromShop(shopId, shopName, shopProfileImage)

    // ─────────────────────────────────────────────────────────────────────────
    // Internal: handle repository result
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleChatResult(result: Resource<Chat>) {
        when (result) {
            is Resource.Success -> {
                Log.d(TAG, "✅ Chat ready — chatId=${result.data.chatId}")
                cachedChat = result.data
                _currentChat.value = result.data
                listenToMessages(result.data.chatId)
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Chat failed — ${result.message}")
                // Allow retry on next call
                isChatInitialized = false
            }
            is Resource.Loading -> Log.d(TAG, "⏳ Chat loading...")
            else -> Unit
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Messages
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts a real-time listener for messages in the given chat.
     * Cancels any previous listener first so there is never more than one active.
     */
    fun listenToMessages(chatId: String) {
        if (chatId.isBlank()) {
            Log.e(TAG, "❌ listenToMessages — chatId is blank")
            _messagesState.value = Resource.Error("Chat ID cannot be empty")
            return
        }

        // Restore from cache immediately for instant UI
        if (cachedChat?.chatId == chatId && cachedMessages.isNotEmpty()) {
            Log.d(TAG, "🔄 Restoring ${cachedMessages.size} cached messages")
            _messagesState.value = Resource.Success(cachedMessages)
        }

        // Cancel any existing listener before starting a new one
        messageListenerJob?.cancel()
        Log.d(TAG, "👂 Starting message listener for chatId=$chatId")

        messageListenerJob = viewModelScope.launch {
            _messagesState.value = Resource.Loading

            chatRepository.listenToMessages(chatId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "📩 ${result.data.size} messages received")
                        cachedMessages = result.data
                        if (result.data.isNotEmpty()) markAsRead(chatId)
                    }
                    is Resource.Error -> Log.e(TAG, "❌ Message listener error: ${result.message}")
                    is Resource.Loading -> Log.d(TAG, "⏳ Messages loading...")
                    else -> Unit
                }
                _messagesState.value = result
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) {
            Log.e(TAG, "❌ sendMessage — text is blank")
            return
        }

        // Prefer live value, fall back to cache
        val chat = _currentChat.value ?: cachedChat?.also {
            Log.d(TAG, "🔄 sendMessage — restoring chat from cache: ${it.chatId}")
            _currentChat.value = it
        }

        if (chat == null) {
            Log.e(TAG, "❌ sendMessage — no active chat. Call startChat* first.")
            return
        }

        val message = Message(
            chatId = chat.chatId,
            senderId = currentUserId,
            senderType = currentUserRole,
            senderName = currentUserName.ifBlank {
                when (currentUserRole) {
                    "customer" -> "Customer"
                    "shop_owner" -> "Shop Owner"
                    else -> "User"
                }
            },
            text = text,
            timeSent = com.google.firebase.Timestamp.now()
        )

        Log.d(TAG, "📤 Sending message to chatId=${chat.chatId}")

        viewModelScope.launch {
            chatRepository.sendMessage(chat.chatId, message).collect { result ->
                Log.d(TAG, "   sendMessage result: ${result::class.simpleName}")
                _sendMessageState.value = result
            }
        }
    }

    private fun sendSystemMessage(chatId: String, text: String) {
        val msg = Message(
            chatId = chatId,
            senderId = currentUserId,
            senderType = "system",
            senderName = "System",
            text = text,
            timeSent = com.google.firebase.Timestamp.now()
        )
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, msg).collect {
                Log.d(TAG, "   system message result: ${it::class.simpleName}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chat list
    // ─────────────────────────────────────────────────────────────────────────

    fun loadUserChats() {
        viewModelScope.launch {
            ensureUserLoaded()   // wait for async init before checking
            if (currentUserId.isBlank()) {
                Log.e(TAG, "❌ loadUserChats — userId blank even after ensureUserLoaded")
                _chatsState.value = Resource.Error("User not authenticated")
                return@launch
            }
            Log.d(TAG, "📋 loadUserChats userId=$currentUserId")
            chatRepository.listenToUserChats(currentUserId).collect {
                _chatsState.value = it
            }
        }
    }

    fun loadShopChats(shopId: String) {
        if (shopId.isBlank()) {
            Log.e(TAG, "❌ loadShopChats — shopId is blank")
            return
        }
        Log.d(TAG, "📋 loadShopChats shopId=$shopId")
        viewModelScope.launch {
            ensureUserLoaded()   // sets currentUserId for markAsRead calls
            chatRepository.listenToShopChats(shopId).collect {
                _chatsState.value = it
            }
        }
    }

    fun loadUnreadCount() {
        if (currentUserId.isBlank()) return
        Log.d(TAG, "📊 loadUnreadCount userId=$currentUserId role=$currentUserRole")
        viewModelScope.launch {
            val flow = if (currentUserRole == "shop_owner") {
                chatRepository.getShopUnreadCount(currentUserId)
            } else {
                chatRepository.getUnreadCount(currentUserId)
            }
            flow.collect { _unreadCount.value = it }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────

    fun markAsRead(chatId: String) {
        if (chatId.isBlank() || currentUserId.isBlank()) return
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId, currentUserId).collect {}
        }
    }

    /**
     * Directly sets a chat (e.g. when navigating from a chat list).
     * Starts message listener automatically.
     */
    fun setCurrentChat(chat: Chat) {
        Log.d(TAG, "📌 setCurrentChat chatId=${chat.chatId}")
        cachedChat = chat
        _currentChat.value = chat
        isChatInitialized = true
        listenToMessages(chat.chatId)
    }

    /**
     * Resets all UI state. Typically called when leaving the chat screen entirely.
     * Does NOT clear the cache — navigate back and forth remains instant.
     */
    fun resetChatStates() {
        Log.d(TAG, "🔄 resetChatStates()")
        messageListenerJob?.cancel()
        messageListenerJob = null
        isChatInitialized = false
        _chatsState.value = Resource.Idle
        _messagesState.value = Resource.Idle
        _currentChat.value = null
        _sendMessageState.value = Resource.Idle
        _createChatState.value = Resource.Idle
        _unreadCount.value = Resource.Idle
    }

    override fun onCleared() {
        Log.d(TAG, "💀 ChatViewModel onCleared — cachedChat=${cachedChat?.chatId}")
        messageListenerJob?.cancel()
        super.onCleared()
    }
}