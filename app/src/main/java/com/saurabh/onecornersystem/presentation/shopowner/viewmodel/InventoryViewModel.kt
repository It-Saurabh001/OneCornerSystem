package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.Inventory
import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.domain.usecase.inventory.CheckLowStockUseCase
import com.saurabh.onecornersystem.domain.usecase.inventory.UpdateStockUseCase
import com.saurabh.onecornersystem.domain.usecase.inventory.SyncInventoryUseCase
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing shop inventory
 * Handles inventory operations like checking low stock, updating stock, and syncing
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val checkLowStockUseCase: CheckLowStockUseCase,
    private val updateStockUseCase: UpdateStockUseCase,
    private val syncInventoryUseCase: SyncInventoryUseCase
) : ViewModel() {

    private val _lowStockProductsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val lowStockProductsState: StateFlow<Resource<List<Product>>> = _lowStockProductsState.asStateFlow()

    private val _updateStockState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateStockState: StateFlow<Resource<Boolean>?> = _updateStockState.asStateFlow()

    private val _syncInventoryState = MutableStateFlow<Resource<Boolean>?>(null)
    val syncInventoryState: StateFlow<Resource<Boolean>?> = _syncInventoryState.asStateFlow()

    private val _lowStockThreshold = MutableStateFlow(5)
    val lowStockThreshold: StateFlow<Int> = _lowStockThreshold.asStateFlow()

    fun checkLowStock(shopId: String, threshold: Int = 5) {
        _lowStockThreshold.value = threshold
        viewModelScope.launch {
            checkLowStockUseCase.execute(shopId, threshold).collect { result ->
                _lowStockProductsState.value = result
            }
        }
    }

    fun updateStock(productId: String, shopId: String, quantity: Int) {
        viewModelScope.launch {
            _updateStockState.value = Resource.Loading
            updateStockUseCase.execute(productId, shopId, quantity).collect { result ->
                _updateStockState.value = result
                if (result is Resource.Success) {
                    // Reload low stock products after update
                    checkLowStock(shopId, _lowStockThreshold.value)
                }
            }
        }
    }

    fun syncInventory(shopId: String) {
        viewModelScope.launch {
            _syncInventoryState.value = Resource.Loading
            syncInventoryUseCase.execute(shopId).collect { result ->
                _syncInventoryState.value = result
                if (result is Resource.Success) {
                    // Reload low stock products after sync
                    checkLowStock(shopId, _lowStockThreshold.value)
                }
            }
        }
    }

    fun loadLowStockProducts(shopId: String) {
        checkLowStock(shopId, _lowStockThreshold.value)
    }

    fun clearUpdateStockState() {
        _updateStockState.value = null
    }

    fun clearSyncInventoryState() {
        _syncInventoryState.value = null
    }

    fun setLowStockThreshold(threshold: Int) {
        _lowStockThreshold.value = threshold
    }
}

