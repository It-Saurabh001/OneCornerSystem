package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.OperatingHour
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

/**
 * ViewModel for managing shop profile
 * Handles shop creation, management, and real-time updates
 */
@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

    // Shop Details
    private val _shopDetailsState = MutableStateFlow<Resource<Shop>>(Resource.Idle)
    val shopDetailsState: StateFlow<Resource<Shop>> = _shopDetailsState.asStateFlow()

    // My Shop (by owner)
    private val _myShopState = MutableStateFlow<Resource<Shop>>(Resource.Idle)
    val myShopState: StateFlow<Resource<Shop>> = _myShopState.asStateFlow()

    // Update Operations
    private val _updateShopState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val updateShopState: StateFlow<Resource<Boolean>> = _updateShopState.asStateFlow()

    // Create Shop
    private val _createShopState = MutableStateFlow<Resource<Shop>>(Resource.Idle)
    val createShopState: StateFlow<Resource<Shop>> = _createShopState.asStateFlow()

    // Deactivate Shop
    private val _deactivateShopState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val deactivateShopState: StateFlow<Resource<Boolean>> = _deactivateShopState.asStateFlow()

    // Toggle Status
    private val _toggleStatusState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val toggleStatusState: StateFlow<Resource<Boolean>> = _toggleStatusState.asStateFlow()

    // Update Stats
    private val _updateStatsState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val updateStatsState: StateFlow<Resource<Boolean>> = _updateStatsState.asStateFlow()

    // Shop Rating
    private val _shopRatingState = MutableStateFlow<Resource<Double>>(Resource.Idle)
    val shopRatingState: StateFlow<Resource<Double>> = _shopRatingState.asStateFlow()

    // Image Upload
    private val _coverUploadState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val coverUploadState: StateFlow<Resource<String>> = _coverUploadState.asStateFlow()

    // Image Remove
    private val _coverRemoveState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val coverRemoveState: StateFlow<Resource<Boolean>> = _coverRemoveState.asStateFlow()

    // Logo Upload
    private val _logoUploadState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val logoUploadState: StateFlow<Resource<String>> = _logoUploadState.asStateFlow()

    // Logo Remove
    private val _logoRemoveState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val logoRemoveState: StateFlow<Resource<Boolean>> = _logoRemoveState.asStateFlow()

    // Combined Loading State for UI
    val isLoading: StateFlow<Boolean> = combine(
        _shopDetailsState,
        _myShopState,
        _updateShopState,
        _createShopState,
        _deactivateShopState,
        _toggleStatusState,
        _updateStatsState,
        _logoUploadState,
        _logoRemoveState,
        _coverUploadState,
        _coverRemoveState,
        _shopRatingState
    ) { states ->
        states.any { it is Resource.Loading }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // ============= SHOP CREATION =============

    fun createShop(
        ownerId: String,
        shopName: String,
        category: String,
        description: String,
        address: String,
        city: String,
        pincode: String,
        contactNumber: String,
        email: String,
        latitude: Double,
        longitude: Double,
        openingTime: String,
        closingTime: String,
        operatingHours: Map<String, OperatingHour> = emptyMap()
    ) {
        Log.d("ShopViewModel_Create", "createShop called - name: $shopName, category: $category, ownerId: $ownerId")
        viewModelScope.launch {
            // Validate inputs
            if (shopName.isBlank() || category.isBlank() || contactNumber.isBlank()) {
                Log.d("ShopViewModel_Create", "Validation failed - Required fields empty")
                _createShopState.value = Resource.Error("Required fields cannot be empty")
                return@launch
            }

            Log.d("ShopViewModel_Create", "Creating shop - address: $address, city: $city, hours: $openingTime-$closingTime")
            val shop = Shop(
                ownerId = ownerId,
                shopName = shopName,
                category = category,
                description = description,
                location = com.google.firebase.firestore.GeoPoint(latitude, longitude),
                address = address,
                city = city,
                pincode = pincode,
                contactNumber = contactNumber,
                email = email,
                openingTime = openingTime,
                closingTime = closingTime,
                operatingHours = operatingHours,
                open = true,
                active = true,
                rating = 0.0,
                totalRatings = 0,
                totalItems = 0,
                totalOrders = 0,
                totalRevenue = 0.0,
                averageOrderValue = 0.0,
                logo = "",
                coverImage = "",
                hasLogo = false,
                hasCover = false
            )

            shopRepository.createShop(shop).collect { result ->
                when (result) {
                    is Resource.Loading -> Log.d("ShopViewModel_Create", "Creating shop...")
                    is Resource.Success -> {
                        Log.d("ShopViewModel_Create", "Shop created successfully - shopId: ${result.data.shopId}")
                        _createShopState.value = result
                        getShopDetails(result.data.shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_Create", "Shop creation failed - ${result.message}")
                        _createShopState.value = result
                    }
                    else -> _createShopState.value = result
                }
            }
        }
    }

    // ============= LOAD SHOP DETAILS =============

    fun getShopDetails(shopId: String) {
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Details", "getShopDetails - shopId is blank, returning error")
            _shopDetailsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        Log.d("ShopViewModel_Details", "getShopDetails - fetching shopId: $shopId, $_shopDetailsState")
        viewModelScope.launch {
            shopRepository.getShopDetails(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Details", "getShopDetails - Success: open: ${result.data.open} ,active: ${result.data.active}")
                    is Resource.Error -> Log.d("ShopViewModel_Details", "getShopDetails - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_Details", "getShopDetails - Loading...")
                    else -> {}
                }
                _shopDetailsState.value = result
            }
        }
    }

    fun listenToShopDetails(shopId: String) {
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Details", "listenToShopDetails - shopId is blank, returning error")
            _shopDetailsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        Log.d("ShopViewModel_Details", "listenToShopDetails - setting up listener for shopId: $shopId")
        viewModelScope.launch {
            shopRepository.listenToShopDetails(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Details", "listenToShopDetails - Success: ${result.data.shopName}")
                    is Resource.Error -> Log.d("ShopViewModel_Details", "listenToShopDetails - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_Details", "listenToShopDetails - Loading...")
                    else -> {}
                }
                _shopDetailsState.value = result
            }
        }
    }

    fun getMyShop(ownerId: String) {
        if (ownerId.isBlank()) {
            Log.d("ShopViewModel_MyShop", "getMyShop - ownerId is blank, returning error")
            _myShopState.value = Resource.Error("Owner ID cannot be empty")
            return
        }

        Log.d("ShopViewModel_MyShop", "getMyShop - fetching shop for ownerId: $ownerId")
        viewModelScope.launch {
            shopRepository.getShopByOwner(ownerId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ShopViewModel_MyShop", "getMyShop - Success: ${result.data.shopName}")
                        _myShopState.value = result
//                        getShopDetails(result.data.shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_MyShop", "getMyShop - Error: ${result.message}")
                        _myShopState.value = result
                    }
                    is Resource.Loading -> {
                        Log.d("ShopViewModel_MyShop", "getMyShop - Loading...")
                        _myShopState.value = result
                    }
                    else -> _myShopState.value = result
                }
            }
        }
    }

    fun listenToMyShop(ownerId: String) {
        if (ownerId.isBlank()) {
            Log.d("ShopViewModel_MyShop", "listenToMyShop - ownerId is blank, returning error")
            _myShopState.value = Resource.Error("Owner ID cannot be empty")
            return
        }

        Log.d("ShopViewModel_MyShop", "listenToMyShop - setting up listener for ownerId: $ownerId")
        viewModelScope.launch {
            shopRepository.getShopByOwner(ownerId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _myShopState.value = result
                    }
                    is Resource.Error -> {
                        _myShopState.value = result
                    }
                    is Resource.Loading -> {
                        _myShopState.value = result
                    }

                    else -> {
                        Log.d("TAG", "listenToMyShop: else section")}
                }
            }
        }
    }

    fun getShopById(shopId: String) {
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_ById", "getShopById - shopId is blank, returning error")
            _myShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        Log.d("ShopViewModel_ById", "getShopById - fetching shopId: $shopId")
        viewModelScope.launch {
            shopRepository.getShopDetails(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_ById", "getShopById - Success: ${result.data.shopName}")
                    is Resource.Error -> Log.d("ShopViewModel_ById", "getShopById - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_ById", "getShopById - Loading...")
                    else -> {}
                }
                _myShopState.value = result
            }
        }
    }


    fun getShopRating(shopId: String) {
        Log.d("ShopViewModel_Rating", "getShopRating - shopId: $shopId")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Rating", "getShopRating - shopId is blank, returning error")
            _shopRatingState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.getShopRating(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Rating", "getShopRating - Success: ${result.data}")
                    is Resource.Error -> Log.d("ShopViewModel_Rating", "getShopRating - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_Rating", "getShopRating - Loading...")
                    else -> {}
                }
                _shopRatingState.value = result
            }
        }
    }

    // ============= SHOP UPDATES =============

    fun updateShopInfo(shopId: String, shopName: String, description: String, category: String) {
        Log.d("ShopViewModel_Update", "updateShopInfo - shopId: $shopId, name: $shopName, category: $category")
        val updates = mapOf(
            "shopName" to shopName,
            "description" to description,
            "category" to category
        )
        performUpdate(shopId, updates)
    }

    fun updateContactDetails(shopId: String, contactNumber: String, email: String) {
        Log.d("ShopViewModel_Update", "updateContactDetails - shopId: $shopId, contact: $contactNumber, email: $email")
        val updates = mapOf(
            "contactNumber" to contactNumber,
            "email" to email
        )
        performUpdate(shopId, updates)
    }

    fun updateShopAddress(shopId: String, address: String, city: String, pincode: String) {
        Log.d("ShopViewModel_Update", "updateShopAddress - shopId: $shopId, city: $city, pincode: $pincode")
        val updates = mapOf(
            "address" to address,
            "city" to city,
            "pincode" to pincode
        )
        performUpdate(shopId, updates)
    }

    fun updateOperatingHours(shopId: String, openingTime: String, closingTime: String) {
        Log.d("ShopViewModel_Update", "updateOperatingHours - shopId: $shopId, hours: $openingTime-$closingTime")
        val updates = mapOf(
            "openingTime" to openingTime,
            "closingTime" to closingTime
        )
        performUpdate(shopId, updates)
    }

    fun updateOperatingHoursMap(shopId: String, operatingHours: Map<String, OperatingHour>) {
        Log.d("ShopViewModel_Update", "updateOperatingHoursMap - shopId: $shopId, days: ${operatingHours.size}")
        performUpdate(shopId, mapOf("operatingHours" to operatingHours))
    }

    fun updateAverageOrderValue(shopId: String, averageOrderValue: Double) {
        Log.d("ShopViewModel_Update", "updateAverageOrderValue - shopId: $shopId, value: $averageOrderValue")
        performUpdate(shopId, mapOf("averageOrderValue" to averageOrderValue))
    }


    fun updateShopName(shopId: String, newName: String) {
        Log.d("ShopViewModel_Update", "updateShopName - shopId: $shopId, newName: $newName")
        performUpdate(shopId, mapOf("shopName" to newName))
    }

    fun updateShopDescription(shopId: String, newDescription: String) {
        Log.d("ShopViewModel_Update", "updateShopDescription - shopId: $shopId")
        performUpdate(shopId, mapOf("description" to newDescription))
    }

    fun updateShopContactNumber(shopId: String, newNumber: String) {
        Log.d("ShopViewModel_Update", "updateShopContactNumber - shopId: $shopId, number: $newNumber")
        performUpdate(shopId, mapOf("contactNumber" to newNumber))
    }

    fun updateShopAddress(shopId: String, newAddress: String) {
        Log.d("ShopViewModel_Update", "updateShopAddress - shopId: $shopId")
        performUpdate(shopId, mapOf("address" to newAddress))
    }

    private fun performUpdate(shopId: String, updates: Map<String, Any>) {
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Update", "performUpdate - shopId is blank, returning error")
            _updateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        Log.d("ShopViewModel_Update", "performUpdate - shopId: $shopId, fields: ${updates.keys}")
        viewModelScope.launch {
            _updateShopState.value = Resource.Loading
            shopRepository.updateShopProfile(shopId, updates).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ShopViewModel_Update", "performUpdate - Success")
                        _updateShopState.value = result
                        getShopDetails(shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_Update", "performUpdate - Error: ${result.message}")
                        _updateShopState.value = result
                    }
                    is Resource.Loading -> Log.d("ShopViewModel_Update", "performUpdate - Loading...")
                    else -> _updateShopState.value = result
                }
            }
        }
    }

    // ============= SHOP STATUS MANAGEMENT =============

    fun toggleShopOpenStatus(shopId: String, isOpen: Boolean) {
        Log.d("ShopViewModel_Status", "toggleShopOpenStatus - shopId: $shopId, isOpen: $isOpen")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Status", "toggleShopOpenStatus - shopId is blank, returning error")
            _toggleStatusState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _toggleStatusState.value = Resource.Loading
            shopRepository.updateShopActiveStatus(shopId, isOpen).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Status", "toggleShopOpenStatus - Success")
                    is Resource.Error -> Log.d("ShopViewModel_Status", "toggleShopOpenStatus - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_Status", "toggleShopOpenStatus - Loading...")
                    else -> {}
                }
                _toggleStatusState.value = result
            }
        }
    }

    fun toggleShopActiveStatus(shopId: String, isActive: Boolean) {
        Log.d("ShopViewModel_Status", "toggleShopActiveStatus - shopId: $shopId, isActive: $isActive")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Status", "toggleShopActiveStatus - shopId is blank, returning error")
            _deactivateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _deactivateShopState.value = Resource.Loading
            shopRepository.updateShopActiveStatus(shopId, isActive).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Status", "toggleShopActiveStatus - Success")
                    is Resource.Error -> Log.d("ShopViewModel_Status", "toggleShopActiveStatus - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_Status", "toggleShopActiveStatus - Loading...")
                    else -> {}
                }
                _deactivateShopState.value = result
            }
        }
    }

    // ============= SHOP STATISTICS =============

    /**
     * Update shop location - called after login when location permission is granted
     */
    fun updateShopLocation(shopId: String, latitude: Double, longitude: Double) {
        Log.d("ShopViewModel_Location", "updateShopLocation - shopId: $shopId, lat: $latitude, lng: $longitude")
        if (shopId.isBlank() || (latitude == 0.0 && longitude == 0.0)) {
            Log.d("ShopViewModel_Location", "updateShopLocation - invalid params, skipping")
            return
        }

        viewModelScope.launch {
            val updates = mapOf(
                "location" to com.google.firebase.firestore.GeoPoint(latitude, longitude)
            )
            shopRepository.updateShopProfile(shopId, updates).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Location", "Shop location updated successfully")
                    is Resource.Error -> Log.d("ShopViewModel_Location", "Failed to update location: ${result.message}")
                    else -> {}
                }
            }
        }
    }

    fun updateShopStatistics(
        shopId: String,
        totalItems: Int,
        totalOrders: Int,
        totalRevenue: Double
    ) {
        Log.d("ShopViewModel_Stats", "updateShopStatistics - shopId: $shopId, items: $totalItems, orders: $totalOrders, revenue: $totalRevenue")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Stats", "updateShopStatistics - shopId is blank, returning error")
            _updateStatsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _updateStatsState.value = Resource.Loading
            shopRepository.updateShopStats(shopId, totalItems, totalOrders, totalRevenue)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> Log.d("ShopViewModel_Stats", "updateShopStatistics - Success")
                        is Resource.Error -> Log.d("ShopViewModel_Stats", "updateShopStatistics - Error: ${result.message}")
                        is Resource.Loading -> Log.d("ShopViewModel_Stats", "updateShopStatistics - Loading...")
                        else -> {}
                    }
                    _updateStatsState.value = result
                }
        }
    }

    fun incrementItemCount(shopId: String, currentCount: Int) {
        Log.d("ShopViewModel_Stats", "incrementItemCount - shopId: $shopId, currentCount: $currentCount")
        updateShopStatistics(shopId, currentCount + 1, 0, 0.0)
    }

    fun addOrderRevenue(shopId: String, currentOrders: Int, currentRevenue: Double, orderAmount: Double) {
        Log.d("ShopViewModel_Stats", "addOrderRevenue - shopId: $shopId, currentOrders: $currentOrders, orderAmount: $orderAmount")
        updateShopStatistics(
            shopId,
            0,
            currentOrders + 1,
            currentRevenue + orderAmount
        )
    }

    // ============= SHOP DELETION =============

    fun deactivateShop(shopId: String) {
        Log.d("ShopViewModel_Delete", "deactivateShop - shopId: $shopId")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Delete", "deactivateShop - shopId is blank, returning error")
            _deactivateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _deactivateShopState.value = Resource.Loading
            shopRepository.deleteShop(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> Log.d("ShopViewModel_Delete", "deactivateShop - Success")
                    is Resource.Error -> Log.d("ShopViewModel_Delete", "deactivateShop - Error: ${result.message}")
                    is Resource.Loading -> Log.d("ShopViewModel_Delete", "deactivateShop - Loading...")
                    else -> {}
                }
                _deactivateShopState.value = result
            }
        }
    }

    // ============= LOGO IMAGE MANAGEMENT =============

    fun uploadLogo(shopId: String, imageUri: Uri) {
        Log.d("ShopViewModel_Logo", "uploadLogo - shopId: $shopId, imageUri: $imageUri")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Logo", "uploadLogo - shopId is blank, returning error")
            _logoUploadState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _logoUploadState.value = Resource.Loading
            shopRepository.uploadShopLogo(shopId, imageUri).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ShopViewModel_Logo", "uploadLogo - Success: ${result.data}")
                        _logoUploadState.value = result
                        getShopDetails(shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_Logo", "uploadLogo - Error: ${result.message}")
                        _logoUploadState.value = result
                    }
                    is Resource.Loading -> Log.d("ShopViewModel_Logo", "uploadLogo - Loading...")
                    else -> _logoUploadState.value = result
                }
            }
        }
    }

    fun removeLogo(shopId: String) {
        Log.d("ShopViewModel_Logo", "removeLogo - shopId: $shopId")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Logo", "removeLogo - shopId is blank, returning error")
            _logoRemoveState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _logoRemoveState.value = Resource.Loading
            shopRepository.removeShopLogo(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ShopViewModel_Logo", "removeLogo - Success")
                        _logoRemoveState.value = result
                        getShopDetails(shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_Logo", "removeLogo - Error: ${result.message}")
                        _logoRemoveState.value = result
                    }
                    is Resource.Loading -> Log.d("ShopViewModel_Logo", "removeLogo - Loading...")
                    else -> _logoRemoveState.value = result
                }
            }
        }
    }

    fun updateLogo(shopId: String, newImageUri: Uri) {
        Log.d("ShopViewModel_Logo", "updateLogo - shopId: $shopId")
        viewModelScope.launch {
            removeLogo(shopId)
            // Wait a bit then upload new one
            _logoRemoveState.collect { removeResult ->
                if (removeResult is Resource.Success) {
                    Log.d("ShopViewModel_Logo", "updateLogo - Old logo removed, uploading new logo")
                    uploadLogo(shopId, newImageUri)
                    return@collect
                }
            }
        }
    }

    // ============= COVER IMAGE MANAGEMENT =============

    fun uploadCover(shopId: String, imageUri: Uri) {
        Log.d("ShopViewModel_Cover", "uploadCover - shopId: $shopId, imageUri: $imageUri")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Cover", "uploadCover - shopId is blank, returning error")
            _coverUploadState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _coverUploadState.value = Resource.Loading
            shopRepository.uploadShopCover(shopId, imageUri).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ShopViewModel_Cover", "uploadCover - Success: ${result.data}")
                        _coverUploadState.value = result
                        getShopDetails(shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_Cover", "uploadCover - Error: ${result.message}")
                        _coverUploadState.value = result
                    }
                    is Resource.Loading -> Log.d("ShopViewModel_Cover", "uploadCover - Loading...")
                    else -> _coverUploadState.value = result
                }
            }
        }
    }

    fun removeCover(shopId: String) {
        Log.d("ShopViewModel_Cover", "removeCover - shopId: $shopId")
        if (shopId.isBlank()) {
            Log.d("ShopViewModel_Cover", "removeCover - shopId is blank, returning error")
            _coverRemoveState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _coverRemoveState.value = Resource.Loading
            shopRepository.removeShopCover(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ShopViewModel_Cover", "removeCover - Success")
                        _coverRemoveState.value = result
                        getShopDetails(shopId)
                    }
                    is Resource.Error -> {
                        Log.d("ShopViewModel_Cover", "removeCover - Error: ${result.message}")
                        _coverRemoveState.value = result
                    }
                    is Resource.Loading -> Log.d("ShopViewModel_Cover", "removeCover - Loading...")
                    else -> _coverRemoveState.value = result
                }
            }
        }
    }

    fun updateCover(shopId: String, newImageUri: Uri) {
        Log.d("ShopViewModel_Cover", "updateCover - shopId: $shopId")
        viewModelScope.launch {
            removeCover(shopId)
            _coverRemoveState.collect { removeResult ->
                if (removeResult is Resource.Success) {
                    Log.d("ShopViewModel_Cover", "updateCover - Old cover removed, uploading new cover")
                    uploadCover(shopId, newImageUri)
                    return@collect
                }
            }
        }
    }

    // ============= HELPER FUNCTIONS (MOVED FROM COMMON) =============

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
                shop.open &&
                shop.active &&
                shop.totalItems > 0
    }

    fun getCategoryDisplay(category: String): String {
        return when (category.lowercase()) {
            "restaurant" -> "Restaurant"
            "grocery" -> "Grocery Store"
            "medical" -> "Medical Store"
            "bakery" -> "Bakery"
            "electronics" -> "Electroics"
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

    fun resetCreateShopState() {
        Log.d("ShopViewModel_Reset", "resetCreateShopState called")
        _createShopState.value = Resource.Loading
    }

    fun resetUpdateShopState() {
        Log.d("ShopViewModel_Reset", "resetUpdateShopState called")
        _updateShopState.value = Resource.Loading
    }

    fun resetDeactivateShopState() {
        Log.d("ShopViewModel_Reset", "resetDeactivateShopState called")
        _deactivateShopState.value = Resource.Loading
    }

    fun resetShopDetails() {
        Log.d("ShopViewModel_Reset", "resetShopDetails called")
        _shopDetailsState.value = Resource.Loading
    }

    fun resetShopRating() {
        Log.d("ShopViewModel_Reset", "resetShopRating called")
        _shopRatingState.value = Resource.Loading
    }

    fun resetAllStates() {
        Log.d("ShopViewModel_Reset", "resetAllStates called - resetting all state flows")
        _shopDetailsState.value = Resource.Loading
        _myShopState.value = Resource.Loading
        _updateShopState.value = Resource.Loading
        _createShopState.value = Resource.Loading
        _deactivateShopState.value = Resource.Loading
        _toggleStatusState.value = Resource.Loading
        _updateStatsState.value = Resource.Loading
        _shopRatingState.value = Resource.Loading
        _coverUploadState.value = Resource.Loading
        _logoUploadState.value = Resource.Loading
        _logoRemoveState.value = Resource.Loading
        _coverRemoveState.value = Resource.Loading
    }

    // ============= REFRESH FUNCTIONS =============

    fun refreshMyShop(ownerId: String) {
        Log.d("ShopViewModel_Refresh", "refreshMyShop called - ownerId: $ownerId")
        getMyShop(ownerId)
    }

    // ============= CLEANUP =============

    override fun onCleared() {
        Log.d("ShopViewModel_Cleanup", "onCleared called - cleaning up ViewModel")
        super.onCleared()
        resetAllStates()
    }
}