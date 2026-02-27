package com.saurabh.onecornersystem.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


//@HiltViewModel
class CommonShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

//    / Shop Details - Dono ke liye common
    private val _shopDetailsState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val shopDetailsState: StateFlow<Resource<Shop>> = _shopDetailsState.asStateFlow()

    // Shop Rating - Dono dekh sakte hain
    private val _shopRatingState = MutableStateFlow<Resource<Double>>(Resource.Loading)
    val shopRatingState: StateFlow<Resource<Double>> = _shopRatingState.asStateFlow()

    // Combined Loading State
    val isLoading: StateFlow<Boolean> = combine(
        _shopDetailsState,
        _shopRatingState
    ) { states ->
        states.any { it is Resource.Loading }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // ============= SHOP DETAILS =============

    fun getShopDetails(shopId: String) {
        if (shopId.isBlank()) {
            _shopDetailsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.getShopDetails(shopId).collect { result ->
                _shopDetailsState.value = result
            }
        }
    }

    fun listenToShopDetails(shopId: String) {
        if (shopId.isBlank()) {
            _shopDetailsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.listenToShopDetails(shopId).collect { result ->
                _shopDetailsState.value = result
            }
        }
    }

    // ============= SHOP RATING =============

    fun getShopRating(shopId: String) {
        if (shopId.isBlank()) {
            _shopRatingState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.getShopRating(shopId).collect { result ->
                _shopRatingState.value = result
            }
        }
    }

    // ============= COMMON HELPER FUNCTIONS =============

    fun isShopOpen(shop: Shop?): Boolean {
        return shop != null && shop.isOpen && shop.isActive
    }

    fun getShopStatusMessage(shop: Shop?): String {
        if (shop == null) return "Shop not found"
        return when {
            !shop.isActive -> "Permanently Closed"
            !shop.isOpen -> "Currently Closed"
            else -> "Open • ${shop.openingTime} - ${shop.closingTime}"
        }
    }

    fun formatShopAddress(shop: Shop?): String {
        if (shop == null) return ""
        return buildString {
            append(shop.address)
            if (shop.city.isNotBlank()) append(", ${shop.city}")
            if (shop.pincode.isNotBlank()) append(" - ${shop.pincode}")
        }
    }

    fun getShopTimings(shop: Shop?): String {
        if (shop == null) return ""
        return "${shop.openingTime} to ${shop.closingTime}"
    }

    fun getFormattedRating(shop: Shop?): String {
        if (shop == null) return ""
        return if (shop.totalRatings > 0) {
            String.format("%.1f (%d ratings)", shop.rating, shop.totalRatings)
        } else {
            "No ratings yet"
        }
    }

    fun canAcceptOrders(shop: Shop?): Boolean {
        return shop != null &&
                shop.isOpen &&
                shop.isActive &&
                shop.totalItems > 0
    }

    fun getCategoryDisplay(category: String): String {
        return when (category.lowercase()) {
            "restaurant" -> "Restaurant"
            "grocery" -> "Grocery Store"
            "medical" -> "Medical Store"
            "bakery" -> "Bakery"
            "electronics" -> "Electronics"
            "fashion" -> "Fashion"
            else -> category
        }
    }


    fun hasLogo(shop: Shop?): Boolean {
        return shop?.hasLogo == true && shop.logo.isNotBlank()
    }

    fun hasCover(shop: Shop?): Boolean {
        return shop?.hasCover == true && shop.coverImage.isNotBlank()
    }

    fun getLogo(shop: Shop?): String {
        return shop?.logo ?: ""
    }

    fun getCover(shop: Shop?): String {
        return shop?.coverImage ?: ""
    }


    fun getShopCompletionPercentage(shop: Shop?): Int {
        if (shop == null) return 0

        var score = 0
        val totalFields = 8

        if (shop.shopName.isNotBlank()) score++
        if (shop.description.isNotBlank()) score++
        if (shop.category.isNotBlank()) score++
        if (shop.address.isNotBlank()) score++
        if (shop.contactNumber.isNotBlank()) score++
        if (hasLogo(shop)) score++
        if (hasCover(shop)) score++
        if (shop.operatingHours.isNotEmpty()) score++

        return (score * 100) / totalFields
    }


    // ============= STATE RESET FUNCTIONS =============

    fun resetShopDetails() {
        _shopDetailsState.value = Resource.Loading
    }

    fun resetShopRating() {
        _shopRatingState.value = Resource.Loading
    }

    fun resetAllStates() {
        _shopDetailsState.value = Resource.Loading
        _shopRatingState.value = Resource.Loading
    }

    // ============= CLEANUP =============

    override fun onCleared() {
        super.onCleared()
        resetAllStates()
    }



}
//
//┌─────────────────────────────────────────────────────────┐
//│                 COMMON SHOP VIEWMODEL                   │
//│            (Customer + Shop Owner dono use)             │
//└─────────────────────────────────────────────────────────┘
//│
//▼
//┌─────────────────────────────────────────────────────────┐
//│  🔵 STATES (2)                                           │
//├─────────────────────────────────────────────────────────┤
//│  • _shopDetailsState  → shopDetailsState  (Shop data)   │
//│  • _shopRatingState   → shopRatingState   (Rating data) │
//└─────────────────────────────────────────────────────────┘
//│
//▼
//┌─────────────────────────────────────────────────────────┐
//│  🟢 MAIN FUNCTIONS (3)                                   │
//├─────────────────────────────────────────────────────────┤
//│  • getShopDetails()      - Fetch shop by ID             │
//│  • listenToShopDetails() - Real-time updates            │
//│  • getShopRating()       - Get shop rating              │
//└─────────────────────────────────────────────────────────┘
//│
//▼
//┌─────────────────────────────────────────────────────────┐
//│  🟡 HELPER FUNCTIONS (10+)                               │
//├─────────────────────────────────────────────────────────┤
//│  • isShopOpen()          - Check open/closed            │
//│  • getShopStatusMessage()- "Open • 9AM-9PM" etc.        │
//│  • formatShopAddress()   - "Street, City - Pincode"     │
//│  • getShopTimings()      - "9:00 to 21:00"              │
//│  • getFormattedRating()  - "4.5 (100 ratings)"          │
//│  • canAcceptOrders()     - Open + Active + Products     │
//│  • getCategoryDisplay()  - "🍽️ Restaurant" etc.         │
//│  • hasLogo()             - Logo exists?                  │
//│  • hasCover()            - Cover exists?                 │
//│  • getShopCompletion()   - Profile completion %          │
//└─────────────────────────────────────────────────────────┘
//│
//▼
//┌─────────────────────────────────────────────────────────┐
//│  🔴 RESET FUNCTIONS (3)                                  │
//├─────────────────────────────────────────────────────────┤
//│  • resetShopDetails()    - Clear shop data              │
//│  • resetShopRating()     - Clear rating data            │
//│  • resetAllStates()      - Clear everything             │
//└─────────────────────────────────────────────────────────┘