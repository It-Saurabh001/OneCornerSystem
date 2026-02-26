package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.model.ProductVariant
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /**
     * Create a new product for a shop
     */
    fun createProduct(product: Product): Flow<Resource<Product>>

    /**
     * Get product details
     */
    fun getProduct(productId: String, shopId: String): Flow<Resource<Product>>

    /**
     * Update product details
     */
    fun updateProduct(productId: String, shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>

    /**
     * Delete product (soft delete - mark as inactive)
     */
    fun deleteProduct(productId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Get all products for a shop
     */
    fun getShopProducts(shopId: String): Flow<Resource<List<Product>>>

    /**
     * Get products with pagination
     */
    fun getShopProductsPaginated(shopId: String, pageSize: Int, lastDocument: Any? = null): Flow<Resource<Pair<List<Product>, Any?>>>

    /**
     * Search products by name or category
     */
    fun searchProducts(shopId: String, query: String): Flow<Resource<List<Product>>>

    /**
     * Add a product variant
     */
    fun addVariant(productId: String, shopId: String, variant: ProductVariant): Flow<Resource<ProductVariant>>

    /**
     * Update product variant
     */
    fun updateVariant(productId: String, variantId: String, shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>

    /**
     * Delete product variant
     */
    fun deleteVariant(productId: String, variantId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Get all variants for a product
     */
    fun getProductVariants(productId: String, shopId: String): Flow<Resource<List<ProductVariant>>>

    /**
     * Real-time listener for shop products
     */
    fun listenToShopProducts(shopId: String): Flow<List<Product>>

    /**
     * Update product availability
     */
    fun updateProductAvailability(productId: String, shopId: String, isAvailable: Boolean): Flow<Resource<Boolean>>

    /**
     * Bulk update products status
     */
    fun bulkUpdateProductsStatus(shopId: String, productIds: List<String>, isActive: Boolean): Flow<Resource<Boolean>>

    /**
     * Get products by category
     */
    fun getProductsByCategory(shopId: String, category: String): Flow<Resource<List<Product>>>
}

