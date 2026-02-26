package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import android.net.Uri
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
) : ViewModel(){
    // Shop Details
    private val _shopDetailsState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val shopDetailsState: StateFlow<Resource<Shop>> = _shopDetailsState.asStateFlow()

    // My Shop (by owner)
    private val _myShopState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val myShopState: StateFlow<Resource<Shop>> = _myShopState.asStateFlow()

    // Update Operations
    private val _updateShopState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val updateShopState: StateFlow<Resource<Boolean>> = _updateShopState.asStateFlow()

    // Create Shop
    private val _createShopState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val createShopState: StateFlow<Resource<Shop>> = _createShopState.asStateFlow()

    // Deactivate Shop
    private val _deactivateShopState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val deactivateShopState: StateFlow<Resource<Boolean>> = _deactivateShopState.asStateFlow()


    // Toggle Status
    private val _toggleStatusState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val toggleStatusState: StateFlow<Resource<Boolean>> = _toggleStatusState.asStateFlow()

    // Update Stats
    private val _updateStatsState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val updateStatsState: StateFlow<Resource<Boolean>> = _updateStatsState.asStateFlow()

    // Shop Rating
    private val _shopRatingState = MutableStateFlow<Resource<Double>>(Resource.Loading)
    val shopRatingState: StateFlow<Resource<Double>> = _shopRatingState.asStateFlow()

    // Image Upload
    private val _coverUploadState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val coverUploadState: StateFlow<Resource<String>> = _coverUploadState.asStateFlow()

    // Image Remove
    private val _coverRemoveState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val coverRemoveState: StateFlow<Resource<Boolean>> = _coverRemoveState.asStateFlow()

    // Logo Upload
    private val _logoUploadState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val logoUploadState: StateFlow<Resource<String>> = _logoUploadState.asStateFlow()
    // Logo Remove
    private val _logoRemoveState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
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
        _coverRemoveState
    ) { states ->
        states.any { it is Resource.Loading }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

//        ======> shop creation  <======
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
                description = description, // location missing
                location = com.google.firebase.firestore.GeoPoint(latitude, longitude),
                address = address,
                city = city,
                pincode = pincode,
                contactNumber = contactNumber,
                email = email,
                openingTime = openingTime,
                closingTime = closingTime,
                operatingHours = operatingHours,
                isOpen = true,
                isActive = true,
                rating = 0.0,
                totalRatings = 0,
                totalProducts = 0,
                totalOrders = 0,
                totalRevenue = 0.0,
                averageOrderValue = 0.0,
                logo = "",
                coverImage = "",
                hasLogo = false,
                hasCover = false
            )

            shopRepository.createShop(shop).collect { result ->
                _createShopState.value = result
                if (result is Resource.Success) {
                    loadShopDetails(result.data.shopId)
                }
            }
        }
    }

//    ======> load shop details  <======

    fun loadShopDetails(shopId: String) {
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

    fun getMyShop(ownerId: String) {
        if (ownerId.isBlank()) {
            _myShopState.value = Resource.Error("Owner ID cannot be empty")
            return
        }

        viewModelScope.launch {
            shopRepository.getShopByOwner(ownerId).collect { result ->
                _myShopState.value = result
                if (result is Resource.Success) {
                    loadShopDetails(result.data.shopId)
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

//    ======> shop creation  <======

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

    fun updateShopName(shopId: String, newName: String) {
        performUpdate(shopId, mapOf("shopName" to newName))
    }

    fun updateShopDescription(shopId: String, newDescription: String) {
        performUpdate(shopId, mapOf("description" to newDescription))
    }

    fun updateShopContactNumber(shopId: String, newNumber: String) {
        performUpdate(shopId, mapOf("contactNumber" to newNumber))
    }

    fun updateShopAddress(shopId: String, newAddress: String) {
        performUpdate(shopId, mapOf("address" to newAddress))
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
                    loadShopDetails(shopId)
                }
            }
        }
    }


//    ======> shop status management  <======

    fun toggleShopOpenStatus(shopId: String, isOpen: Boolean) {
        if (shopId.isBlank()) {
            _toggleStatusState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _toggleStatusState.value = Resource.Loading
            shopRepository.updateShopActiveStatus(shopId, isOpen).collect { result ->
                _toggleStatusState.value = result
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
            }
        }
    }

//    ======> shop statistics  <======

    fun updateShopStatistics(
        shopId: String,
        totalProducts: Int,
        totalOrders: Int,
        totalRevenue: Double
    ) {
        if (shopId.isBlank()) {
            _updateStatsState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _updateStatsState.value = Resource.Loading
            shopRepository.updateShopStats(shopId, totalProducts, totalOrders, totalRevenue)
                .collect { result ->
                    _updateStatsState.value = result
                }
        }
    }

    fun incrementProductCount(shopId: String, currentCount: Int) {
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

    //    ======> shop rating  <======


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


    //    ======> shop deletion  <======

    fun deactivateShop(shopId: String) {
        if (shopId.isBlank()) {
            _deactivateShopState.value = Resource.Error("Shop ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _deactivateShopState.value = Resource.Loading
            shopRepository.deleteShop(shopId).collect { result ->
                _deactivateShopState.value = result
            }
        }
    }

    // ======> logo image management <======

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
                    loadShopDetails(shopId)
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
                    loadShopDetails(shopId)
                }
            }
        }
    }

    fun updateLogo(shopId: String, newImageUri: Uri) {
        viewModelScope.launch {
            // Pehle purana hatao
            removeLogo(shopId)

            // Jab removal complete ho jaye, naya upload karo
            launch {
                _logoRemoveState.collect { removeResult ->
                    if (removeResult is Resource.Success) {
                        uploadLogo(shopId, newImageUri)
                    }
                }
            }
        }
    }

    // ======> cover image management <======

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
                    loadShopDetails(shopId)
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
                    loadShopDetails(shopId)
                }
            }
        }
    }

    fun updateCover(shopId: String, newImageUri: Uri) {
        viewModelScope.launch {
            // Pehle purana hatao
            removeCover(shopId)

            // Jab removal complete ho jaye, naya upload karo
            launch {
                _coverRemoveState.collect { removeResult ->
                    if (removeResult is Resource.Success) {
                        uploadCover(shopId, newImageUri)
                    }
                }
            }
        }
    }


    // ======> helper functions <======

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



    //    ======> state reset functions <======

    fun resetCreateShopState() {
        _createShopState.value = Resource.Loading
    }

    fun resetUpdateShopState() {
        _updateShopState.value = Resource.Loading
    }

    fun resetDeactivateShopState() {
        _deactivateShopState.value = Resource.Loading
    }

    fun resetAllStates() {
        _shopDetailsState.value = Resource.Loading
        _myShopState.value = Resource.Loading
        _updateShopState.value = Resource.Loading
        _createShopState.value = Resource.Loading
        _deactivateShopState.value = Resource.Loading
        _toggleStatusState.value = Resource.Loading
        _updateStatsState.value = Resource.Loading
        _shopRatingState.value = Resource.Loading
        _coverUploadState.value = Resource.Loading
    }

    //    ======> refresh functions  <======

    fun refreshShopDetails(shopId: String) {
        loadShopDetails(shopId)
    }

    fun refreshMyShop(ownerId: String) {
        getMyShop(ownerId)
    }

    //    ======> cleanup  <======

    override fun onCleared() {
        super.onCleared()
        resetAllStates()
    }


}