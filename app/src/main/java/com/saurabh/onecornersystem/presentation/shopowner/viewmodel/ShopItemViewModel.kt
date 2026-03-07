package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import android.net.Uri
import android.util.Log
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

    private val _currentItemState = MutableStateFlow<Resource<ShopItem>>(Resource.Idle)
    val currentItemState: StateFlow<Resource<ShopItem>> = _currentItemState.asStateFlow()

    // Products
    private val _productsState = MutableStateFlow<Resource<List<ShopItem>>>(Resource.Idle)
    val productsState: StateFlow<Resource<List<ShopItem>>> = _productsState.asStateFlow()

    // Services
    private val _servicesState = MutableStateFlow<Resource<List<ShopItem>>>(Resource.Idle)
    val servicesState: StateFlow<Resource<List<ShopItem>>> = _servicesState.asStateFlow()

    // Single Item
    private val _itemState = MutableStateFlow<Resource<ShopItem>>(Resource.Idle)
    val itemState: StateFlow<Resource<ShopItem>> = _itemState.asStateFlow()

    // Create/Update/Delete
    private val _createItemState = MutableStateFlow<Resource<ShopItem>>(Resource.Idle)
    val createItemState: StateFlow<Resource<ShopItem>> = _createItemState.asStateFlow()

    private val _updateItemState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val updateItemState: StateFlow<Resource<Boolean>> = _updateItemState.asStateFlow()

    private val _deleteItemState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val deleteItemState: StateFlow<Resource<Boolean>> = _deleteItemState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val imageUploadState: StateFlow<Resource<String>> = _imageUploadState.asStateFlow()

    // Combined Loading State
    val isLoading: StateFlow<Boolean> = combine(
        _productsState,
        _servicesState,
        _createItemState,
        _updateItemState,
        _deleteItemState,
        _imageUploadState
    ) { states ->
        states.any { it is Resource.Loading }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

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
                active = true
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
        Log.d("ShopItemViewModel_Service", "getServicesByShop - shopId: $shopId")
        viewModelScope.launch {
            _servicesState.value = Resource.Loading
            Log.d("ShopItemViewModel_Service", "Fetching services from repository for shopId: $shopId")
            shopItemRepository.getItemsByShopAndType(shopId, ShopType.SERVICE)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("ShopItemViewModel_Service", "getServicesByShop Success - count: ${result.data.size}")
                            _servicesState.value = result
                        }
                        is Resource.Error -> {
                            Log.d("ShopItemViewModel_Service", "getServicesByShop Error - ${result.message}")
                            _servicesState.value = result
                        }
                        is Resource.Loading -> {
                            Log.d("ShopItemViewModel_Service", "getServicesByShop Loading...")
                            _servicesState.value = result
                        }
                        else -> _servicesState.value = result
                    }
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
        homeService: Boolean = false,
        requiresAppointment: Boolean = false,
        imageUri: Uri? = null
    ) {
        Log.d("ShopItemViewModel_Service", "createService - shopId: $shopId, name: $name, category: $category, price: $price")
        viewModelScope.launch {
            // Validate inputs
            if (name.isBlank() || category.isBlank() || price <= 0) {
                Log.d("ShopItemViewModel_Service", "Validation failed - name or category or price invalid")
                _createItemState.value = Resource.Error("Name, category and price are required")
                return@launch
            }

            Log.d("ShopItemViewModel_Service", "Creating service object - homeService: $homeService, appointmentRequired: $requiresAppointment, hasImage: ${imageUri != null}")
            val service = ShopItem(
                shopId = shopId,
                name = name,
                description = description,
                category = category,
                price = price,
                itemType = ShopType.SERVICE,
                duration = duration,
                homeService = homeService,
                requiresAppointment = requiresAppointment,
                isAvailable = true,
                active = true
            )

            Log.d("ShopItemViewModel_Service", "Uploading service to repository")
            shopItemRepository.createItem(service, imageUri)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("ShopItemViewModel_Service", "Service created successfully - itemId: ${result.data.itemId}")
                            _createItemState.value = result
                            Log.d("ShopItemViewModel_Service", "Refreshing services list after creation")
                            getServicesByShop(shopId)
                        }
                        is Resource.Error -> {
                            Log.d("ShopItemViewModel_Service", "Service creation failed - ${result.message}")
                            _createItemState.value = result
                        }
                        is Resource.Loading -> {
                            Log.d("ShopItemViewModel_Service", "Creating service...")
                            _createItemState.value = result
                        }
                        else -> _createItemState.value = result
                    }
                }
        }
    }


    // ============= COMMON OPERATIONS =============

    fun getItemById(itemId: String) {
        Log.d("ShopItemViewModel_Item", "getItemById - itemId: $itemId")
        viewModelScope.launch {
            _itemState.value = Resource.Loading
            _currentItemState.value = Resource.Loading
            Log.d("ShopItemViewModel_Item", "Fetching item from repository - itemId: $itemId")
            shopItemRepository.getItemById(itemId)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("ShopItemViewModel_Item", "Item loaded - name: ${result.data.name}, type: ${result.data.itemType}")
                            _itemState.value = result
                            _currentItemState.value = result
                        }
                        is Resource.Error -> {
                            Log.d("ShopItemViewModel_Item", "Error loading item - ${result.message}")
                            _itemState.value = result
                            _currentItemState.value = result
                        }
                        is Resource.Loading -> {
                            Log.d("ShopItemViewModel_Item", "Loading item...")
                            _itemState.value = result
                            _currentItemState.value = result
                        }
                        else -> {
                            _itemState.value = result
                            _currentItemState.value = result
                        }
                    }
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
        Log.d("ShopItemViewModel_Update", "updateItem - itemId: $itemId, updateFields: ${updates.keys}, hasImage: ${imageUri != null}")
        viewModelScope.launch {
            _updateItemState.value = Resource.Loading

            if (imageUri != null) {
                Log.d("ShopItemViewModel_Update", "Uploading new image for item - itemId: $itemId")
                // First upload new image
                shopItemRepository.uploadItemImage(itemId, imageUri)
                    .collect { imageResult ->
                        when (imageResult) {
                            is Resource.Success -> {
                                Log.d("ShopItemViewModel_Update", "Image uploaded successfully, updating item fields")
                                // Then update other fields
                                shopItemRepository.updateItem(itemId, updates)
                                    .collect { updateResult ->
                                        Log.d("ShopItemViewModel_Update", "Item update result - ${updateResult.javaClass.simpleName}")
                                        _updateItemState.value = updateResult
                                        if (updateResult is Resource.Success) {
                                            Log.d("ShopItemViewModel_Update", "Item updated successfully, refreshing list")
                                            refreshItems(shopId, itemType)
                                        }
                                    }
                            }
                            is Resource.Error -> {
                                Log.d("ShopItemViewModel_Update", "Image upload failed - ${imageResult.message}")
                                _updateItemState.value = Resource.Error(imageResult.message)
                            }
                            else -> {}
                        }
                    }
            } else {
                Log.d("ShopItemViewModel_Update", "Updating item without image - itemId: $itemId")
                // Just update fields
                shopItemRepository.updateItem(itemId, updates)
                    .collect { result ->
                        Log.d("ShopItemViewModel_Update", "Item update result - ${result.javaClass.simpleName}")
                        _updateItemState.value = result
                        if (result is Resource.Success) {
                            Log.d("ShopItemViewModel_Update", "Item updated successfully, refreshing list")
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
        Log.d("ShopItemViewModel_Availability", "toggleItemAvailability - itemId: $itemId, newStatus: $isAvailable")
        updateItem(itemId, mapOf("isAvailable" to isAvailable), null, shopId, itemType)
    }

    fun deleteItem(
        itemId: String,
        shopId: String,
        itemType: ShopType
    ) {
        Log.d("ShopItemViewModel_Delete", "deleteItem - itemId: $itemId, shopId: $shopId, itemType: $itemType")
        viewModelScope.launch {
            _deleteItemState.value = Resource.Loading
            Log.d("ShopItemViewModel_Delete", "Deleting item from repository")
            shopItemRepository.deleteItem(itemId)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("ShopItemViewModel_Delete", "Item deleted successfully")
                            _deleteItemState.value = result
                            refreshItems(shopId, itemType)
                        }
                        is Resource.Error -> {
                            Log.d("ShopItemViewModel_Delete", "Item deletion failed - ${result.message}")
                            _deleteItemState.value = result
                        }
                        is Resource.Loading -> {
                            Log.d("ShopItemViewModel_Delete", "Deleting...")
                            _deleteItemState.value = result
                        }
                        else -> _deleteItemState.value = result
                    }
                }
        }
    }

    // ============= REFRESH HELPER =============

    private fun refreshItems(shopId: String?, itemType: ShopType?) {
        Log.d("ShopItemViewModel_Refresh", "refreshItems - shopId: $shopId, itemType: $itemType")
        if (shopId != null && itemType != null) {
            when (itemType) {
                ShopType.PRODUCT -> {
                    Log.d("ShopItemViewModel_Refresh", "Refreshing products for shopId: $shopId")
                    getProductsByShop(shopId)
                }
                ShopType.SERVICE -> {
                    Log.d("ShopItemViewModel_Refresh", "Refreshing services for shopId: $shopId")
                    getServicesByShop(shopId)
                }
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