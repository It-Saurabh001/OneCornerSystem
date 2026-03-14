package com.saurabh.onecornersystem.presentation.customer.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.*
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.repository.ShopItemRepository
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CustomerShopViewModel"

@HiltViewModel
class CustomerShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val shopItemRepository: ShopItemRepository
) : ViewModel() {

    // ============= NEARBY SHOPS STATES =============

    private val _nearbyShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val nearbyShopsState: StateFlow<Resource<List<Shop>>> = _nearbyShopsState.asStateFlow()

    private val _nearbyProductShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val nearbyProductShopsState: StateFlow<Resource<List<Shop>>> = _nearbyProductShopsState.asStateFlow()

    private val _nearbyServiceShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val nearbyServiceShopsState: StateFlow<Resource<List<Shop>>> = _nearbyServiceShopsState.asStateFlow()

    private val _serviceItemDetailsState = MutableStateFlow<Resource<ShopItem>>(Resource.Idle)
    val serviceItemDetailsState: StateFlow<Resource<ShopItem>> = _serviceItemDetailsState.asStateFlow()
    // ============= FAVORITES STATES =============

    private val _favoriteShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val favoriteShopsState: StateFlow<Resource<List<Shop>>> = _favoriteShopsState.asStateFlow()

    private val _favoriteProductShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val favoriteProductShopsState: StateFlow<Resource<List<Shop>>> = _favoriteProductShopsState.asStateFlow()

    private val _favoriteServiceShopsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val favoriteServiceShopsState: StateFlow<Resource<List<Shop>>> = _favoriteServiceShopsState.asStateFlow()

    // ============= SEARCH & DETAILS STATES =============

    private val _searchResultsState = MutableStateFlow<Resource<List<Shop>>>(Resource.Idle)
    val searchResultsState: StateFlow<Resource<List<Shop>>> = _searchResultsState.asStateFlow()

    private val _searchServicesState = MutableStateFlow<Resource<List<ShopItem>>>(Resource.Idle)
    val searchServicesState: StateFlow<Resource<List<ShopItem>>> = _searchServicesState.asStateFlow()

    private val _shopDetailsState = MutableStateFlow<Resource<Shop>>(Resource.Idle)
    val shopDetailsState: StateFlow<Resource<Shop>> = _shopDetailsState.asStateFlow()

    private val _shopRatingState = MutableStateFlow<Resource<Double>>(Resource.Idle)
    val shopRatingState: StateFlow<Resource<Double>> = _shopRatingState.asStateFlow()

    // ============= CATEGORIES STATES =============

    private val _shopCategoriesState = MutableStateFlow<Resource<List<CategoryWithType>>>(Resource.Idle)
    val shopCategoriesState: StateFlow<Resource<List<CategoryWithType>>> = _shopCategoriesState.asStateFlow()

    private val _productCategoriesState = MutableStateFlow<Resource<List<CategoryWithType>>>(Resource.Idle)
    val productCategoriesState: StateFlow<Resource<List<CategoryWithType>>> = _productCategoriesState.asStateFlow()

    private val _serviceCategoriesState = MutableStateFlow<Resource<List<CategoryWithType>>>(Resource.Idle)
    val serviceCategoriesState: StateFlow<Resource<List<CategoryWithType>>> = _serviceCategoriesState.asStateFlow()

    private val _nearbyServiceItemsState = MutableStateFlow<Resource<List<ShopItem>>>(Resource.Idle)
    val nearbyServiceItemsState: StateFlow<Resource<List<ShopItem>>> = _nearbyServiceItemsState.asStateFlow()

    // ============= FILTER STATES =============

    private val _selectedShopType = MutableStateFlow<ShopType?>(null)
    val selectedShopType: StateFlow<ShopType?> = _selectedShopType.asStateFlow()

    // ============= BOOKING STATES =============

    private val _createBookingState = MutableStateFlow<Resource<Booking>>(Resource.Idle)
    val createBookingState: StateFlow<Resource<Booking>> = _createBookingState.asStateFlow()

    private val _myBookingsState = MutableStateFlow<Resource<List<Booking>>>(Resource.Idle)
    val myBookingsState: StateFlow<Resource<List<Booking>>> = _myBookingsState.asStateFlow()

    private val _bookingDetailsState = MutableStateFlow<Resource<Booking>>(Resource.Idle)
    val bookingDetailsState: StateFlow<Resource<Booking>> = _bookingDetailsState.asStateFlow()

    private val _availableTimeSlotsState = MutableStateFlow<Resource<List<TimeSlot>>>(Resource.Idle)
    val availableTimeSlotsState: StateFlow<Resource<List<TimeSlot>>> = _availableTimeSlotsState.asStateFlow()

    private val _cancelBookingState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val cancelBookingState: StateFlow<Resource<Boolean>> = _cancelBookingState.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation = _userLocation.asStateFlow()

    // Helper function location update karne ke liye
    fun updateUserLocation(location: Location?) {
        _userLocation.value = location
    }



    // ============= INIT BLOCK - MOCK DATA COMMENTED OUT =============

    init {
        Log.d(TAG, "========== CustomerShopViewModel Initialized ==========")

        // 🔴 MOCK DATA COMMENTED OUT - Using real repository data
        /*
        viewModelScope.launch {
            Log.d(TAG, "⚠️ Creating mock data for testing (TEMPORARY)")
            val mockShops = listOf(
                Shop(
                    shopId = "1",
                    shopName = "Quick Mechanic",
                    category = "Automotive",
                    shopType = ShopType.SERVICE,
                    location = GeoPoint(26.9325, 80.9402),
                    address = "123 Main St",
                    rating = 4.5,
                    totalRatings = 128,
                    open = true,
                    active = true
                ),
                Shop(
                    shopId = "2",
                    shopName = "Style Salon",
                    category = "Beauty",
                    shopType = ShopType.SERVICE,
                    location = GeoPoint(26.9335, 80.9412),
                    address = "456 Park Ave",
                    rating = 4.2,
                    totalRatings = 89,
                    open = true,
                    active = true
                )
            )
            _nearbyServiceShopsState.value = Resource.Success(mockShops)
            Log.d(TAG, "✅ Mock data set to _nearbyServiceShopsState")
        }
        */

        Log.d(TAG, "✅ Mock data disabled - using real repository data")
    }

    // ============= COMBINED LOADING STATE =============

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
        _shopRatingState,
        _createBookingState,
        _myBookingsState,
        _bookingDetailsState,
        _availableTimeSlotsState,
        _cancelBookingState
    ) { states ->
        states.any { it is Resource.Loading }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // ============= NEARBY SHOPS FUNCTIONS =============

    fun getNearbyShops(
        latitude: Double,
        longitude: Double,
        shopType: ShopType? = null,
        radiusInKm: Double = 10.0
    ) {
        Log.d(TAG, "========== getNearbyShops ==========")
        Log.d(TAG, "📍 Location: ($latitude, $longitude)")
        Log.d(TAG, "🏷️ ShopType: $shopType")
        Log.d(TAG, "📏 Radius: $radiusInKm km")

        viewModelScope.launch {
            when (shopType) {
                null -> {
                    Log.d(TAG, "📦 Fetching ALL nearby shops")
                    _nearbyShopsState.value = Resource.Loading
                    try {
                        shopRepository.getNearbyShops(latitude, longitude, radiusInKm)
                            .collect { result ->
                                when (result) {
                                    is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} nearby shops")
                                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                                    is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                                    else -> {}
                                }
                                _nearbyShopsState.value = result
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Exception: ${e.message}", e)
                        _nearbyShopsState.value = Resource.Error(e.message ?: "Failed to get nearby shops")
                    }
                }
                ShopType.PRODUCT -> {
                    Log.d(TAG, "📦 Fetching PRODUCT shops")
                    _nearbyProductShopsState.value = Resource.Loading
                    try {
                        shopRepository.getNearbyShopsByType(latitude, longitude, radiusInKm, ShopType.PRODUCT)
                            .collect { result ->
                                when (result) {
                                    is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} product shops")
                                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                                    is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                                    else -> {}
                                }
                                _nearbyProductShopsState.value = result
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Exception: ${e.message}", e)
                        _nearbyProductShopsState.value = Resource.Error(e.message ?: "Failed to get nearby product shops")
                    }
                }
                ShopType.SERVICE -> {
                    Log.d(TAG, "📦 Fetching SERVICE shops")
                    _nearbyServiceShopsState.value = Resource.Loading
                    try {
                        shopRepository.getNearbyShopsByType(latitude, longitude, radiusInKm, ShopType.SERVICE)
                            .collect { result ->
                                when (result) {
                                    is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} service shops")
                                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                                    is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                                    else -> {}
                                }
                                _nearbyServiceShopsState.value = result
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Exception: ${e.message}", e)
                        _nearbyServiceShopsState.value = Resource.Error(e.message ?: "Failed to get nearby service shops")
                    }
                }
            }
        }
    }


    fun getNearbyServiceItems(latitude: Double, longitude: Double, radiusInKm: Double = 10.0) {
        Log.d(TAG, "========== getNearbyServiceItems ==========")
        Log.d(TAG, "📍 Location: ($latitude, $longitude), Radius: $radiusInKm km")

        viewModelScope.launch {
            _nearbyServiceItemsState.value = Resource.Loading

            try {
                // Step 1: ShopRepository se paas ki dukaanein fetch karo
                Log.d(TAG, "Fetching nearby service shops first...")
                shopRepository.getNearbyShopsByType(latitude, longitude, radiusInKm, ShopType.SERVICE)
                    .collect { shopResult ->
                        when (shopResult) {
                            is Resource.Loading -> {
                                Log.d(TAG, "Loading nearby shops...")
                                _nearbyServiceItemsState.value = Resource.Loading
                            }
                            is Resource.Error -> {
                                Log.e(TAG, "Error fetching nearby shops: ${shopResult.message}")
                                _nearbyServiceItemsState.value = Resource.Error(shopResult.message ?: "Failed to fetch nearby shops")
                            }
                            is Resource.Success -> {
                                val nearbyShops = shopResult.data

                                // Agar aas-paas koi dukaan nahi hai, toh empty list bhej do
                                if (nearbyShops.isEmpty()) {
                                    Log.d(TAG, "ℹNo nearby shops found in $radiusInKm km radius.")
                                    _nearbyServiceItemsState.value = Resource.Success(emptyList())
                                    return@collect
                                }

                                // Step 2: Un dukaano ki ID nikal lo (e.g., "PnzoDEFZUXCS6cSbFVUZ")
                                val nearbyShopIds = nearbyShops.map { it.shopId }.toSet()
                                Log.d(TAG, "Found ${nearbyShopIds.size} nearby shops. Fetching their services now...")

                                // Step 3: ShopItemRepository se saari services fetch karo
                                shopItemRepository.searchServices("", ShopType.SERVICE)
                                    .collect { itemResult ->
                                        when (itemResult) {
                                            is Resource.Loading -> {
                                                Log.d(TAG, "Loading services from repository...")
                                            }
                                            is Resource.Error -> {
                                                Log.e(TAG, "Error fetching services: ${itemResult.message}")
                                                _nearbyServiceItemsState.value = Resource.Error(itemResult.message ?: "Failed to fetch services")
                                            }
                                            is Resource.Success -> {
                                                val allItems = itemResult.data

                                                // Step 4: KOTLIN MAGIC - Sirf un services ko rakho jo nearby dukaano ki hain
                                                val nearbyItems = allItems.filter { it.shopId in nearbyShopIds }

                                                Log.d(TAG, "Success! Filtered ${nearbyItems.size} services for the home screen.")
                                                _nearbyServiceItemsState.value = Resource.Success(nearbyItems)
                                            }
                                            else -> {
                                                Log.w(TAG, "Unexpected state in itemResult: $itemResult")
                                            }
                                        }
                                    }
                            }
                            else -> {
                                Log.w(TAG, "Unexpected state in shopResult: $shopResult")
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getNearbyServiceItems", e)
                _nearbyServiceItemsState.value = Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    fun getServiceItemDetails(itemId: String) {
        Log.d(TAG, "========== getServiceItemDetails ==========")
        Log.d(TAG, "🆔 serviceId: $itemId")

        // 1. Validation: Agar ID empty hai toh aage mat badho
        if (itemId.isBlank()) {
            Log.e(TAG, "serviceId is blank")
            _serviceItemDetailsState.value = Resource.Error("Invalid Service ID")
            return
        }

        viewModelScope.launch {
            // 2. Reset State: Purana data saaf karo taaki user ko 'Loading' dikhe, pichla data nahi
            _serviceItemDetailsState.value = Resource.Loading

            try {
                shopItemRepository.getItemById(itemId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d(TAG, "Service Details Loaded: ${result.data.name}")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Repo Error: ${result.message}")
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading service details from repository...")
                        }
                        else -> {}
                    }
                    _serviceItemDetailsState.value = result
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getServiceItemDetails: ${e.message}", e)
                _serviceItemDetailsState.value = Resource.Error(e.message ?: "Failed to load details")
            }
        }
    }


    fun getNearbyProductShops(latitude: Double, longitude: Double, radiusInKm: Double = 10.0) {
        Log.d(TAG, "getNearbyProductShops called - delegating to getNearbyShops")
        getNearbyShops(latitude, longitude, ShopType.PRODUCT, radiusInKm)
    }

    fun getNearbyServiceShops(latitude: Double, longitude: Double, radiusInKm: Double = 10.0) {
        Log.d(TAG, "getNearbyServiceShops called - delegating to getNearbyShops")
        getNearbyShops(latitude, longitude, ShopType.SERVICE, radiusInKm)
    }

    // ============= SHOP DETAILS FUNCTIONS =============

    fun getShopDetails(shopId: String) {
        Log.d(TAG, "========== getShopDetails ==========")
        Log.d(TAG, "🆔 shopId: $shopId")

        if (shopId.isBlank()) {
            Log.e(TAG, "❌ shopId is blank")
            _shopDetailsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "⏳ Fetching shop details...")
            _shopDetailsState.value = Resource.Loading
            shopRepository.getShopDetails(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Shop found: ${result.data.shopName}")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                    else -> {}
                }
                _shopDetailsState.value = result
            }
        }
    }

    fun listenToShopDetails(shopId: String) {
        Log.d(TAG, "========== listenToShopDetails ==========")
        Log.d(TAG, "🆔 shopId: $shopId")

        if (shopId.isBlank()) {
            Log.e(TAG, "❌ shopId is blank")
            _shopDetailsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "⏳ Setting up listener for shop details...")
            shopRepository.listenToShopDetails(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "📢 Shop update: ${result.data.shopName}")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                    else -> {}
                }
                _shopDetailsState.value = result
            }
        }
    }

    // ============= SHOP RATING FUNCTIONS =============

    fun getShopRating(shopId: String) {
        Log.d(TAG, "getShopRating - shopId: $shopId")
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

    // ============= SEARCH FUNCTIONS =============

    fun searchShops(query: String, shopType: ShopType? = null) {
        Log.d(TAG, "========== searchShops ==========")
        Log.d(TAG, "🔍 query: '$query', type: $shopType")

        if (query.isBlank()) {
            Log.e(TAG, "❌ Search query is blank")
            _searchResultsState.value = Resource.Error("Search query cannot be empty")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "⏳ Searching...")
            _searchResultsState.value = Resource.Loading
            try {
                shopRepository.searchShops(query, shopType).collect { result ->
                    when (result) {
                        is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} shops")
                        is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                        is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                        else -> {}
                    }
                    _searchResultsState.value = result
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                _searchResultsState.value = Resource.Error(e.message ?: "Search failed")
            }
        }
    }

    fun searchProductShops(query: String) = searchShops(query, ShopType.PRODUCT)
    fun searchServiceShops(query: String) = searchShops(query, ShopType.SERVICE)

    /**
     * Search services by name, description or category
     */
    fun searchServices(query: String) {
        Log.d(TAG, "========== searchServices ==========")
        Log.d(TAG, "🔍 Searching services with query: '$query'")

        if (query.isBlank()) {
            Log.e(TAG, "❌ Search query is blank")
            _searchServicesState.value = Resource.Success(emptyList())
            return
        }


        viewModelScope.launch {
            Log.d(TAG, "⏳ Searching services...")
            _searchServicesState.value = Resource.Loading
            try {
                shopItemRepository.searchServices(query, ShopType.SERVICE).collect { result ->
                    when (result) {
                        is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} services")
                        is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                        is Resource.Loading -> Log.d(TAG, "⏳ Loading...")
                        else -> {}
                    }
                    _searchServicesState.value = result
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                _searchServicesState.value = Resource.Error(e.message ?: "Search failed")
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearSearchResults() {
        _searchServicesState.value = Resource.Idle
        _searchResultsState.value = Resource.Idle
    }

    // ============= CATEGORIES FUNCTIONS =============

    fun getShopCategories() {
        Log.d(TAG, "getShopCategories called")
        viewModelScope.launch {
            _shopCategoriesState.value = Resource.Loading
            try {
                shopRepository.getAllCategoriesWithType().collect { result ->
                    _shopCategoriesState.value = result
                }
            } catch (e: Exception) {
                _shopCategoriesState.value = Resource.Error(e.message ?: "Failed to get categories")
            }
        }
    }

    fun getProductCategories() {
        Log.d(TAG, "getProductCategories called")
        viewModelScope.launch {
            _productCategoriesState.value = Resource.Loading
            try {
                shopRepository.getProductCategoriesWithType().collect { result ->
                    _productCategoriesState.value = result
                }
            } catch (e: Exception) {
                _productCategoriesState.value = Resource.Error(e.message ?: "Failed to get product categories")
            }
        }
    }

    fun getServiceCategories() {
        Log.d(TAG, "getServiceCategories called")
        viewModelScope.launch {
            _serviceCategoriesState.value = Resource.Loading
            try {
                shopRepository.getServiceCategoriesWithType().collect { result ->
                    _serviceCategoriesState.value = result
                }
            } catch (e: Exception) {
                _serviceCategoriesState.value = Resource.Error(e.message ?: "Failed to get service categories")
            }
        }
    }

    fun getShopsByCategory(category: String, shopType: ShopType? = null) {
        Log.d(TAG, "getShopsByCategory - category: $category, type: $shopType")
        if (category.isBlank()) {
            _searchResultsState.value = Resource.Error("Category cannot be empty")
            return
        }

        viewModelScope.launch {
            _searchResultsState.value = Resource.Loading
            try {
                shopRepository.getShopsByCategory(category, shopType).collect { result ->
                    _searchResultsState.value = result
                }
            } catch (e: Exception) {
                _searchResultsState.value = Resource.Error(e.message ?: "Failed to get shops")
            }
        }
    }

    // ============= FAVORITES FUNCTIONS =============

    fun addToFavorites(shop: Shop) {
        Log.d(TAG, "addToFavorites - shopId: ${shop.shopId}, name: ${shop.shopName}")
        if (shop.shopId.isBlank()) return
        viewModelScope.launch {
            try {
                shopRepository.addToFavorites(shop).collect { result ->
                    if (result is Resource.Success) {
                        Log.d(TAG, "✅ Added to favorites")
                        getFavoriteShops()
                        when (shop.shopType) {
                            ShopType.PRODUCT -> getFavoriteProductShops()
                            ShopType.SERVICE -> getFavoriteServiceShops()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error adding to favorites", e)
            }
        }
    }

    fun removeFromFavorites(shop: Shop) {
        Log.d(TAG, "removeFromFavorites - shopId: ${shop.shopId}")
        if (shop.shopId.isBlank()) return
        viewModelScope.launch {
            try {
                shopRepository.removeFromFavorites(shop.shopId).collect { result ->
                    if (result is Resource.Success) {
                        Log.d(TAG, "✅ Removed from favorites")
                        getFavoriteShops()
                        when (shop.shopType) {
                            ShopType.PRODUCT -> getFavoriteProductShops()
                            ShopType.SERVICE -> getFavoriteServiceShops()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error removing from favorites", e)
            }
        }
    }

    fun getFavoriteShops() {
        Log.d(TAG, "getFavoriteShops called")
        viewModelScope.launch {
            _favoriteShopsState.value = Resource.Loading
            shopRepository.getFavoriteShops().collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} favorite shops")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    else -> {}
                }
                _favoriteShopsState.value = result
            }
        }
    }

    fun getFavoriteProductShops() {
        Log.d(TAG, "getFavoriteProductShops called")
        viewModelScope.launch {
            _favoriteProductShopsState.value = Resource.Loading
            shopRepository.getFavoriteShopsByType(ShopType.PRODUCT).collect { result ->
                _favoriteProductShopsState.value = result
            }
        }
    }

    fun getFavoriteServiceShops() {
        Log.d(TAG, "getFavoriteServiceShops called")
        viewModelScope.launch {
            _favoriteServiceShopsState.value = Resource.Loading
            shopRepository.getFavoriteShopsByType(ShopType.SERVICE).collect { result ->
                _favoriteServiceShopsState.value = result
            }
        }
    }

    fun isFavorite(shopId: String): Boolean {
        val favorites = _favoriteShopsState.value
        return if (favorites is Resource.Success) {
            favorites.data.any { it.shopId == shopId }
        } else false
    }

    // ============= FILTER MANAGEMENT =============

    fun setSelectedShopType(shopType: ShopType?) {
        Log.d(TAG, "setSelectedShopType - $shopType")
        _selectedShopType.value = shopType
    }

    fun clearShopTypeFilter() {
        Log.d(TAG, "clearShopTypeFilter called")
        _selectedShopType.value = null
    }

    // ============= BOOKING FUNCTIONS =============

    fun createBooking(
        customerId: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        shopId: String,
        shopName: String,
        shopOwnerId: String,
        serviceId: String,
        serviceName: String,
        servicePrice: Double,
        serviceDuration: String,
        bookingDate: String,
        bookingTime: String,
        serviceLocation: ServiceLocation,
        serviceAddress: String = "",
        customerCity: String = "",
        customerPincode: String = "",
        notes: String = ""
    ) {
        Log.d(TAG, "========== createBooking ==========")
        Log.d(TAG, "📅 Date: $bookingDate, Time: $bookingTime")
        Log.d(TAG, "🔧 Service: $serviceName, Price: $servicePrice")

        viewModelScope.launch {
            _createBookingState.value = Resource.Loading

            val booking = Booking(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                shopId = shopId,
                shopName = shopName,
                shopOwnerId = shopOwnerId,
                serviceId = serviceId,
                serviceName = serviceName,
                servicePrice = servicePrice,
                serviceDuration = serviceDuration,
                bookingDate = bookingDate,
                bookingTime = bookingTime,
                serviceLocation = serviceLocation,
                serviceAddress = serviceAddress,
                customerCity = customerCity,
                customerPincode = customerPincode,
                notes = notes,
                status = BookingStatus.PENDING,
                paymentStatus = PaymentStatus.PENDING
            )

            shopRepository.createBooking(booking).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Booking created: ${result.data.bookingId}")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    is Resource.Loading -> Log.d(TAG, "⏳ Creating booking...")
                    else -> {}
                }
                _createBookingState.value = result
                if (result is Resource.Success) {
                    getMyBookings(customerId)
                }
            }
        }
    }

    fun getMyBookings(customerId: String) {
        Log.d(TAG, "getMyBookings - customerId: $customerId")
        if (customerId.isBlank()) {
            _myBookingsState.value = Resource.Error("Customer ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _myBookingsState.value = Resource.Loading
            shopRepository.getBookingsByCustomer(customerId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} bookings")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    else -> {}
                }
                _myBookingsState.value = result
            }
        }
    }

    fun getBookingDetails(bookingId: String) {
        Log.d(TAG, "getBookingDetails - bookingId: $bookingId")
        if (bookingId.isBlank()) {
            _bookingDetailsState.value = Resource.Error("Booking ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _bookingDetailsState.value = Resource.Loading
            shopRepository.getBookingById(bookingId).collect { result ->
                _bookingDetailsState.value = result
            }
        }
    }

    fun getAvailableTimeSlots(shopId: String, date: String) {
        Log.d(TAG, "getAvailableTimeSlots - shopId: $shopId, date: $date")
        if (shopId.isBlank()) {
            _availableTimeSlotsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        if (date.isBlank()) {
            _availableTimeSlotsState.value = Resource.Error("Date cannot be empty")
            return
        }

        viewModelScope.launch {
            _availableTimeSlotsState.value = Resource.Loading
            shopRepository.getAvailableTimeSlots(shopId, date).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Found ${result.data.size} available slots")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    else -> {}
                }
                _availableTimeSlotsState.value = result
            }
        }
    }

    fun cancelBooking(bookingId: String, reason: String, customerId: String) {
        Log.d(TAG, "cancelBooking - bookingId: $bookingId, reason: $reason")
        if (bookingId.isBlank()) {
            _cancelBookingState.value = Resource.Error("Booking ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _cancelBookingState.value = Resource.Loading
            shopRepository.cancelBooking(bookingId, reason, "customer").collect { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Booking cancelled successfully")
                    is Resource.Error -> Log.e(TAG, "❌ Error: ${result.message}")
                    else -> {}
                }
                _cancelBookingState.value = result
                if (result is Resource.Success) {
                    getMyBookings(customerId)
                }
            }
        }
    }

    fun listenToBookingUpdates(bookingId: String): Flow<Resource<Booking>> = callbackFlow {
        Log.d(TAG, "listenToBookingUpdates - bookingId: $bookingId")
        trySend(Resource.Loading)

        val job = launch {
            shopRepository.listenToCustomerBookings(bookingId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val booking = result.data.firstOrNull()
                        if (booking != null) {
                            trySend(Resource.Success(booking))
                        } else {
                            trySend(Resource.Error("Booking not found"))
                        }
                    }
                    is Resource.Error -> trySend(Resource.Error(result.message))
                    is Resource.Loading -> trySend(Resource.Loading)
                    else -> {}
                }
            }
        }

        awaitClose {
            Log.d(TAG, "listenToBookingUpdates closed for bookingId: $bookingId")
            job.cancel()
        }
    }

    // ============= HELPER FUNCTIONS =============

    fun isShopOpen(shop: Shop?): Boolean {
        return shop != null && shop.open && shop.active
    }

    fun getShopStatusMessage(shop: Shop?): String {
        if (shop == null) return "Shop not found"
        return when {
            !shop.active -> "Permanently Closed"
            !shop.open -> "Currently Closed"
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
        return shop != null && shop.open && shop.active && shop.totalItems > 0
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

    fun hasLogo(shop: Shop?): Boolean = shop?.hasLogo == true && shop.logo.isNotBlank()
    fun hasCover(shop: Shop?): Boolean = shop?.hasCover == true && shop.coverImage.isNotBlank()
    fun getLogo(shop: Shop?): String = shop?.logo ?: ""
    fun getCover(shop: Shop?): String = shop?.coverImage ?: ""

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

    // ============= STATE RESET FUNCTIONS =============

    fun resetNearbyShops() {
        Log.d(TAG, "resetNearbyShops called")
        _nearbyShopsState.value = Resource.Idle
        _nearbyProductShopsState.value = Resource.Idle
        _nearbyServiceShopsState.value = Resource.Idle
        _nearbyServiceItemsState.value = Resource.Idle
    }

    fun resetSearchResults() {
        Log.d(TAG, "resetSearchResults called")
        _searchResultsState.value = Resource.Idle
        _searchServicesState.value = Resource.Idle
    }

    fun resetShopDetails() {
        Log.d(TAG, "resetShopDetails called")
        _shopDetailsState.value = Resource.Idle
        _serviceItemDetailsState.value = Resource.Idle
    }

    fun resetFavorites() {
        Log.d(TAG, "resetFavorites called")
        _favoriteShopsState.value = Resource.Idle
        _favoriteProductShopsState.value = Resource.Idle
        _favoriteServiceShopsState.value = Resource.Idle
    }

    fun resetCategories() {
        Log.d(TAG, "resetCategories called")
        _shopCategoriesState.value = Resource.Idle
        _productCategoriesState.value = Resource.Idle
        _serviceCategoriesState.value = Resource.Idle
    }

    fun resetShopRating() {
        Log.d(TAG, "resetShopRating called")
        _shopRatingState.value = Resource.Idle
    }

    fun resetBookingStates() {
        Log.d(TAG, "resetBookingStates called")
        _createBookingState.value = Resource.Idle
        _myBookingsState.value = Resource.Idle
        _bookingDetailsState.value = Resource.Idle
        _availableTimeSlotsState.value = Resource.Idle
        _cancelBookingState.value = Resource.Idle
    }

    fun resetAll() {
        Log.d(TAG, "resetAll called - resetting all states")
        resetNearbyShops()
        resetSearchResults()
        resetFavorites()
        resetCategories()
        resetShopDetails()
        resetShopRating()
        resetBookingStates()
        clearShopTypeFilter()
    }

    // ============= CLEANUP =============

    override fun onCleared() {
        Log.d(TAG, "========== ViewModel onCleared ==========")
        super.onCleared()
        resetAll()
    }
}