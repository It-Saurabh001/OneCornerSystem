package com.saurabh.onecornersystem.presentation.state

import com.saurabh.onecornersystem.data.model.Shop

/**
 * Sealed class representing different states for shop operations
 */
sealed class ShopState {
    object Idle : ShopState()
    object Loading : ShopState()
    data class Success(val shop: Shop) : ShopState()
    data class Error(val message: String) : ShopState()
}

/**
 * Shop UI state holding all shop-related state flows
 */
data class ShopUiState(
    val shopState: ShopState = ShopState.Idle,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null
)

