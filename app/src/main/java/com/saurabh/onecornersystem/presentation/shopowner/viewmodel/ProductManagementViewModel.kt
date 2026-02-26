package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.model.ProductVariant
import com.saurabh.onecornersystem.domain.usecase.product.CreateProductUseCase
import com.saurabh.onecornersystem.domain.usecase.product.ListProductsUseCase
import com.saurabh.onecornersystem.domain.usecase.product.UpdateProductUseCase
import com.saurabh.onecornersystem.domain.usecase.product.DeleteProductUseCase
import com.saurabh.onecornersystem.domain.usecase.product.AddProductVariantUseCase
import com.saurabh.onecornersystem.domain.usecase.product.UpdatePricingUseCase
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing shop products
 * Handles CRUD operations for products and variants
 */
@HiltViewModel
class ProductManagementViewModel @Inject constructor(
    private val createProductUseCase: CreateProductUseCase,
    private val listProductsUseCase: ListProductsUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val addProductVariantUseCase: AddProductVariantUseCase,
    private val updatePricingUseCase: UpdatePricingUseCase
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val productsState: StateFlow<Resource<List<Product>>> = _productsState.asStateFlow()

    private val _createProductState = MutableStateFlow<Resource<Product>?>(null)
    val createProductState: StateFlow<Resource<Product>?> = _createProductState.asStateFlow()

    private val _updateProductState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateProductState: StateFlow<Resource<Boolean>?> = _updateProductState.asStateFlow()

    private val _deleteProductState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteProductState: StateFlow<Resource<Boolean>?> = _deleteProductState.asStateFlow()

    private val _addVariantState = MutableStateFlow<Resource<ProductVariant>?>(null)
    val addVariantState: StateFlow<Resource<ProductVariant>?> = _addVariantState.asStateFlow()

    private val _updatePricingState = MutableStateFlow<Resource<Boolean>?>(null)
    val updatePricingState: StateFlow<Resource<Boolean>?> = _updatePricingState.asStateFlow()

    fun loadProducts(shopId: String, category: String? = null, searchQuery: String? = null) {
        viewModelScope.launch {
            listProductsUseCase.execute(shopId, category, searchQuery).collect { result ->
                _productsState.value = result
            }
        }
    }

    fun createProduct(product: Product) {
        viewModelScope.launch {
            _createProductState.value = Resource.Loading
            createProductUseCase.execute(product).collect { result ->
                _createProductState.value = result
                if (result is Resource.Success) {
                    // Reload products after successful creation
                    loadProducts(product.shopId)
                }
            }
        }
    }

    fun updateProduct(productId: String, shopId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _updateProductState.value = Resource.Loading
            updateProductUseCase.execute(productId, shopId, updates).collect { result ->
                _updateProductState.value = result
                if (result is Resource.Success) {
                    // Reload products after successful update
                    loadProducts(shopId)
                }
            }
        }
    }

    fun deleteProduct(productId: String, shopId: String) {
        viewModelScope.launch {
            _deleteProductState.value = Resource.Loading
            deleteProductUseCase.execute(productId, shopId).collect { result ->
                _deleteProductState.value = result
                if (result is Resource.Success) {
                    // Reload products after successful deletion
                    loadProducts(shopId)
                }
            }
        }
    }

    fun addProductVariant(productId: String, shopId: String, variant: ProductVariant) {
        viewModelScope.launch {
            _addVariantState.value = Resource.Loading
            addProductVariantUseCase.execute(productId, shopId, variant).collect { result ->
                _addVariantState.value = result
            }
        }
    }

    fun updateProductPricing(
        productId: String,
        shopId: String,
        price: Double,
        discountedPrice: Double? = null
    ) {
        viewModelScope.launch {
            _updatePricingState.value = Resource.Loading
            updatePricingUseCase.execute(productId, shopId, price, discountedPrice).collect { result ->
                _updatePricingState.value = result
                if (result is Resource.Success) {
                    // Reload products after successful pricing update
                    loadProducts(shopId)
                }
            }
        }
    }

    fun clearCreateProductState() {
        _createProductState.value = null
    }

    fun clearUpdateProductState() {
        _updateProductState.value = null
    }

    fun clearDeleteProductState() {
        _deleteProductState.value = null
    }

    fun clearAddVariantState() {
        _addVariantState.value = null
    }

    fun clearUpdatePricingState() {
        _updatePricingState.value = null
    }
}

