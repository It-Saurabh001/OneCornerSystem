package com.saurabh.onecornersystem.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
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


@HiltViewModel
class CustomerShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {
    // All Nearby Shops
    private val _nearbyShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val nearbyShopsState: StateFlow<Resource<List<Shop>>> = _nearbyShopsState.asStateFlow()

    // Filtered by Product
    private val _nearbyProductShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val nearbyProductShopsState: StateFlow<Resource<List<Shop>>> = _nearbyProductShopsState.asStateFlow()

    private val _favoriteProductShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val favoriteProductShopsState: StateFlow<Resource<List<Shop>>> = _favoriteProductShopsState.asStateFlow()

    // Filtered by Service
    private val _nearbyServiceShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val nearbyServiceShopsState: StateFlow<Resource<List<Shop>>> = _nearbyServiceShopsState.asStateFlow()

    private val _favoriteServiceShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val favoriteServiceShopsState: StateFlow<Resource<List<Shop>>> = _favoriteServiceShopsState.asStateFlow()

    // Search Results
    private val _searchResultsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val searchResultsState: StateFlow<Resource<List<Shop>>> = _searchResultsState.asStateFlow()

    // Favorite Shops
    private val _favoriteShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Loading)
    val favoriteShopsState: StateFlow<Resource<List<Shop>>> = _favoriteShopsState.asStateFlow()

    // Shop Details (for individual shop view)
    private val _shopDetailsState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val shopDetailsState: StateFlow<Resource<Shop>> = _shopDetailsState.asStateFlow()

    // Shop Rating
    private val _shopRatingState = MutableStateFlow<Resource<Double>>(Resource.Loading)
    val shopRatingState: StateFlow<Resource<Double>> = _shopRatingState.asStateFlow()

    // Shop Categories
    private val _shopCategoriesState = MutableStateFlow<Resource<List<CategoryWithType>>>(Resource.Loading)
    val shopCategoriesState: StateFlow<Resource<List<CategoryWithType>>> = _shopCategoriesState.asStateFlow()

    private val _productCategoriesState = MutableStateFlow<Resource<List<CategoryWithType>>>(Resource.Loading)
    val productCategoriesState: StateFlow<Resource<List<CategoryWithType>>> = _productCategoriesState.asStateFlow()

    private val _serviceCategoriesState = MutableStateFlow<Resource<List<CategoryWithType>>>(Resource.Loading)
    val serviceCategoriesState: StateFlow<Resource<List<CategoryWithType>>> = _serviceCategoriesState.asStateFlow()

    // Selected Filter
    private val _selectedShopType = MutableStateFlow<ShopType?>(null)
    val selectedShopType: StateFlow<ShopType?> = _selectedShopType.asStateFlow()

    // Combined Loading State
    val isLoading: StateFlow<Boolean> = combine(
        _nearbyShopsState,
        _nearbyProductShopsState,
        _nearbyServiceShopsState,
        _searchResultsState,
        _favoriteShopsState,
        _favoriteProductShopsState,
        _favoriteServiceShopsState,
        _shopCategoriesState,
        _productCategoriesState,
        _serviceCategoriesState,
        _shopDetailsState,
        _shopRatingState
    ) { states ->
        states.any { it is Resource.Loading }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // ============= NEARBY SHOPS =============

    fun getNearbyShops(
        latitude: Double,
        longitude: Double,
        shopType: ShopType? = null,
        radiusInKm: Double = 10.0
    ) {
        viewModelScope.launch {
            when (shopType) {
                null -> {
                    _nearbyShopsState.value = Resource.Loading
                    try {
                        shopRepository.getNearbyShops(latitude, longitude, radiusInKm)
                            .collect { _nearbyShopsState.value = it }
                    } catch (e: Exception) {
                        _nearbyShopsState.value = Resource.Error(e.message ?: "Failed to get nearby shops")
                    }
                }
                ShopType.PRODUCT -> {
                    _nearbyProductShopsState.value = Resource.Loading
                    try {
                        shopRepository.getNearbyShopsByType(latitude, longitude, radiusInKm, ShopType.PRODUCT)
                            .collect { _nearbyProductShopsState.value = it }
                    } catch (e: Exception) {
                        _nearbyProductShopsState.value = Resource.Error(e.message ?: "Failed to get nearby product shops")
                    }
                }
                ShopType.SERVICE -> {
                    _nearbyServiceShopsState.value = Resource.Loading
                    try {
                        shopRepository.getNearbyShopsByType(latitude, longitude, radiusInKm, ShopType.SERVICE)
                            .collect { _nearbyServiceShopsState.value = it }
                    } catch (e: Exception) {
                        _nearbyServiceShopsState.value = Resource.Error(e.message ?: "Failed to get nearby service shops")
                    }
                }
            }
        }
    }

    fun getNearbyProductShops(latitude: Double, longitude: Double, radiusInKm: Double = 10.0) {
        getNearbyShops(latitude, longitude, ShopType.PRODUCT, radiusInKm)
    }

    fun getNearbyServiceShops(latitude: Double, longitude: Double, radiusInKm: Double = 10.0) {
        getNearbyShops(latitude, longitude, ShopType.SERVICE, radiusInKm)
    }

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

    // ============= SEARCH =============

    fun searchShops(query: String, shopType: ShopType? = null) {
        if (query.isBlank()) {
            _searchResultsState.value = Resource.Error("Search query cannot be empty")
            return
        }

        viewModelScope.launch {
            _searchResultsState.value = Resource.Loading
            try {
                shopRepository.searchShops(query, shopType).collect { _searchResultsState.value = it }
            } catch (e: Exception) {
                _searchResultsState.value = Resource.Error(e.message ?: "Search failed")
            }
        }
    }

    fun searchProductShops(query: String) = searchShops(query, ShopType.PRODUCT)
    fun searchServiceShops(query: String) = searchShops(query, ShopType.SERVICE)

    // ============= CATEGORIES =============

    fun getShopCategories() {
        viewModelScope.launch {
            _shopCategoriesState.value = Resource.Loading
            try {
                shopRepository.getAllCategoriesWithType().collect { _shopCategoriesState.value = it }
            } catch (e: Exception) {
                _shopCategoriesState.value = Resource.Error(e.message ?: "Failed to get categories")
            }
        }
    }

    fun getProductCategories() {
        viewModelScope.launch {
            _productCategoriesState.value = Resource.Loading
            try {
                shopRepository.getProductCategoriesWithType().collect { _productCategoriesState.value = it }
            } catch (e: Exception) {
                _productCategoriesState.value = Resource.Error(e.message ?: "Failed to get product categories")
            }
        }
    }

    fun getServiceCategories() {
        viewModelScope.launch {
            _serviceCategoriesState.value = Resource.Loading
            try {
                shopRepository.getServiceCategoriesWithType().collect { _serviceCategoriesState.value = it }
            } catch (e: Exception) {
                _serviceCategoriesState.value = Resource.Error(e.message ?: "Failed to get service categories")
            }
        }
    }

    fun getShopsByCategory(category: String, shopType: ShopType? = null) {
        if (category.isBlank()) {
            _searchResultsState.value = Resource.Error("Category cannot be empty")
            return
        }

        viewModelScope.launch {
            _searchResultsState.value = Resource.Loading
            try {
                shopRepository.getShopsByCategory(category, shopType).collect { _searchResultsState.value = it }
            } catch (e: Exception) {
                _searchResultsState.value = Resource.Error(e.message ?: "Failed to get shops")
            }
        }
    }

    // ============= FAVORITES =============

    fun addToFavorites(shop: Shop) {
        if (shop.shopId.isBlank()) return
        viewModelScope.launch {
            try {
                shopRepository.addToFavorites(shop)
                getFavoriteShops()
                when (shop.shopType) {
                    ShopType.PRODUCT -> getFavoriteProductShops()
                    ShopType.SERVICE -> getFavoriteServiceShops()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeFromFavorites(shop: Shop) {
        if (shop.shopId.isBlank()) return
        viewModelScope.launch {
            try {
                shopRepository.removeFromFavorites(shop.shopId)
                getFavoriteShops()
                when (shop.shopType) {
                    ShopType.PRODUCT -> getFavoriteProductShops()
                    ShopType.SERVICE -> getFavoriteServiceShops()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getFavoriteShops() {
        viewModelScope.launch {
            _favoriteShopsState.value = Resource.Loading
            try {
                shopRepository.getFavoriteShops().collect { _favoriteShopsState.value = it }
            } catch (e: Exception) {
                _favoriteShopsState.value = Resource.Error(e.message ?: "Failed to get favorites")
            }
        }
    }

    fun getFavoriteProductShops() {
        viewModelScope.launch {
            _favoriteProductShopsState.value = Resource.Loading
            try {
                shopRepository.getFavoriteShopsByType(ShopType.PRODUCT).collect { _favoriteProductShopsState.value = it }
            } catch (e: Exception) {
                _favoriteProductShopsState.value = Resource.Error(e.message ?: "Failed to get favorite product shops")
            }
        }
    }

    fun getFavoriteServiceShops() {
        viewModelScope.launch {
            _favoriteServiceShopsState.value = Resource.Loading
            try {
                shopRepository.getFavoriteShopsByType(ShopType.SERVICE).collect { _favoriteServiceShopsState.value = it }
            } catch (e: Exception) {
                _favoriteServiceShopsState.value = Resource.Error(e.message ?: "Failed to get favorite service shops")
            }
        }
    }

    fun isFavorite(shopId: String): Boolean = false // TODO: Implement with repository

    // ============= FILTER MANAGEMENT =============

    fun setSelectedShopType(shopType: ShopType?) {
        _selectedShopType.value = shopType
    }

    fun clearShopTypeFilter() {
        _selectedShopType.value = null
    }

    // ============= HELPER FUNCTIONS (MOVED FROM COMMON) =============

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
            "restaurant" -> "Restaurant 🍽️"
            "grocery" -> "Grocery Store 🛒"
            "medical" -> "Medical Store 💊"
            "bakery" -> "Bakery 🥖"
            "electronics" -> "Electronics 📱"
            "fashion" -> "Fashion 👕"
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

    fun isProductShop(shop: Shop?): Boolean = shop?.shopType == ShopType.PRODUCT
    fun isServiceShop(shop: Shop?): Boolean = shop?.shopType == ShopType.SERVICE

    fun getShopTypeIcon(shop: Shop?): String = when (shop?.shopType) {
        ShopType.PRODUCT -> "🛍️"
        ShopType.SERVICE -> "🔧"
        null -> "🏪"
    }

    fun getShopTypeDisplayName(shop: Shop?): String = when (shop?.shopType) {
        ShopType.PRODUCT -> "Product Shop"
        ShopType.SERVICE -> "Service Shop"
        null -> "Shop"
    }

    fun filterShopsByType(shops: List<Shop>, type: ShopType?): List<Shop> {
        return if (type == null) shops else shops.filter { it.shopType == type }
    }

    // ============= STATE RESET =============

    fun resetNearbyShops() {
        _nearbyShopsState.value = Resource.Loading
        _nearbyProductShopsState.value = Resource.Loading
        _nearbyServiceShopsState.value = Resource.Loading
    }

    fun resetSearchResults() {
        _searchResultsState.value = Resource.Loading
    }

    fun resetFavorites() {
        _favoriteShopsState.value = Resource.Loading
        _favoriteProductShopsState.value = Resource.Loading
        _favoriteServiceShopsState.value = Resource.Loading
    }

    fun resetCategories() {
        _shopCategoriesState.value = Resource.Loading
        _productCategoriesState.value = Resource.Loading
        _serviceCategoriesState.value = Resource.Loading
    }

    fun resetShopDetails() {
        _shopDetailsState.value = Resource.Loading
    }

    fun resetShopRating() {
        _shopRatingState.value = Resource.Loading
    }

    fun resetAll() {
        resetNearbyShops()
        resetSearchResults()
        resetFavorites()
        resetCategories()
        resetShopDetails()
        resetShopRating()
        clearShopTypeFilter()
    }

    // ============= CLEANUP =============

    override fun onCleared() {
        super.onCleared()
        resetAll()
    }



    private fun mockAllShops(): List<Shop> = listOf(
        Shop(shopName = "Pizza House", category = "Restaurant", rating = 4.5, shopType = ShopType.PRODUCT),
        Shop(shopName = "Fresh Mart", category = "Grocery", rating = 4.2, shopType = ShopType.PRODUCT),
        Shop(shopName = "MedPlus", category = "Medical", rating = 4.8, shopType = ShopType.PRODUCT),
        Shop(shopName = "Quick Mechanic", category = "Automotive", rating = 4.7, shopType = ShopType.SERVICE),
        Shop(shopName = "Style Salon", category = "Beauty", rating = 4.5, shopType = ShopType.SERVICE)
    )

    private fun mockProductShops(): List<Shop> = mockAllShops().filter { it.shopType == ShopType.PRODUCT }
    private fun mockServiceShops(): List<Shop> = mockAllShops().filter { it.shopType == ShopType.SERVICE }

    private fun mockCategoriesWithType(): List<CategoryWithType> = listOf(
        CategoryWithType("Restaurant", ShopType.PRODUCT),
        CategoryWithType("Grocery", ShopType.PRODUCT),
        CategoryWithType("Medical", ShopType.PRODUCT),
        CategoryWithType("Automotive", ShopType.SERVICE),
        CategoryWithType("Beauty", ShopType.SERVICE)
    )

    private fun mockProductCategories(): List<String> = listOf("Restaurant", "Grocery", "Medical", "Bakery", "Electronics", "Fashion")
    private fun mockServiceCategories(): List<String> = listOf("Automotive", "Beauty", "Repair", "Cleaning", "Plumbing", "Electrical")


}

