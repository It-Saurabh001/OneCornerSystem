package com.saurabh.onecornersystem.presentation.state

import com.saurabh.onecornersystem.data.model.Order

/**
 * Sealed class representing different states for order operations
 */
sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val orders: List<Order>) : OrderState()
    data class OrderSuccess(val order: Order) : OrderState()
    data class Error(val message: String) : OrderState()
}

/**
 * Order UI state holding all order-related state flows
 */
data class OrderUiState(
    val orderState: OrderState = OrderState.Idle,
    val isAccepting: Boolean = false,
    val isRejecting: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val selectedOrder: Order? = null,
    val filterStatus: String? = null
)

