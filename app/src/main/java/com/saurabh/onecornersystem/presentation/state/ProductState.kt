package com.saurabh.onecornersystem.presentation.state

import com.saurabh.onecornersystem.data.model.Product

/**
 * Sealed class representing different states for product operations
 */
sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class ProductSuccess(val product: Product) : ProductState()
    data class Error(val message: String) : ProductState()
}

/**
 * Product UI state holding all product-related state flows
 */
data class ProductUiState(
    val productState: ProductState = ProductState.Idle,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val selectedProduct: Product? = null
)

