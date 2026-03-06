package com.saurabh.onecornersystem.domain.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.utils.ImageUtils
import com.saurabh.onecornersystem.utils.LocationUtils
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentResolver: ContentResolver
) : ShopRepository {

    private val shopsCollection = firestore.collection("shops")
    private val favoritesCollection = firestore.collection("favorites")

    // ============= BASIC CRUD =============

    override fun createShop(shop: Shop): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            val shopWithId = shop.copy(
                shopId = shopsCollection.document().id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            shopsCollection
                .document(shopWithId.shopId)
                .set(shopWithId)
                .await()

            emit(Resource.Success(shopWithId))
            Log.d("ShopRepository", "Shop created: ${shopWithId.shopId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create shop"))
            Log.e("ShopRepository", "Create shop error", e)
        }
    }

    override fun createShopWithImages(
        shop: Shop,
        logoUri: Uri?,
        coverUri: Uri?
    ): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            var updatedShop = shop.copy(
                shopId = shopsCollection.document().id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                hasLogo = false,
                hasCover = false
            )

            if (logoUri != null) {
                val logoBase64 = ImageUtils.uriToBase64(logoUri, contentResolver)
                if (logoBase64 != null) {
                    updatedShop = updatedShop.copy(
                        logo = logoBase64,
                        hasLogo = true
                    )
                }
            }

            if (coverUri != null) {
                val coverBase64 = ImageUtils.uriToBase64(coverUri, contentResolver)
                if (coverBase64 != null) {
                    updatedShop = updatedShop.copy(
                        coverImage = coverBase64,
                        hasCover = true
                    )
                }
            }

            shopsCollection
                .document(updatedShop.shopId)
                .set(updatedShop)
                .await()

            emit(Resource.Success(updatedShop))
            Log.d("ShopRepository", "Shop created with images: ${updatedShop.shopId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create shop with images"))
            Log.e("ShopRepository", "Create shop with images error", e)
        }
    }

    override fun getShopDetails(shopId: String): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            val shopDoc = shopsCollection
                .document(shopId)
                .get()
                .await()

            val shop = shopDoc.toObject(Shop::class.java)
            if (shop != null) {
                // Ensure we always set the Firestore document id onto the model
                val mapped = shop.copy(shopId = shopDoc.id)
                emit(Resource.Success(mapped))
            } else {
                emit(Resource.Error("Shop not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop details"))
            Log.e("ShopRepository", "Get shop details error", e)
        }
    }

    override fun getShopByOwner(ownerId: String): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = shopsCollection
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()

            when {
                querySnapshot.isEmpty -> {
                    emit(Resource.Error("Shop not found for owner"))
                }
                querySnapshot.size() > 1 -> {
                    emit(Resource.Error("Multiple shops found for owner"))
                }
                else -> {
                    val doc = querySnapshot.documents[0]
                    val shop = doc.toObject(Shop::class.java)
                    if (shop != null) {
                        // Backfill shopId from document id
                        val mapped = shop.copy(shopId = doc.id)
                        emit(Resource.Success(mapped))
                    } else {
                        emit(Resource.Error("Shop data is invalid"))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop by owner"))
            Log.e("ShopRepository", "Get shop by owner error", e)
        }
    }

    override fun updateShopProfile(shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = Timestamp.now()

            shopsCollection
                .document(shopId)
                .update(updateData)
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Shop profile updated: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update shop profile"))
            Log.e("ShopRepository", "Update shop profile error", e)
        }
    }

    override fun updateShopActiveStatus(shopId: String, isActive: Boolean): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "isActive" to isActive,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Shop active status updated: $isActive")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update shop status"))
            Log.e("ShopRepository", "Update shop status error", e)
        }
    }

    override fun getShopRating(shopId: String): Flow<Resource<Double>> = flow {
        emit(Resource.Loading)
        try {
            val shopDoc = shopsCollection
                .document(shopId)
                .get()
                .await()

            val shop = shopDoc.toObject(Shop::class.java)
            if (shop != null) {
                emit(Resource.Success(shop.rating))
            } else {
                emit(Resource.Error("Shop not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop rating"))
            Log.e("ShopRepository", "Get shop rating error", e)
        }
    }

    override fun listenToShopDetails(shopId: String): Flow<Resource<Shop>> = callbackFlow {
        var listener: ListenerRegistration? = null

        try {
            listener = shopsCollection
                .document(shopId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Listen Failed"))
                        Log.e("ShopRepository", "Listen to shop details error", error)
                        return@addSnapshotListener
                    }
                    try {
                        when {
                            snapshot == null -> {
                                trySend(Resource.Error("Null Snapshot received"))
                            }
                            !snapshot.exists() -> {
                                trySend(Resource.Error("Shop not found"))
                            }
                            else -> {
                                val shop = snapshot.toObject(Shop::class.java)
                                if (shop != null) {
                                    // Map the document id back to shopId
                                    trySend(Resource.Success(shop.copy(shopId = snapshot.id)))
                                } else {
                                    trySend(Resource.Error("Shop data is invalid"))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.message ?: "Listen Failed Data processing error"))
                        Log.e("ShopRepository", "Listen to shop data details error", e)
                    }
                }
            awaitClose {
                try {
                    listener?.remove()
                    Log.d("ShopRepository", "Shop details listener removed")
                } catch (e: Exception) {
                    Log.e("ShopRepository", "Error removing listener", e)
                }
            }
        } catch (e: Exception) {
            Log.e("ShopRepository", "Listen to shop details error with shop id : $shopId", e)
            trySend(Resource.Error(e.message ?: "Listen Failed"))
            close(e)
        }
    }

    override fun deleteShop(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Shop deleted (soft): $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete shop"))
            Log.e("ShopRepository", "Delete shop error", e)
        }
    }

    override fun updateShopStats(
        shopId: String,
        totalItems: Int,
        totalOrders: Int,
        totalRevenue: Double
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "totalItems" to totalItems,
                        "totalOrders" to totalOrders,
                        "totalRevenue" to totalRevenue,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Shop stats updated: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update shop stats"))
            Log.e("ShopRepository", "Update shop stats error", e)
        }
    }

    // ============= IMAGE MANAGEMENT =============

    override fun uploadShopLogo(shopId: String, imageUri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val logoBase64 = ImageUtils.uriToBase64(imageUri, contentResolver)
                ?: throw Exception("Failed to convert image")

            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "logo" to logoBase64,
                        "hasLogo" to true,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(logoBase64))
            Log.d("ShopRepository", "Logo uploaded for shop: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to upload logo"))
            Log.e("ShopRepository", "Upload logo error", e)
        }
    }

    override fun uploadShopCover(shopId: String, imageUri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val coverBase64 = ImageUtils.uriToBase64(imageUri, contentResolver)
                ?: throw Exception("Failed to convert image")

            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "coverImage" to coverBase64,
                        "hasCover" to true,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(coverBase64))
            Log.d("ShopRepository", "Cover uploaded for shop: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to upload cover"))
            Log.e("ShopRepository", "Upload cover error", e)
        }
    }

    override fun removeShopLogo(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "logo" to "",
                        "hasLogo" to false,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Logo removed for shop: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove logo"))
            Log.e("ShopRepository", "Remove logo error", e)
        }
    }

    override fun removeShopCover(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            shopsCollection
                .document(shopId)
                .update(
                    mapOf(
                        "coverImage" to "",
                        "hasCover" to false,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Cover removed for shop: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove cover"))
            Log.e("ShopRepository", "Remove cover error", e)
        }
    }

    // ============= NEARBY SHOPS =============

    override fun getNearbyShops(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<Resource<List<Shop>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = shopsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val allShops = snapshot.toObjects(Shop::class.java)

            val nearbyShops = allShops
                .filter { shop ->
                    LocationUtils.isWithinRadius(
                        latitude, longitude,
                        shop.location.latitude, shop.location.longitude,
                        radiusInKm
                    )
                }
                .sortedBy { shop ->
                    LocationUtils.calculateDistance(
                        latitude, longitude,
                        shop.location.latitude, shop.location.longitude
                    )
                }

            emit(Resource.Success(nearbyShops))
            Log.d("ShopRepository", "Found ${nearbyShops.size} nearby shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get nearby shops"))
            Log.e("ShopRepository", "Nearby shops error", e)
        }
    }

    override fun getNearbyShopsByType(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double,
        shopType: ShopType
    ): Flow<Resource<List<Shop>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = shopsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val nearbyShops = snapshot.toObjects(Shop::class.java)
                .filter { shop ->
                    shop.shopType == shopType &&
                            LocationUtils.isWithinRadius(
                                latitude, longitude,
                                shop.location.latitude, shop.location.longitude,
                                radiusInKm
                            )
                }
                .sortedBy { shop ->
                    LocationUtils.calculateDistance(
                        latitude, longitude,
                        shop.location.latitude, shop.location.longitude
                    )
                }

            emit(Resource.Success(nearbyShops))
            Log.d("ShopRepository", "Found ${nearbyShops.size} nearby $shopType shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get nearby shops by type"))
            Log.e("ShopRepository", "Nearby shops by type error", e)
        }
    }

    // ============= SEARCH =============

    override fun searchShops(
        query: String,
        shopType: ShopType?
    ): Flow<Resource<List<Shop>>> = flow {
        emit(Resource.Loading)
        try {
            var baseQuery: Query = shopsCollection
                .whereEqualTo("isActive", true)

            if (shopType != null) {
                baseQuery = baseQuery.whereEqualTo("shopType", shopType)
            }

            val snapshot = baseQuery.get().await()
            val allShops = snapshot.toObjects(Shop::class.java)

            val results = allShops.filter { shop ->
                shop.shopName.contains(query, ignoreCase = true)
            }

            emit(Resource.Success(results))
            Log.d("ShopRepository", "Search found ${results.size} shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
            Log.e("ShopRepository", "Search error", e)
        }
    }

    // ============= CATEGORIES =============

    override fun getShopsByCategory(
        category: String,
        shopType: ShopType?
    ): Flow<Resource<List<Shop>>> = flow {
        emit(Resource.Loading)
        try {
            var baseQuery: Query = shopsCollection
                .whereEqualTo("category", category)
                .whereEqualTo("isActive", true)

            if (shopType != null) {
                baseQuery = baseQuery.whereEqualTo("shopType", shopType)
            }

            val snapshot = baseQuery.get().await()
            val shops = snapshot.toObjects(Shop::class.java)

            emit(Resource.Success(shops))
            Log.d("ShopRepository", "Found ${shops.size} shops in category $category")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shops by category"))
            Log.e("ShopRepository", "Get shops by category error", e)
        }
    }

    override fun getAllCategoriesWithType(): Flow<Resource<List<CategoryWithType>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = shopsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val shops = snapshot.toObjects(Shop::class.java)

            val categoriesMap = mutableMapOf<String, ShopType>()
            shops.forEach { shop ->
                if (!categoriesMap.containsKey(shop.category)) {
                    categoriesMap[shop.category] = shop.shopType
                }
            }

            val categories = categoriesMap.map { (categoryName, shopType) ->
                CategoryWithType(categoryName, shopType)
            }.sortedBy { it.categoryName }

            emit(Resource.Success(categories))
            Log.d("ShopRepository", "Found ${categories.size} unique categories")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get categories"))
            Log.e("ShopRepository", "Get categories error", e)
        }
    }

    override fun getProductCategoriesWithType(): Flow<Resource<List<CategoryWithType>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = shopsCollection
                .whereEqualTo("isActive", true)
                .whereEqualTo("shopType", ShopType.PRODUCT)
                .get()
                .await()

            val shops = snapshot.toObjects(Shop::class.java)

            val categories = shops
                .map { it.category }
                .distinct()
                .map { categoryName ->
                    CategoryWithType(categoryName, ShopType.PRODUCT)
                }
                .sortedBy { it.categoryName }

            emit(Resource.Success(categories))
            Log.d("ShopRepository", "Found ${categories.size} product categories")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get product categories"))
            Log.e("ShopRepository", "Get product categories error", e)
        }
    }

    override fun getServiceCategoriesWithType(): Flow<Resource<List<CategoryWithType>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = shopsCollection
                .whereEqualTo("isActive", true)
                .whereEqualTo("shopType", ShopType.SERVICE)
                .get()
                .await()

            val shops = snapshot.toObjects(Shop::class.java)

            val categories = shops
                .map { it.category }
                .distinct()
                .map { categoryName ->
                    CategoryWithType(categoryName, ShopType.SERVICE)
                }
                .sortedBy { it.categoryName }

            emit(Resource.Success(categories))
            Log.d("ShopRepository", "Found ${categories.size} service categories")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get service categories"))
            Log.e("ShopRepository", "Get service categories error", e)
        }
    }

    // ============= FAVORITES =============

    override fun addToFavorites(shop: Shop): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = getCurrentUserId()
            val favoriteData = mapOf(
                "userId" to userId,
                "shopId" to shop.shopId,
                "shopType" to shop.shopType,
                "shopName" to shop.shopName,
                "category" to shop.category,
                "addedAt" to Timestamp.now()
            )

            favoritesCollection
                .document("${userId}_${shop.shopId}")
                .set(favoriteData)
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Shop added to favorites: ${shop.shopId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add to favorites"))
            Log.e("ShopRepository", "Add to favorites error", e)
        }
    }

    override fun removeFromFavorites(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = getCurrentUserId()

            favoritesCollection
                .document("${userId}_${shopId}")
                .delete()
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "Shop removed from favorites: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove from favorites"))
            Log.e("ShopRepository", "Remove from favorites error", e)
        }
    }

    override fun getFavoriteShops(): Flow<Resource<List<Shop>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = getCurrentUserId()

            val favoritesSnapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val shopIds = favoritesSnapshot.documents.mapNotNull { it.getString("shopId") }

            if (shopIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val shops = shopsCollection
                .whereIn("shopId", shopIds)
                .get()
                .await()
                .toObjects(Shop::class.java)

            emit(Resource.Success(shops))
            Log.d("ShopRepository", "Found ${shops.size} favorite shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get favorite shops"))
            Log.e("ShopRepository", "Get favorite shops error", e)
        }
    }

    override fun getFavoriteShopsByType(shopType: ShopType): Flow<Resource<List<Shop>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = getCurrentUserId()

            val favoritesSnapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val shopIds = favoritesSnapshot.documents.mapNotNull { it.getString("shopId") }

            if (shopIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val shops = shopsCollection
                .whereIn("shopId", shopIds)
                .whereEqualTo("shopType", shopType)
                .get()
                .await()
                .toObjects(Shop::class.java)

            emit(Resource.Success(shops))
            Log.d("ShopRepository", "Found ${shops.size} favorite $shopType shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get favorite shops by type"))
            Log.e("ShopRepository", "Get favorite shops by type error", e)
        }
    }

    // ============= HELPER FUNCTIONS =============

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not logged in")
    }
}
