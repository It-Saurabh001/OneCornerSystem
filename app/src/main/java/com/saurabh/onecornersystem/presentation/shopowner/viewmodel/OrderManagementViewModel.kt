package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.Order
import com.saurabh.onecornersystem.domain.usecase.order.GetShopOrdersUseCase
import com.saurabh.onecornersystem.domain.usecase.order.AcceptOrderUseCase
import com.saurabh.onecornersystem.domain.usecase.order.RejectOrderUseCase
import com.saurabh.onecornersystem.domain.usecase.order.UpdateOrderStatusUseCase
import com.saurabh.onecornersystem.domain.usecase.order.MarkOrderAsDeliveredUseCase
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing shop orders
 * Handles order operations like accept, reject, update status, and mark as delivered
 */
@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val getShopOrdersUseCase: GetShopOrdersUseCase,
    private val acceptOrderUseCase: AcceptOrderUseCase,
    private val rejectOrderUseCase: RejectOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val markOrderAsDeliveredUseCase: MarkOrderAsDeliveredUseCase
) : ViewModel() {

    private val _allOrdersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading)
    val allOrdersState: StateFlow<Resource<List<Order>>> = _allOrdersState.asStateFlow()

    private val _pendingOrdersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading)
    val pendingOrdersState: StateFlow<Resource<List<Order>>> = _pendingOrdersState.asStateFlow()

    private val _acceptedOrdersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading)
    val acceptedOrdersState: StateFlow<Resource<List<Order>>> = _acceptedOrdersState.asStateFlow()

    private val _acceptOrderState = MutableStateFlow<Resource<Boolean>?>(null)
    val acceptOrderState: StateFlow<Resource<Boolean>?> = _acceptOrderState.asStateFlow()

    private val _rejectOrderState = MutableStateFlow<Resource<Boolean>?>(null)
    val rejectOrderState: StateFlow<Resource<Boolean>?> = _rejectOrderState.asStateFlow()

    private val _updateOrderStatusState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateOrderStatusState: StateFlow<Resource<Boolean>?> = _updateOrderStatusState.asStateFlow()

    private val _markAsDeliveredState = MutableStateFlow<Resource<Boolean>?>(null)
    val markAsDeliveredState: StateFlow<Resource<Boolean>?> = _markAsDeliveredState.asStateFlow()

    fun loadAllOrders(shopId: String) {
        viewModelScope.launch {
            getShopOrdersUseCase.execute(shopId).collect { result ->
                _allOrdersState.value = result
            }
        }
    }

    fun loadPendingOrders(shopId: String) {
        viewModelScope.launch {
            getShopOrdersUseCase.execute(shopId, "pending").collect { result ->
                _pendingOrdersState.value = result
            }
        }
    }

    fun loadAcceptedOrders(shopId: String) {
        viewModelScope.launch {
            getShopOrdersUseCase.execute(shopId, "accepted").collect { result ->
                _acceptedOrdersState.value = result
            }
        }
    }

    fun acceptOrder(orderId: String, shopId: String) {
        viewModelScope.launch {
            _acceptOrderState.value = Resource.Loading
            acceptOrderUseCase.execute(orderId, shopId).collect { result ->
                _acceptOrderState.value = result
                if (result is Resource.Success) {
                    loadAllOrders(shopId)
                    loadPendingOrders(shopId)
                }
            }
        }
    }

    fun rejectOrder(orderId: String, shopId: String, reason: String = "") {
        viewModelScope.launch {
            _rejectOrderState.value = Resource.Loading
            rejectOrderUseCase.execute(orderId, shopId, reason).collect { result ->
                _rejectOrderState.value = result
                if (result is Resource.Success) {
                    loadAllOrders(shopId)
                    loadPendingOrders(shopId)
                }
            }
        }
    }

    fun updateOrderStatus(orderId: String, shopId: String, newStatus: String) {
        viewModelScope.launch {
            _updateOrderStatusState.value = Resource.Loading
            updateOrderStatusUseCase.execute(orderId, shopId, newStatus).collect { result ->
                _updateOrderStatusState.value = result
                if (result is Resource.Success) {
                    loadAllOrders(shopId)
                    loadAcceptedOrders(shopId)
                }
            }
        }
    }

    fun markOrderAsDelivered(orderId: String, shopId: String) {
        viewModelScope.launch {
            _markAsDeliveredState.value = Resource.Loading
            markOrderAsDeliveredUseCase.execute(orderId, shopId).collect { result ->
                _markAsDeliveredState.value = result
                if (result is Resource.Success) {
                    loadAllOrders(shopId)
                    loadAcceptedOrders(shopId)
                }
            }
        }
    }

    fun loadOrdersByStatus(shopId: String, status: String) {
        viewModelScope.launch {
            getShopOrdersUseCase.execute(shopId, status).collect { result ->
                when (status.lowercase()) {
                    "pending" -> _pendingOrdersState.value = result
                    "accepted" -> _acceptedOrdersState.value = result
                    else -> _allOrdersState.value = result
                }
            }
        }
    }

    fun clearAcceptOrderState() {
        _acceptOrderState.value = null
    }

    fun clearRejectOrderState() {
        _rejectOrderState.value = null
    }

    fun clearUpdateOrderStatusState() {
        _updateOrderStatusState.value = null
    }

    fun clearMarkAsDeliveredState() {
        _markAsDeliveredState.value = null
    }
}

