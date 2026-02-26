package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.Inventory
import com.saurabh.onecornersystem.data.model.StockAlert
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    /**
     * Get shop inventory summary
     */
    fun getShopInventory(shopId: String): Flow<Resource<Inventory>>

    /**
     * Update stock quantity for a product
     */
    fun updateStockQuantity(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>

    /**
     * Update stock quantity for a variant
     */
    fun updateVariantStock(productId: String, variantId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>

    /**
     * Increase stock (restocking)
     */
    fun increaseStock(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>

    /**
     * Decrease stock (sale)
     */
    fun decreaseStock(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>

    /**
     * Decrease variant stock
     */
    fun decreaseVariantStock(productId: String, variantId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>>

    /**
     * Get stock status for a product
     */
    fun getStockStatus(productId: String, shopId: String): Flow<Resource<Int>>

    /**
     * Get products with low stock
     */
    fun getLowStockProducts(shopId: String, threshold: Int = 5): Flow<Resource<List<com.saurabh.onecornersystem.data.model.Product>>>

    /**
     * Get out of stock products
     */
    fun getOutOfStockProducts(shopId: String): Flow<Resource<List<com.saurabh.onecornersystem.data.model.Product>>>

    /**
     * Get stock alerts
     */
    fun getStockAlerts(shopId: String, resolved: Boolean = false): Flow<Resource<List<StockAlert>>>

    /**
     * Create stock alert
     */
    fun createStockAlert(alert: StockAlert): Flow<Resource<StockAlert>>

    /**
     * Resolve stock alert
     */
    fun resolveStockAlert(alertId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Sync inventory (refresh counts)
     */
    fun syncInventory(shopId: String): Flow<Resource<Boolean>>

    /**
     * Set low stock threshold
     */
    fun setLowStockThreshold(shopId: String, threshold: Int): Flow<Resource<Boolean>>

    /**
     * Get inventory history (movements)
     */
    fun getInventoryHistory(shopId: String, limit: Int = 100): Flow<Resource<List<InventoryMovement>>>

    /**
     * Real-time listener for inventory
     */
    fun listenToShopInventory(shopId: String): Flow<Inventory?>

    /**
     * Real-time listener for low stock products
     */
    fun listenToLowStockProducts(shopId: String, threshold: Int = 5): Flow<List<com.saurabh.onecornersystem.data.model.Product>>
}

data class InventoryMovement(
    val movementId: String = "",
    val shopId: String = "",
    val productId: String = "",
    val type: String = "", // in, out, adjustment
    val quantity: Int = 0,
    val reason: String = "", // sale, return, restock, damage, etc.
    val orderId: String = "",
    val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

