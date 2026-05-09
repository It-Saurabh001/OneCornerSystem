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

    // Tracks the active shop-chats listener job
    private var shopChatsJob: Job? = null

    // Guards loadShopChats from restarting when already listening to same shopId
    private var currentlyListeningShopId: String = ""

    // Tracks the active shop total-unread listener job
    private var shopTotalUnreadJob: Job? = null
    private var totalUnreadShopId: String = ""

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

    /** Real-time total shopUnreadCount — drives the bottom nav badge. */
    private val _totalShopUnreadCount = MutableStateFlow(0)
    val totalShopUnreadCount: StateFlow<Int> = _totalShopUnreadCount.asStateFlow()

    /** Set after a chat is soft-deleted; consumed by the UI to show Undo Snackbar. */
    private val _deletedChat = MutableStateFlow<Chat?>(null)
    val deletedChat: StateFlow<Chat?> = _deletedChat.asStateFlow()

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

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                currentUserId   = sessionManager.userId.first() ?: ""
                currentUserRole = sessionManager.userRole.first() ?: ""
                Log.d(TAG, "👤 Session loaded — userId=$currentUserId role=$currentUserRole")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load session: ${e.message}")
            }
        }
    }

    private suspend fun ensureUserLoaded() {
        if (currentUserId.isBlank()) {
            currentUserId   = sessionManager.userId.first() ?: ""
            currentUserRole = sessionManager.userRole.first() ?: ""
            Log.d(TAG, "👤 ensureUserLoaded — userId=$currentUserId role=$currentUserRole")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User info update (called from UI with display name / avatar)
    // ─────────────────────────────────────────────────────────────────────────

    fun updateUserInfo(name: String, image: String) {
        Log.d(TAG, "👤 updateUserInfo() name=$name hasImage=${image.isNotBlank()}")
        currentUserName  = name
        currentUserImage = image
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chat creation — customer side
    // ─────────────────────────────────────────────────────────────────────────

    fun startChatFromShop(
        shopId: String,
        shopName: String,
        shopImage: String,
        serviceName: String = ""
    ) {
        if (isChatInitialized) {
            Log.d(TAG, "⚠️ startChatFromShop — already initialized, skipping")
            return
        }
        isChatInitialized = true

        viewModelScope.launch {
            ensureUserLoaded()
            if (currentUserId.isBlank()) {
                _createChatState.value = Resource.Error("User not logged in")
                isChatInitialized = false
                return@launch
            }
            val displayName = currentUserName.ifBlank { "Customer" }
            _createChatState.value = Resource.Loading

            chatRepository.getOrCreateChat(
                userId           = currentUserId,
                shopId           = shopId,
                userName         = displayName,
                shopName         = shopName,
                userProfileImage = currentUserImage,
                shopProfileImage = shopImage,
                bookingId        = "",
                serviceName      = serviceName
            ).collect { result ->
                handleChatResult(result)
                _createChatState.value = result
            }
        }
    }

    fun startChatFromBooking(
        shopId: String,
        shopName: String,
        shopImage: String,
        bookingId: String,
        serviceName: String = ""
    ) {
        if (isChatInitialized) {
            Log.d(TAG, "⚠️ startChatFromBooking — already initialized, skipping")
            return
        }
        isChatInitialized = true

        viewModelScope.launch {
            ensureUserLoaded()
            if (currentUserId.isBlank()) {
                _createChatState.value = Resource.Error("User not logged in")
                isChatInitialized = false
                return@launch
            }
            val displayName = currentUserName.ifBlank { "Customer" }
            _createChatState.value = Resource.Loading

            chatRepository.getOrCreateChat(
                userId           = currentUserId,
                shopId           = shopId,
                userName         = displayName,
                shopName         = shopName,
                userProfileImage = currentUserImage,
                shopProfileImage = shopImage,
                bookingId        = bookingId,
                serviceName      = serviceName
            ).collect { result ->
                handleChatResult(result)
                _createChatState.value = result
            }
        }
    }

    fun startChatAsShopOwner(
        shopId: String,
        shopName: String,
        shopImage: String,
        customerId: String,
        customerName: String,
        customerImage: String,
        bookingId: String? = null,
        serviceName: String = ""
    ) {
        if (isChatInitialized) {
            Log.d(TAG, "⚠️ startChatAsShopOwner — already initialized, skipping")
            return
        }
        isChatInitialized = true

        viewModelScope.launch {
            ensureUserLoaded()
            _createChatState.value = Resource.Loading

            chatRepository.getOrCreateChat(
                userId           = customerId,
                shopId           = shopId,
                userName         = customerName,
                shopName         = shopName,
                userProfileImage = customerImage,
                shopProfileImage = shopImage,
                bookingId        = bookingId ?: "",
                serviceName      = serviceName
            ).collect { result ->
                handleChatResult(result)
                _createChatState.value = result
            }
        }
    }

    // Convenience alias kept for backward-compat
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
                isChatInitialized = false
            }
            is Resource.Loading -> Log.d(TAG, "⏳ Chat loading...")
            else -> Unit
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Messages
    // ─────────────────────────────────────────────────────────────────────────

    fun listenToMessages(chatId: String) {
        if (chatId.isBlank()) {
            Log.e(TAG, "❌ listenToMessages — chatId is blank")
            _messagesState.value = Resource.Error("Chat ID cannot be empty")
            return
        }

        if (cachedChat?.chatId == chatId && cachedMessages.isNotEmpty()) {
            Log.d(TAG, "🔄 Restoring ${cachedMessages.size} cached messages")
            _messagesState.value = Resource.Success(cachedMessages)
        }

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

        val chat = _currentChat.value ?: cachedChat?.also {
            Log.d(TAG, "🔄 sendMessage — restoring chat from cache: ${it.chatId}")
            _currentChat.value = it
        }

        if (chat == null) {
            Log.e(TAG, "❌ sendMessage — no active chat")
            return
        }

        val message = Message(
            chatId     = chat.chatId,
            senderId   = currentUserId,
            senderType = currentUserRole,
            senderName = currentUserName.ifBlank {
                when (currentUserRole) {
                    "customer"   -> "Customer"
                    "shop_owner" -> "Shop Owner"
                    else         -> "User"
                }
            },
            text      = text,
            timeSent  = com.google.firebase.Timestamp.now()
        )

        Log.d(TAG, "📤 Sending message to chatId=${chat.chatId}")

        viewModelScope.launch {
            chatRepository.sendMessage(chat.chatId, message).collect { result ->
                Log.d(TAG, "   sendMessage result: ${result::class.simpleName}")
                _sendMessageState.value = result
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chat list
    // ─────────────────────────────────────────────────────────────────────────

    fun loadUserChats() {
        viewModelScope.launch {
            ensureUserLoaded()
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
        if (shopId == currentlyListeningShopId && shopChatsJob?.isActive == true) {
            Log.d(TAG, "⏭️ loadShopChats — already listening to $shopId, skipping")
            return
        }
        currentlyListeningShopId = shopId
        Log.d(TAG, "📋 loadShopChats — starting new listener for shopId=$shopId")
        shopChatsJob?.cancel()
        shopChatsJob = viewModelScope.launch {
            ensureUserLoaded()
            chatRepository.listenToShopChats(shopId).collect { result ->
                Log.d(TAG, "📋 loadShopChats emission: ${result::class.simpleName}")
                val deduped = if (result is Resource.Success) {
                    Resource.Success(result.data.distinctBy { it.chatId })
                } else result
                _chatsState.value = deduped
            }
            currentlyListeningShopId = ""
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Total shop unread (for bottom nav badge)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts a real-time listener for total shopUnreadCount. Call this once
     * when the ShopOwnerHomeScreen becomes visible, passing the resolved shopId.
     * Idempotent — does nothing if already listening to the same shopId.
     */
    fun listenToShopTotalUnread(shopId: String) {
        if (shopId.isBlank()) return
        if (shopId == totalUnreadShopId && shopTotalUnreadJob?.isActive == true) return

        totalUnreadShopId   = shopId
        shopTotalUnreadJob?.cancel()
        shopTotalUnreadJob = viewModelScope.launch {
            chatRepository.listenToShopTotalUnread(shopId).collect { result ->
                if (result is Resource.Success) {
                    _totalShopUnreadCount.value = result.data
                }
            }
        }
        Log.d(TAG, "🔔 listenToShopTotalUnread started for shopId=$shopId")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Soft-delete
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes a chat for the shop owner.
     * Stores the deleted chat in [deletedChat] so the UI can offer Undo.
     */
    fun deleteChatForShop(chat: Chat) {
        _deletedChat.value = chat
        viewModelScope.launch {
            chatRepository.deleteChatForShop(chat.chatId).collect {
                Log.d(TAG, "🗑️ deleteChatForShop result: ${it::class.simpleName}")
            }
        }
    }

    /**
     * Soft-deletes a chat for the customer.
     * Stores the deleted chat in [deletedChat] so the UI can offer Undo.
     */
    fun deleteChatForCustomer(chat: Chat) {
        _deletedChat.value = chat
        viewModelScope.launch {
            chatRepository.deleteChatForCustomer(chat.chatId).collect {
                Log.d(TAG, "🗑️ deleteChatForCustomer result: ${it::class.simpleName}")
            }
        }
    }

    /** Undoes the last shop soft-delete (called from Snackbar Undo button). */
    fun undoDeleteForShop() {
        val chat = _deletedChat.value ?: return
        _deletedChat.value = null
        viewModelScope.launch {
            chatRepository.undoDeleteChatForShop(chat.chatId).collect {
                Log.d(TAG, "↩️ undoDeleteForShop result: ${it::class.simpleName}")
            }
        }
    }

    /** Undoes the last customer soft-delete. */
    fun undoDeleteForCustomer() {
        val chat = _deletedChat.value ?: return
        _deletedChat.value = null
        viewModelScope.launch {
            chatRepository.undoDeleteChatForCustomer(chat.chatId).collect {
                Log.d(TAG, "↩️ undoDeleteForCustomer result: ${it::class.simpleName}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Unread count (one-shot)
    // ─────────────────────────────────────────────────────────────────────────

    fun loadUnreadCount() {
        if (currentUserId.isBlank()) return
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
            chatRepository.markMessagesAsRead(chatId, currentUserId, currentUserRole).collect {}
        }
    }

    /**
     * Pre-seeds the current chat from the list screen so the UI can show the
     * top-bar name immediately while the real startChatAs* call completes.
     *
     * IMPORTANT: Do NOT set isChatInitialized = true here.
     * ShopChatScreen calls startChatAsShopOwner() in its own LaunchedEffect,
     * which must be allowed to run so Firestore confirms the correct chatId.
     */
    fun setCurrentChat(chat: Chat) {
        Log.d(TAG, "📌 setCurrentChat chatId=${chat.chatId} (pre-seeding only)")
        cachedChat = chat
        _currentChat.value = chat
        // NOTE: isChatInitialized intentionally NOT set
        listenToMessages(chat.chatId)
    }

    /**
     * Resets all UI state. Typically called when leaving the chat screen entirely.
     * Does NOT clear the cache — navigate back and forth remains instant.
     */
    fun resetChatStates() {
        Log.d(TAG, "🔄 resetChatStates() — clearing per-conversation state only")
        messageListenerJob?.cancel()
        messageListenerJob = null
        isChatInitialized  = false
        _messagesState.value    = Resource.Idle
        _currentChat.value      = null
        _sendMessageState.value = Resource.Idle
        _createChatState.value  = Resource.Idle
        _unreadCount.value      = Resource.Idle
    }

    override fun onCleared() {
        Log.d(TAG, "💀 ChatViewModel onCleared — cachedChat=${cachedChat?.chatId}")
        messageListenerJob?.cancel()
        shopTotalUnreadJob?.cancel()
        super.onCleared()
    }
}