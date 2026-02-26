package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.domain.usecase.shop.GetShopDetailsUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.UpdateShopProfileUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.CreateShopUseCase
import com.saurabh.onecornersystem.domain.usecase.shop.DeactivateShopUseCase
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing shop profile
 * Handles shop CRUD operations and profile updates
 */
@HiltViewModel
class ShopProfileViewModel @Inject constructor(
    private val getShopDetailsUseCase: GetShopDetailsUseCase,
    private val updateShopProfileUseCase: UpdateShopProfileUseCase,
    private val createShopUseCase: CreateShopUseCase,
    private val deactivateShopUseCase: DeactivateShopUseCase
) : ViewModel() {

    private val _shopDetailsState = MutableStateFlow<Resource<Shop>>(Resource.Loading)
    val shopDetailsState: StateFlow<Resource<Shop>> = _shopDetailsState.asStateFlow()

    private val _updateShopState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateShopState: StateFlow<Resource<Boolean>?> = _updateShopState.asStateFlow()

    private val _createShopState = MutableStateFlow<Resource<Shop>?>(null)
    val createShopState: StateFlow<Resource<Shop>?> = _createShopState.asStateFlow()

    private val _deactivateShopState = MutableStateFlow<Resource<Boolean>?>(null)
    val deactivateShopState: StateFlow<Resource<Boolean>?> = _deactivateShopState.asStateFlow()

    fun loadShopDetails(shopId: String) {
        viewModelScope.launch {
            getShopDetailsUseCase.execute(shopId).collect { result ->
                _shopDetailsState.value = result
            }
        }
    }

    fun updateShopProfile(shopId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _updateShopState.value = Resource.Loading
            updateShopProfileUseCase.execute(shopId, updates).collect { result ->
                _updateShopState.value = result
                if (result is Resource.Success) {
                    // Reload shop details after successful update
                    loadShopDetails(shopId)
                }
            }
        }
    }

    fun createShop(shop: Shop) {
        viewModelScope.launch {
            _createShopState.value = Resource.Loading
            createShopUseCase.execute(shop).collect { result ->
                _createShopState.value = result
                if (result is Resource.Success) {
                    // Load the newly created shop details
                    loadShopDetails(result.data.shopId)
                }
            }
        }
    }

    fun deactivateShop(shopId: String) {
        viewModelScope.launch {
            _deactivateShopState.value = Resource.Loading
            deactivateShopUseCase.execute(shopId).collect { result ->
                _deactivateShopState.value = result
            }
        }
    }

    fun updateShopName(shopId: String, newName: String) {
        updateShopProfile(shopId, mapOf("shopName" to newName))
    }

    fun updateShopDescription(shopId: String, newDescription: String) {
        updateShopProfile(shopId, mapOf("description" to newDescription))
    }

    fun updateShopContactNumber(shopId: String, newNumber: String) {
        updateShopProfile(shopId, mapOf("contactNumber" to newNumber))
    }

    fun updateShopAddress(shopId: String, newAddress: String) {
        updateShopProfile(shopId, mapOf("address" to newAddress))
    }

    fun clearUpdateShopState() {
        _updateShopState.value = null
    }

    fun clearCreateShopState() {
        _createShopState.value = null
    }

    fun clearDeactivateShopState() {
        _deactivateShopState.value = null
    }

    fun refreshShopDetails(shopId: String) {
        loadShopDetails(shopId)
    }
}

