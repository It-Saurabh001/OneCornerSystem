package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.saurabh.onecornersystem.data.model.Inventory
import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.model.StockAlert
import com.saurabh.onecornersystem.data.repository.InventoryRepository
import com.saurabh.onecornersystem.data.repository.InventoryMovement
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : InventoryRepository {

    override fun getShopInventory(shopId: String): Flow<Resource<Inventory>> = flow {
        emit(Resource.Loading)
        try {
            val inventoryDoc = firestore.collection("inventory").document(shopId).get().await()
            val inventory = inventoryDoc.toObject(Inventory::class.java)
            if (inventory != null) {
                emit(Resource.Success(inventory))
            } else {
                val defaultInventory = Inventory(inventoryId = shopId, shopId = shopId)
                emit(Resource.Success(defaultInventory))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get inventory"))
            Log.e("InventoryRepository", "Get inventory error", e)
        }
    }

    override fun updateStockQuantity(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("shops").document(shopId).collection("products").document(productId)
                .update(mapOf("stockQuantity" to quantity, "updatedAt" to com.google.firebase.Timestamp.now())).await()
            firestore.collection("products").document(productId).update("stockQuantity", quantity).await()
            createInventoryMovement(shopId, productId, "adjustment", quantity, "Manual adjustment")
            emit(Resource.Success(true))
            Log.d("InventoryRepository", "Stock updated: $productId -> $quantity")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update stock"))
            Log.e("InventoryRepository", "Update stock error", e)
        }
    }

    override fun updateVariantStock(productId: String, variantId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("shops").document(shopId).collection("products").document(productId).collection("variants").document(variantId)
                .update(mapOf("stockQuantity" to quantity, "stockStatus" to if (quantity == 0) "out_of_stock" else if (quantity < 5) "low_stock" else "in_stock", "updatedAt" to com.google.firebase.Timestamp.now())).await()
            emit(Resource.Success(true))
            Log.d("InventoryRepository", "Variant stock updated: $variantId -> $quantity")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update variant stock"))
            Log.e("InventoryRepository", "Update variant stock error", e)
        }
    }

    override fun increaseStock(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val productDoc = firestore.collection("shops").document(shopId).collection("products").document(productId).get().await()
            val product = productDoc.toObject(Product::class.java)
            if (product != null) {
                val newQuantity = product.stockQuantity + quantity
                firestore.collection("shops").document(shopId).collection("products").document(productId)
                    .update(mapOf("stockQuantity" to newQuantity, "updatedAt" to com.google.firebase.Timestamp.now())).await()
                createInventoryMovement(shopId, productId, "in", quantity, "Restock")
                emit(Resource.Success(true))
                Log.d("InventoryRepository", "Stock increased: $productId by $quantity")
            } else {
                emit(Resource.Error("Product not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to increase stock"))
            Log.e("InventoryRepository", "Increase stock error", e)
        }
    }

    override fun decreaseStock(productId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val productDoc = firestore.collection("shops").document(shopId).collection("products").document(productId).get().await()
            val product = productDoc.toObject(Product::class.java)
            if (product != null) {
                val newQuantity = maxOf(0, product.stockQuantity - quantity)
                firestore.collection("shops").document(shopId).collection("products").document(productId)
                    .update(mapOf("stockQuantity" to newQuantity, "isAvailable" to (newQuantity > 0), "updatedAt" to com.google.firebase.Timestamp.now())).await()
                createInventoryMovement(shopId, productId, "out", quantity, "Sale")
                emit(Resource.Success(true))
                Log.d("InventoryRepository", "Stock decreased: $productId by $quantity")
            } else {
                emit(Resource.Error("Product not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to decrease stock"))
            Log.e("InventoryRepository", "Decrease stock error", e)
        }
    }

    override fun decreaseVariantStock(productId: String, variantId: String, shopId: String, quantity: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val variantDoc = firestore.collection("shops").document(shopId).collection("products").document(productId).collection("variants").document(variantId).get().await()
            val variant = variantDoc.toObject(com.saurabh.onecornersystem.data.model.ProductVariant::class.java)
            if (variant != null) {
                val newQuantity = maxOf(0, variant.stockQuantity - quantity)
                firestore.collection("shops").document(shopId).collection("products").document(productId).collection("variants").document(variantId)
                    .update(mapOf("stockQuantity" to newQuantity, "stockStatus" to if (newQuantity == 0) "out_of_stock" else if (newQuantity < 5) "low_stock" else "in_stock", "updatedAt" to com.google.firebase.Timestamp.now())).await()
                emit(Resource.Success(true))
                Log.d("InventoryRepository", "Variant stock decreased: $variantId by $quantity")
            } else {
                emit(Resource.Error("Variant not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to decrease variant stock"))
            Log.e("InventoryRepository", "Decrease variant stock error", e)
        }
    }

    override fun getStockStatus(productId: String, shopId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val productDoc = firestore.collection("shops").document(shopId).collection("products").document(productId).get().await()
            val product = productDoc.toObject(Product::class.java)
            if (product != null) {
                emit(Resource.Success(product.stockQuantity))
            } else {
                emit(Resource.Error("Product not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get stock status"))
            Log.e("InventoryRepository", "Get stock status error", e)
        }
    }

    override fun getLowStockProducts(shopId: String, threshold: Int): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("shops").document(shopId).collection("products")
                .whereLessThanOrEqualTo("stockQuantity", threshold)
                .whereEqualTo("isActive", true)
                .get().await()
            val products = querySnapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get low stock products"))
            Log.e("InventoryRepository", "Get low stock products error", e)
        }
    }

    override fun getOutOfStockProducts(shopId: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("shops").document(shopId).collection("products")
                .whereEqualTo("stockQuantity", 0)
                .whereEqualTo("isActive", true)
                .get().await()
            val products = querySnapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get out of stock products"))
            Log.e("InventoryRepository", "Get out of stock products error", e)
        }
    }

    override fun getStockAlerts(shopId: String, resolved: Boolean): Flow<Resource<List<StockAlert>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("stockAlerts")
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("isResolved", resolved)
                .get().await()
            val alerts = querySnapshot.documents.mapNotNull { it.toObject(StockAlert::class.java) }
            emit(Resource.Success(alerts))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get stock alerts"))
            Log.e("InventoryRepository", "Get stock alerts error", e)
        }
    }

    override fun createStockAlert(alert: StockAlert): Flow<Resource<StockAlert>> = flow {
        emit(Resource.Loading)
        try {
            val alertWithId = alert.copy(alertId = firestore.collection("stockAlerts").document().id)
            firestore.collection("stockAlerts").document(alertWithId.alertId).set(alertWithId).await()
            emit(Resource.Success(alertWithId))
            Log.d("InventoryRepository", "Stock alert created: ${alertWithId.alertId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create stock alert"))
            Log.e("InventoryRepository", "Create stock alert error", e)
        }
    }

    override fun resolveStockAlert(alertId: String, shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("stockAlerts").document(alertId).update(mapOf("isResolved" to true, "resolvedAt" to com.google.firebase.Timestamp.now())).await()
            emit(Resource.Success(true))
            Log.d("InventoryRepository", "Stock alert resolved: $alertId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to resolve stock alert"))
            Log.e("InventoryRepository", "Resolve stock alert error", e)
        }
    }

    override fun syncInventory(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val productsSnapshot = firestore.collection("shops").document(shopId).collection("products").whereEqualTo("isActive", true).get().await()
            val products = productsSnapshot.documents.mapNotNull { it.toObject(Product::class.java) }

            val totalProducts = products.size
            val activeProducts = products.count { it.isAvailable }
            val inStockProducts = products.count { it.stockQuantity > 0 }
            val lowStockProducts = products.count { it.stockQuantity in 1..5 }
            val outOfStockProducts = products.count { it.stockQuantity == 0 }

            firestore.collection("inventory").document(shopId).set(
                Inventory(
                    inventoryId = shopId,
                    shopId = shopId,
                    totalProducts = totalProducts,
                    activeProducts = activeProducts,
                    inStockProducts = inStockProducts,
                    lowStockProducts = lowStockProducts,
                    outOfStockProducts = outOfStockProducts,
                    lastSyncTime = com.google.firebase.Timestamp.now()
                )
            ).await()

            emit(Resource.Success(true))
            Log.d("InventoryRepository", "Inventory synced: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to sync inventory"))
            Log.e("InventoryRepository", "Sync inventory error", e)
        }
    }

    override fun setLowStockThreshold(shopId: String, threshold: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("inventory").document(shopId).update("lowStockAlert", threshold).await()
            emit(Resource.Success(true))
            Log.d("InventoryRepository", "Low stock threshold set: $threshold")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to set threshold"))
            Log.e("InventoryRepository", "Set threshold error", e)
        }
    }

    override fun getInventoryHistory(shopId: String, limit: Int): Flow<Resource<List<InventoryMovement>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("inventory").document(shopId).collection("movements")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()
            val movements = querySnapshot.documents.mapNotNull { it.toObject(InventoryMovement::class.java) }
            emit(Resource.Success(movements))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get inventory history"))
            Log.e("InventoryRepository", "Get inventory history error", e)
        }
    }

    override fun listenToShopInventory(shopId: String): Flow<Inventory?> = flow {
        try {
            firestore.collection("inventory").document(shopId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("InventoryRepository", "Listen to inventory error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val inventory = snapshot.toObject(Inventory::class.java)
                }
            }
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Listen to inventory error", e)
        }
    }

    override fun listenToLowStockProducts(shopId: String, threshold: Int): Flow<List<Product>> = flow {
        try {
            firestore.collection("shops").document(shopId).collection("products")
                .whereLessThanOrEqualTo("stockQuantity", threshold)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("InventoryRepository", "Listen to low stock error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val products = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
                    }
                }
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Listen to low stock error", e)
        }
    }

    private suspend fun createInventoryMovement(shopId: String, productId: String, type: String, quantity: Int, reason: String, orderId: String = "") {
        try {
            val movementId = firestore.collection("movements").document().id
            val movement = InventoryMovement(
                movementId = movementId,
                shopId = shopId,
                productId = productId,
                type = type,
                quantity = quantity,
                reason = reason,
                orderId = orderId,
                timestamp = com.google.firebase.Timestamp.now()
            )

            firestore.collection("inventory").document(shopId).collection("movements").document(movementId).set(movement).await()
            Log.d("InventoryRepository", "Inventory movement recorded: $movementId")
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Create movement error", e)
        }
    }
}


