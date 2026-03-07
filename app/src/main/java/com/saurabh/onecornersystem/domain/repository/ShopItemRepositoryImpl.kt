package com.saurabh.onecornersystem.domain.repository


import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType  // ✅ Use ShopType
import com.saurabh.onecornersystem.data.repository.ShopItemRepository
import com.saurabh.onecornersystem.utils.ImageUtils
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopItemRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentResolver: ContentResolver
) : ShopItemRepository {

    private val itemsCollection = firestore.collection("shop_items")
    private val shopsCollection = firestore.collection("shops")

    // ============= CREATE =============

    override fun createItem(
        item: ShopItem,
        imageUri: Uri?
    ): Flow<Resource<ShopItem>> = flow {
        emit(Resource.Loading)
        try {
            var newItem = item.copy(
                itemId = itemsCollection.document().id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            if (imageUri != null) {
                val imageBase64 = ImageUtils.uriToBase64(imageUri, contentResolver)
                if (imageBase64 != null) {
                    newItem = newItem.copy(images = listOf(imageBase64))
                }
            }

            itemsCollection
                .document(newItem.itemId)
                .set(newItem)
                .await()

            // Update shop's totalItems count
            updateShopItemCount(newItem.shopId, +1)

            emit(Resource.Success(newItem))
            Log.d("ShopItemRepo", "Item created: ${newItem.itemId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create item"))
            Log.e("ShopItemRepo", "Create error", e)
        }
    }

    // ============= READ =============

    override fun getItemById(itemId: String): Flow<Resource<ShopItem>> = flow {
        emit(Resource.Loading)
        try {
            val doc = itemsCollection.document(itemId).get().await()
            val item = doc.toObject(ShopItem::class.java)
            if (item != null) {
                emit(Resource.Success(item))
            } else {
                emit(Resource.Error("Item not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get item"))
            Log.e("ShopItemRepo", "Get item error", e)
        }
    }

    override fun getItemsByShop(shopId: String): Flow<Resource<List<ShopItem>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = itemsCollection
                .whereEqualTo("shopId", shopId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val items = snapshot.toObjects(ShopItem::class.java)
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get items"))
            Log.e("ShopItemRepo", "Get items error", e)
        }
    }

    override fun getItemsByShopAndType(
        shopId: String,
        itemType: ShopType
    ): Flow<Resource<List<ShopItem>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = itemsCollection
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("itemType", itemType.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val items = snapshot.toObjects(ShopItem::class.java)
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get items by type"))
            Log.e("ShopItemRepo", "Get items by type error", e)
        }
    }

    // ============= SEARCH =============

    override fun searchServices(
        query: String,
        shopType: ShopType
    ): Flow<Resource<List<ShopItem>>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("ShopItemRepo", "Searching services with query: $query, type: $shopType")

            // Get all services of the specified type
            val snapshot = itemsCollection
                .whereEqualTo("itemType", shopType.name)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val allItems = snapshot.toObjects(ShopItem::class.java)

            // Filter locally by name or description containing the query (case-insensitive)
            val queryLower = query.lowercase().trim()
            val filteredItems = allItems.filter { item ->
                item.name.lowercase().contains(queryLower) ||
                item.description.lowercase().contains(queryLower) ||
                item.category.lowercase().contains(queryLower)
            }

            Log.d("ShopItemRepo", "Search found ${filteredItems.size} services matching '$query'")
            emit(Resource.Success(filteredItems))
        } catch (e: Exception) {
            Log.e("ShopItemRepo", "Search services error", e)
            emit(Resource.Error(e.message ?: "Failed to search services"))
        }
    }

    // ============= UPDATE =============

    override fun updateItem(
        itemId: String,
        updates: Map<String, Any>
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = Timestamp.now()

            itemsCollection
                .document(itemId)
                .update(updateData)
                .await()

            emit(Resource.Success(true))
            Log.d("ShopItemRepo", "Item updated: $itemId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update item"))
            Log.e("ShopItemRepo", "Update error", e)
        }
    }

    override fun updateItemAvailability(
        itemId: String,
        isAvailable: Boolean
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            itemsCollection
                .document(itemId)
                .update(
                    mapOf(
                        "isAvailable" to isAvailable,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopItemRepo", "Item availability updated: $isAvailable")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update availability"))
            Log.e("ShopItemRepo", "Update availability error", e)
        }
    }

    override fun updateStock(
        itemId: String,
        stockQuantity: Int
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            itemsCollection
                .document(itemId)
                .update(
                    mapOf(
                        "stockQuantity" to stockQuantity,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopItemRepo", "Stock updated: $stockQuantity")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update stock"))
            Log.e("ShopItemRepo", "Update stock error", e)
        }
    }

    // ============= DELETE =============

    override fun deleteItem(itemId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val item = itemsCollection.document(itemId).get().await()
                .toObject(ShopItem::class.java)

            itemsCollection.document(itemId).delete().await()

            if (item != null) {
                updateShopItemCount(item.shopId, -1)
            }

            emit(Resource.Success(true))
            Log.d("ShopItemRepo", "Item deleted: $itemId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete item"))
            Log.e("ShopItemRepo", "Delete error", e)
        }
    }

    // ============= IMAGES =============

    override fun uploadItemImage(
        itemId: String,
        imageUri: Uri
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val imageBase64 = ImageUtils.uriToBase64(imageUri, contentResolver)
                ?: throw Exception("Failed to convert image")

            itemsCollection
                .document(itemId)
                .update(
                    mapOf(
                        "images" to listOf(imageBase64),
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(imageBase64))
            Log.d("ShopItemRepo", "Image uploaded for item: $itemId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to upload image"))
            Log.e("ShopItemRepo", "Upload image error", e)
        }
    }

    override fun removeItemImage(
        itemId: String,
        imageUrl: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            itemsCollection
                .document(itemId)
                .update(
                    mapOf(
                        "images" to emptyList<String>(),
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopItemRepo", "Image removed for item: $itemId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove image"))
            Log.e("ShopItemRepo", "Remove image error", e)
        }
    }

    // ============= HELPER FUNCTIONS =============

    private suspend fun updateShopItemCount(shopId: String, increment: Int) {
        try {
            val shopRef = shopsCollection.document(shopId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(shopRef)
                val currentCount = snapshot.getLong("totalItems") ?: 0
                transaction.update(shopRef, "totalItems", currentCount + increment)
            }.await()
        } catch (e: Exception) {
            Log.e("ShopItemRepo", "Failed to update shop item count", e)
        }
    }
}