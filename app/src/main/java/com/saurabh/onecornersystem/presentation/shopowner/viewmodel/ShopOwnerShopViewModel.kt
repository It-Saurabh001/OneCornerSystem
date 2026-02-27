package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.OperatingHour
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.presentation.common.CommonShopViewModel
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
class ShopOwnerShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val commonShopViewModel: CommonShopViewModel
) : ViewModel() {

    // ============= STATES =============

    // My Shop (by owner)
    private val _myShopState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val myShopState: StateFlow<Resource<Shop>> = _myShopState.asStateFlow()

    // Create Shop
    private val _createShopState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val createShopState: StateFlow<Resource<Shop>> = _createShopState.asStateFlow()

    // Update Operations
    private val _updateShopState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val updateShopState: StateFlow<Resource<Boolean>> = _updateShopState.asStateFlow()

    // Deactivate Shop
    private val _deactivateShopState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val deactivateShopState: StateFlow<Resource<Boolean>> = _deactivateShopState.asStateFlow()

    // Toggle Status
    private val _toggleStatusState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val toggleStatusState: StateFlow<Resource<Boolean>> = _toggleStatusState.asStateFlow()

    // Update Stats
    private val _updateStatsState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val updateStatsState: StateFlow<Resource<Boolean>> = _updateStatsState.asStateFlow()

    // Logo Upload
    private val _logoUploadState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val logoUploadState: StateFlow<Resource<String>> = _logoUploadState.asStateFlow()

    // Logo Remove
    private val _logoRemoveState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val logoRemoveState: StateFlow<Resource<Boolean>> = _logoRemoveState.asStateFlow()

    // Cover Upload
    private val _coverUploadState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val coverUploadState: StateFlow<Resource<String>> = _coverUploadState.asStateFlow()

    // Cover Remove
    private val _coverRemoveState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val coverRemoveState: StateFlow<Resource<Boolean>> = _coverRemoveState.asStateFlow()

    // Combined Loading State
    val isLoading: StateFlow<Boolean> = combine(
        _myShopState,
        _createShopState,
        _updateShopState,
        _deactivateShopState,
        _toggleStatusState,
        _updateStatsState,
        _logoUploadState,
        _logoRemoveState,
        _coverUploadState,
        _coverRemoveState
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
        shopType: ShopType,
        operatingHours: Map<String, OperatingHour> = emptyMap(),
        logoUri: Uri? = null,
        coverUri: Uri? = null
    ) {
        viewModelScope.launch {
            // Validate inputs
            if (shopName.isBlank() || category.isBlank() || contactNumber.isBlank()) {
                _createShopState.value = Resource.Error("Required fields cannot be empty")
                return@launch
            }

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
                shopType = shopType,
                isOpen = true,
                isActive = true,
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

            if (logoUri != null || coverUri != null) {
                shopRepository.createShopWithImages(shop, logoUri, coverUri).collect { result ->
                    _createShopState.value = result
                    if (result is Resource.Success) {
                        commonShopViewModel.getShopDetails(result.data.shopId)
                    }
                }
            } else {
                shopRepository.createShop(shop).collect { result ->
                    _createShopState.value = result
                    if (result is Resource.Success) {
                        commonShopViewModel.getShopDetails(result.data.shopId)
                    }
                }
            }
        }
    }

    // ============= FETCH SHOP =============

    fun getMyShop(ownerId: String) {
        if (ownerId.isBlank()) {
            _myShopState.value = Resource.Error("Owner ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.getShopByOwner(ownerId).collect { result ->
                _myShopState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(result.data.shopId)
                }
            }
        }
    }

    fun listenToMyShop(ownerId: String) {
        if (ownerId.isBlank()) {
            _myShopState.value = Resource.Error("Owner ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.getShopByOwner(ownerId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        launch {
                            shopRepository.listenToShopDetails(result.data.shopId).collect { detailsResult ->
                                _myShopState.value = detailsResult
                            }
                        }
                    }
                    is Resource.Error -> {
                        _myShopState.value = Resource.Error(result.message)
                    }
                    is Resource.Loading -> {
                        _myShopState.value = Resource.Loading
                    }
                    else -> {}
                }
            }
        }
    }

    // ============= SHOP UPDATES =============

    fun updateShopInfo(shopId: String, shopName: String, description: String, category: String) {
        val updates = mapOf(
            "shopName" to shopName,
            "description" to description,
            "category" to category
        )
        performUpdate(shopId, updates)
    }

    fun updateContactDetails(shopId: String, contactNumber: String, email: String) {
        val updates = mapOf(
            "contactNumber" to contactNumber,
            "email" to email
        )
        performUpdate(shopId, updates)
    }

    fun updateShopAddress(shopId: String, address: String, city: String, pincode: String) {
        val updates = mapOf(
            "address" to address,
            "city" to city,
            "pincode" to pincode
        )
        performUpdate(shopId, updates)
    }

    fun updateOperatingHours(shopId: String, openingTime: String, closingTime: String) {
        val updates = mapOf(
            "openingTime" to openingTime,
            "closingTime" to closingTime
        )
        performUpdate(shopId, updates)
    }

    fun updateOperatingHoursMap(shopId: String, operatingHours: Map<String, OperatingHour>) {
        performUpdate(shopId, mapOf("operatingHours" to operatingHours))
    }

    fun updateAverageOrderValue(shopId: String, averageOrderValue: Double) {
        performUpdate(shopId, mapOf("averageOrderValue" to averageOrderValue))
    }

    fun updateShopLocation(shopId: String, latitude: Double, longitude: Double) {
        val updates = mapOf(
            "location" to com.google.firebase.firestore.GeoPoint(latitude, longitude)
        )
        performUpdate(shopId, updates)
    }

    fun updateShopType(shopId: String, shopType: ShopType) {
        performUpdate(shopId, mapOf("shopType" to shopType))
    }

    private fun performUpdate(shopId: String, updates: Map<String, Any>) {
        if (shopId.isBlank()) {
            _updateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _updateShopState.value = Resource.Loading
            shopRepository.updateShopProfile(shopId, updates).collect { result ->
                _updateShopState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    // ============= SHOP STATUS MANAGEMENT =============

    fun toggleShopOpenStatus(shopId: String, isOpen: Boolean) {
        if (shopId.isBlank()) {
            _toggleStatusState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _toggleStatusState.value = Resource.Loading
            shopRepository.updateShopActiveStatus(shopId, isOpen).collect { result ->
                _toggleStatusState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    fun toggleShopActiveStatus(shopId: String, isActive: Boolean) {
        if (shopId.isBlank()) {
            _deactivateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _deactivateShopState.value = Resource.Loading
            shopRepository.updateShopActiveStatus(shopId, isActive).collect { result ->
                _deactivateShopState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    // ============= SHOP STATISTICS =============

    fun updateShopStatistics(
        shopId: String,
        totalItems: Int,
        totalOrders: Int,
        totalRevenue: Double
    ) {
        if (shopId.isBlank()) {
            _updateStatsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _updateStatsState.value = Resource.Loading
            shopRepository.updateShopStats(shopId, totalItems, totalOrders, totalRevenue)
                .collect { result ->
                    _updateStatsState.value = result
                    if (result is Resource.Success) {
                        commonShopViewModel.getShopDetails(shopId)
                    }
                }
        }
    }

    fun incrementProductCount(shopId: String, currentCount: Int) {
        updateShopStatistics(shopId, currentCount + 1, 0, 0.0)
    }

    fun incrementServiceCount(shopId: String, currentCount: Int) {
        updateShopStatistics(shopId, currentCount + 1, 0, 0.0)
    }

    fun incrementItemCount(shopId: String, currentCount: Int) {
        updateShopStatistics(shopId, currentCount + 1, 0, 0.0)
    }

    fun addOrderRevenue(shopId: String, currentOrders: Int, currentRevenue: Double, orderAmount: Double) {
        updateShopStatistics(
            shopId,
            0,
            currentOrders + 1,
            currentRevenue + orderAmount
        )
    }

    // ============= SHOP DELETION =============

    fun deactivateShop(shopId: String) {
        if (shopId.isBlank()) {
            _deactivateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _deactivateShopState.value = Resource.Loading
            shopRepository.deleteShop(shopId).collect { result ->
                _deactivateShopState.value = result
                if (result is Resource.Success) {
                    _myShopState.value = Resource.Loading
                }
            }
        }
    }

    // ============= LOGO MANAGEMENT =============

    fun uploadLogo(shopId: String, imageUri: Uri) {
        if (shopId.isBlank()) {
            _logoUploadState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _logoUploadState.value = Resource.Loading
            shopRepository.uploadShopLogo(shopId, imageUri).collect { result ->
                _logoUploadState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    fun removeLogo(shopId: String) {
        if (shopId.isBlank()) {
            _logoRemoveState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _logoRemoveState.value = Resource.Loading
            shopRepository.removeShopLogo(shopId).collect { result ->
                _logoRemoveState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    fun updateLogo(shopId: String, newImageUri: Uri) {
        viewModelScope.launch {
            removeLogo(shopId)
            launch {
                _logoRemoveState.collect { removeResult ->
                    if (removeResult is Resource.Success) {
                        uploadLogo(shopId, newImageUri)
                    }
                }
            }
        }
    }

    // ============= COVER MANAGEMENT =============

    fun uploadCover(shopId: String, imageUri: Uri) {
        if (shopId.isBlank()) {
            _coverUploadState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _coverUploadState.value = Resource.Loading
            shopRepository.uploadShopCover(shopId, imageUri).collect { result ->
                _coverUploadState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    fun removeCover(shopId: String) {
        if (shopId.isBlank()) {
            _coverRemoveState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _coverRemoveState.value = Resource.Loading
            shopRepository.removeShopCover(shopId).collect { result ->
                _coverRemoveState.value = result
                if (result is Resource.Success) {
                    commonShopViewModel.getShopDetails(shopId)
                }
            }
        }
    }

    fun updateCover(shopId: String, newImageUri: Uri) {
        viewModelScope.launch {
            removeCover(shopId)
            launch {
                _coverRemoveState.collect { removeResult ->
                    if (removeResult is Resource.Success) {
                        uploadCover(shopId, newImageUri)
                    }
                }
            }
        }
    }

    // ============= DELEGATED COMMON FUNCTIONS =============

    fun getShopDetails(shopId: String) = commonShopViewModel.getShopDetails(shopId)
    fun getShopRating(shopId: String) = commonShopViewModel.getShopRating(shopId)
    fun isShopOpen(shop: Shop?) = commonShopViewModel.isShopOpen(shop)
    fun getShopStatusMessage(shop: Shop?) = commonShopViewModel.getShopStatusMessage(shop)
    fun formatShopAddress(shop: Shop?) = commonShopViewModel.formatShopAddress(shop)
    fun getShopTimings(shop: Shop?) = commonShopViewModel.getShopTimings(shop)
    fun getFormattedRating(shop: Shop?) = commonShopViewModel.getFormattedRating(shop)
    fun getCategoryDisplay(category: String) = commonShopViewModel.getCategoryDisplay(category)
    fun canAcceptOrders(shop: Shop?) = commonShopViewModel.canAcceptOrders(shop)
    fun hasLogo(shop: Shop?) = commonShopViewModel.hasLogo(shop)
    fun hasCover(shop: Shop?) = commonShopViewModel.hasCover(shop)
    fun getLogo(shop: Shop?) = commonShopViewModel.getLogo(shop)
    fun getCover(shop: Shop?) = commonShopViewModel.getCover(shop)
    fun getShopCompletionPercentage(shop: Shop?) = commonShopViewModel.getShopCompletionPercentage(shop)

    // ============= STATE RESET =============

    fun resetCreateShopState() {
        _createShopState.value = Resource.Loading
    }

    fun resetUpdateShopState() {
        _updateShopState.value = Resource.Loading
    }

    fun resetDeactivateShopState() {
        _deactivateShopState.value = Resource.Loading
    }

    fun resetImageStates() {
        _logoUploadState.value = Resource.Loading
        _logoRemoveState.value = Resource.Loading
        _coverUploadState.value = Resource.Loading
        _coverRemoveState.value = Resource.Loading
    }

    fun resetAllStates() {
        _myShopState.value = Resource.Loading
        _createShopState.value = Resource.Loading
        _updateShopState.value = Resource.Loading
        _deactivateShopState.value = Resource.Loading
        _toggleStatusState.value = Resource.Loading
        _updateStatsState.value = Resource.Loading
        resetImageStates()
        commonShopViewModel.resetAllStates()
    }

    // ============= REFRESH =============

    fun refreshMyShop(ownerId: String) {
        getMyShop(ownerId)
    }

    // ============= CLEANUP =============

    override fun onCleared() {
        super.onCleared()
        resetAllStates()
    }
}

