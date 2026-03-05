package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.OperatingHour
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.repository.ShopItemRepository
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
class ShopItemViewModel @Inject constructor(
    private val shopItemRepository: ShopItemRepository
) : ViewModel() {

    // ============= STATES =============

    private val _currentItemState = MutableStateFlow<Resource<ShopItem>>(Resource.Loading)
    val currentItemState: StateFlow<Resource<ShopItem>> = _currentItemState.asStateFlow()

    // Products
    private val _productsState = MutableStateFlow<Resource<List<ShopItem>>>(Resource.Loading)
    val productsState: StateFlow<Resource<List<ShopItem>>> = _productsState.asStateFlow()

    // Services
    private val _servicesState = MutableStateFlow<Resource<List<ShopItem>>>(Resource.Loading)
    val servicesState: StateFlow<Resource<List<ShopItem>>> = _servicesState.asStateFlow()

    // Single Item
    private val _itemState = MutableStateFlow<Resource<ShopItem>>(Resource.Loading)
    val itemState: StateFlow<Resource<ShopItem>> = _itemState.asStateFlow()

    // Create/Update/Delete
    private val _createItemState = MutableStateFlow<Resource<ShopItem>>(Resource.Loading)
    val createItemState: StateFlow<Resource<ShopItem>> = _createItemState.asStateFlow()

    private val _updateItemState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val updateItemState: StateFlow<Resource<Boolean>> = _updateItemState.asStateFlow()

    private val _deleteItemState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val deleteItemState: StateFlow<Resource<Boolean>> = _deleteItemState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val imageUploadState: StateFlow<Resource<String>> = _imageUploadState.asStateFlow()

    // Combined Loading State
    val isLoading: StateFlow<Boolean> = combineLoadingStates()

    private fun combineLoadingStates(): StateFlow<Boolean> {
        // Simple implementation - can be enhanced with combine if needed
        return MutableStateFlow(
            _productsState.value is Resource.Loading ||
                    _servicesState.value is Resource.Loading ||
                    _createItemState.value is Resource.Loading ||
                    _updateItemState.value is Resource.Loading ||
                    _deleteItemState.value is Resource.Loading ||
                    _imageUploadState.value is Resource.Loading
        ).asStateFlow()
    }

    // ============= PRODUCTS =============

    fun getProductsByShop(shopId: String) {
        viewModelScope.launch {
            _productsState.value = Resource.Loading
            shopItemRepository.getItemsByShopAndType(shopId, ShopType.PRODUCT)
                .collect { result ->
                    _productsState.value = result
                }
        }
    }

    fun createProduct(
        shopId: String,
        name: String,
        description: String,
        category: String,
        price: Double,
        stockQuantity: Int,
        unit: String = "piece",
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            // Validate inputs
            if (name.isBlank() || category.isBlank() || price <= 0) {
                _createItemState.value = Resource.Error("Name, category and price are required")
                return@launch
            }

            val product = ShopItem(
                shopId = shopId,
                name = name,
                description = description,
                category = category,
                price = price,
                itemType = ShopType.PRODUCT,
                stockQuantity = stockQuantity,
                unit = unit,
                isAvailable = true,
                isActive = true
            )

            shopItemRepository.createItem(product, imageUri)
                .collect { result ->
                    _createItemState.value = result
                    if (result is Resource.Success) {
                        getProductsByShop(shopId)
                    }
                }
        }
    }

    // ============= SERVICES =============

    fun getServicesByShop(shopId: String) {
        viewModelScope.launch {
            _servicesState.value = Resource.Loading
            shopItemRepository.getItemsByShopAndType(shopId, ShopType.SERVICE)
                .collect { result ->
                    _servicesState.value = result
                }
        }
    }

    fun createService(
        shopId: String,
        name: String,
        description: String,
        category: String,
        price: Double,
        duration: String,
        isHomeService: Boolean = false,
        requiresAppointment: Boolean = false,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            // Validate inputs
            if (name.isBlank() || category.isBlank() || price <= 0) {
                _createItemState.value = Resource.Error("Name, category and price are required")
                return@launch
            }

            val service = ShopItem(
                shopId = shopId,
                name = name,
                description = description,
                category = category,
                price = price,
                itemType = ShopType.SERVICE,
                duration = duration,
                isHomeService = isHomeService,
                requiresAppointment = requiresAppointment,
                isAvailable = true,
                isActive = true
            )

            shopItemRepository.createItem(service, imageUri)
                .collect { result ->
                    _createItemState.value = result
                    if (result is Resource.Success) {
                        getServicesByShop(shopId)
                    }
                }
        }

        // ============= SINGLE ITEM =============


        fun getItemById(itemId: String) {
            viewModelScope.launch {
                shopItemRepository.getItemById(itemId)
                    .collect { result ->
                        _currentItemState.value = result
                    }
            }
        }
    }

    // ============= COMMON OPERATIONS =============

    fun getItemById(itemId: String) {
        viewModelScope.launch {
            _itemState.value = Resource.Loading
            shopItemRepository.getItemById(itemId)
                .collect { result ->
                    _itemState.value = result
                }
        }
    }

    fun updateItem(
        itemId: String,
        updates: Map<String, Any>,
        imageUri: Uri? = null,
        shopId: String? = null,
        itemType: ShopType? = null
    ) {
        viewModelScope.launch {
            _updateItemState.value = Resource.Loading

            if (imageUri != null) {
                // First upload new image
                shopItemRepository.uploadItemImage(itemId, imageUri)
                    .collect { imageResult ->
                        when (imageResult) {
                            is Resource.Success -> {
                                // Then update other fields
                                shopItemRepository.updateItem(itemId, updates)
                                    .collect { updateResult ->
                                        _updateItemState.value = updateResult
                                        if (updateResult is Resource.Success) {
                                            refreshItems(shopId, itemType)
                                        }
                                    }
                            }
                            is Resource.Error -> {
                                _updateItemState.value = Resource.Error(imageResult.message)
                            }
                            else -> {}
                        }
                    }
            } else {
                // Just update fields
                shopItemRepository.updateItem(itemId, updates)
                    .collect { result ->
                        _updateItemState.value = result
                        if (result is Resource.Success) {
                            refreshItems(shopId, itemType)
                        }
                    }
            }
        }
    }

    fun toggleItemAvailability(
        itemId: String,
        isAvailable: Boolean,
        shopId: String? = null,
        itemType: ShopType? = null
    ) {
        updateItem(itemId, mapOf("isAvailable" to isAvailable), null, shopId, itemType)
    }

    fun deleteItem(
        itemId: String,
        shopId: String,
        itemType: ShopType
    ) {
        viewModelScope.launch {
            _deleteItemState.value = Resource.Loading
            shopItemRepository.deleteItem(itemId)
                .collect { result ->
                    _deleteItemState.value = result
                    if (result is Resource.Success) {
                        refreshItems(shopId, itemType)
                    }
                }
        }
    }

    fun uploadItemImage(
        itemId: String,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            _imageUploadState.value = Resource.Loading
            shopItemRepository.uploadItemImage(itemId, imageUri)
                .collect { result ->
                    _imageUploadState.value = result
                }
        }
    }

    // ============= REFRESH HELPER =============

    private fun refreshItems(shopId: String?, itemType: ShopType?) {
        if (shopId != null && itemType != null) {
            when (itemType) {
                ShopType.PRODUCT -> getProductsByShop(shopId)
                ShopType.SERVICE -> getServicesByShop(shopId)
            }
        }
    }

    // ============= RESET FUNCTIONS =============

    fun resetCreateState() {
        _createItemState.value = Resource.Loading
    }

    fun resetUpdateState() {
        _updateItemState.value = Resource.Loading
    }

    fun resetDeleteState() {
        _deleteItemState.value = Resource.Loading
    }

    fun resetAllStates() {
        _productsState.value = Resource.Loading
        _servicesState.value = Resource.Loading
        _itemState.value = Resource.Loading
        _createItemState.value = Resource.Loading
        _updateItemState.value = Resource.Loading
        _deleteItemState.value = Resource.Loading
        _imageUploadState.value = Resource.Loading
    }

    // ============= CLEANUP =============

    override fun onCleared() {
        super.onCleared()
        resetAllStates()
    }
}