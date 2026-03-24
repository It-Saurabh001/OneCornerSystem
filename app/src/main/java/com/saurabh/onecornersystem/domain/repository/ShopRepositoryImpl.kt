package com.saurabh.onecornersystem.domain.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.Booking
import com.saurabh.onecornersystem.data.model.BookingStatus
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.PaymentStatus
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.model.TimeSlot
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
    private val bookingsCollection = firestore.collection("bookings")
    private val timeSlotsCollection = firestore.collection("time_slots")

    // ============= SHOP CRUD (8) =============

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
            Log.d("ShopRepository", "✅ Shop created: ${shopWithId.shopId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create shop"))
            Log.e("ShopRepository", "❌ Create shop error", e)
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
            Log.d("ShopRepository", "✅ Shop created with images: ${updatedShop.shopId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create shop with images"))
            Log.e("ShopRepository", "❌ Create shop with images error", e)
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
                val mapped = shop.copy(shopId = shopDoc.id)
                emit(Resource.Success(mapped))
            } else {
                emit(Resource.Error("Shop not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop details"))
            Log.e("ShopRepository", "❌ Get shop details error", e)
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
            Log.d("ShopRepository", " Shop profile updated: $shopId")
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
            Log.e("ShopRepository", " Update shop status error", e)
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
            Log.d("ShopRepository", "✅ Shop soft deleted: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete shop"))
            Log.e("ShopRepository", "❌ Delete shop error", e)
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

    // ============= SHOP STATS & RATING (3) =============

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
            Log.d("ShopRepository", " Shop stats updated: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update shop stats"))
            Log.e("ShopRepository", "Update shop stats error", e)
        }
    }

    // ============= IMAGES (6) =============

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
            Log.e("ShopRepository", " Upload cover error", e)
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

    // ============= LOCATION & SEARCH (5) =============

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
            Log.d("ShopRepository", " Search found ${results.size} shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
            Log.e("ShopRepository", " Search error", e)
        }
    }

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
            Log.e("ShopRepository", " Get shops by category error", e)
        }
    }

    // ============= CATEGORIES (3) =============

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
            Log.d("ShopRepository", "📚 Found ${categories.size} product categories")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get product categories"))
            Log.e("ShopRepository", "❌ Get product categories error", e)
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

    // ============= FAVORITES (4) =============

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
            Log.e("ShopRepository", " Add to favorites error", e)
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
            Log.d("ShopRepository", " Shop removed from favorites: $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to remove from favorites"))
            Log.e("ShopRepository", " Remove from favorites error", e)
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
            Log.e("ShopRepository", " Get favorite shops error", e)
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
            Log.d("ShopRepository", " Found ${shops.size} favorite $shopType shops")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get favorite shops by type"))
            Log.e("ShopRepository", " Get favorite shops by type error", e)
        }
    }

    // ============= BOOKING FUNCTIONS (12) =============

    override fun createBooking(booking: Booking): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading)
        try {
            val bookingWithId = booking.copy(
                bookingId = bookingsCollection.document().id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            bookingsCollection
                .document(bookingWithId.bookingId)
                .set(bookingWithId)
                .await()

            emit(Resource.Success(bookingWithId))
            Log.d("ShopRepository", " Booking created: ${bookingWithId.bookingId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create booking"))
            Log.e("ShopRepository", " Create booking error", e)
        }
    }

    override fun getBookingById(bookingId: String): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading)
        try {
            val doc = bookingsCollection.document(bookingId).get().await()
            val booking = doc.toObject(Booking::class.java)
            if (booking != null) {
                emit(Resource.Success(booking))
            } else {
                emit(Resource.Error("Booking not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get booking"))
            Log.e("ShopRepository", " Get booking error", e)
        }
    }

    override fun getBookingsByShop(shopId: String): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = bookingsCollection
                .whereEqualTo("shopId", shopId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val bookings = snapshot.toObjects(Booking::class.java)
            emit(Resource.Success(bookings))
            Log.d("ShopRepository", " Found ${bookings.size} bookings for shop $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop bookings"))
            Log.e("ShopRepository", " Get shop bookings error", e)
        }
    }

    override fun getBookingsByShopAndStatus(
        shopId: String,
        status: BookingStatus
    ): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = bookingsCollection
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("status", status.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val bookings = snapshot.toObjects(Booking::class.java)
            emit(Resource.Success(bookings))
            Log.d("ShopRepository", "📋 Found ${bookings.size} $status bookings for shop $shopId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get bookings by status"))
            Log.e("ShopRepository", "❌ Get bookings by status error", e)
        }
    }

    override fun getBookingsByCustomer(customerId: String): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = bookingsCollection
                .whereEqualTo("customerId", customerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val bookings = snapshot.toObjects(Booking::class.java)
            emit(Resource.Success(bookings))
            Log.d("ShopRepository", "📋 Found ${bookings.size} bookings for customer $customerId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get customer bookings"))
            Log.e("ShopRepository", "❌ Get customer bookings error", e)
        }
    }

    override fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "updatedAt" to Timestamp.now()
            )

            if (status == BookingStatus.COMPLETED) {
                updates["completedAt"] = Timestamp.now()
            }

            bookingsCollection
                .document(bookingId)
                .update(updates)
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "✅ Booking $bookingId status updated to $status")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update booking status"))
            Log.e("ShopRepository", "❌ Update booking status error", e)
        }
    }

    override fun updateBookingPaymentStatus(
        bookingId: String,
        paymentStatus: PaymentStatus
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            bookingsCollection
                .document(bookingId)
                .update(
                    mapOf(
                        "paymentStatus" to paymentStatus.name,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "✅ Booking $bookingId payment status updated to $paymentStatus")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update payment status"))
            Log.e("ShopRepository", "❌ Update payment status error", e)
        }
    }

    override fun updateBookingNotes(
        bookingId: String,
        shopResponseNotes: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            bookingsCollection
                .document(bookingId)
                .update(
                    mapOf(
                        "shopResponseNotes" to shopResponseNotes,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "✅ Booking $bookingId notes updated")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update notes"))
            Log.e("ShopRepository", "❌ Update notes error", e)
        }
    }

    override fun cancelBooking(
        bookingId: String,
        reason: String,
        cancelledBy: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            bookingsCollection
                .document(bookingId)
                .update(
                    mapOf(
                        "status" to BookingStatus.CANCELLED.name,
                        "cancellationReason" to reason,
                        "cancelledBy" to cancelledBy,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ShopRepository", "✅ Booking $bookingId cancelled by $cancelledBy")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to cancel booking"))
            Log.e("ShopRepository", "❌ Cancel booking error", e)
        }
    }

    override fun getAvailableTimeSlots(
        shopId: String,
        date: String
    ): Flow<Resource<List<TimeSlot>>> = flow {
        emit(Resource.Loading)
        try {
            val shopDoc = shopsCollection.document(shopId).get().await()
            val shop = shopDoc.toObject(Shop::class.java)

            if (shop == null) {
                emit(Resource.Error("Shop not found"))
                return@flow
            }

            val existingBookings = bookingsCollection
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("bookingDate", date)
                .whereIn("status", listOf(
                    BookingStatus.PENDING.name,
                    BookingStatus.CONFIRMED.name,
                    BookingStatus.IN_PROGRESS.name
                ))
                .get()
                .await()
                .toObjects(Booking::class.java)

            val bookedSlots = existingBookings.map { it.bookingTime }

            val slots = generateTimeSlots(
                openingTime = shop.openingTime,
                closingTime = shop.closingTime,
                date = date,
                bookedSlots = bookedSlots
            )

            emit(Resource.Success(slots))
            Log.d("ShopRepository", "⏰ Generated ${slots.size} time slots for $date")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get time slots"))
            Log.e("ShopRepository", "❌ Get time slots error", e)
        }
    }

    override fun listenToShopBookings(shopId: String): Flow<Resource<List<Booking>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = bookingsCollection
            .whereEqualTo("shopId", shopId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen to bookings"))
                    return@addSnapshotListener
                }

                val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                trySend(Resource.Success(bookings))
                Log.d("ShopRepository", "👂 Real-time: ${bookings.size} bookings for shop $shopId")
            }

        awaitClose { listener.remove() }
    }

    override fun listenToCustomerBookings(customerId: String): Flow<Resource<List<Booking>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = bookingsCollection
            .whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen to bookings"))
                    return@addSnapshotListener
                }

                val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                trySend(Resource.Success(bookings))
                Log.d("ShopRepository", "👂 Real-time: ${bookings.size} bookings for customer $customerId")
            }

        awaitClose { listener.remove() }
    }

    // ============= HELPER FUNCTIONS =============

    private fun generateTimeSlots(
        openingTime: String,
        closingTime: String,
        date: String,
        bookedSlots: List<String>,
        slotDurationMinutes: Int = 30
    ): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()

        try {
            val openHour = openingTime.split(":")[0].toInt()
            val openMinute = openingTime.split(":")[1].toInt()
            val closeHour = closingTime.split(":")[0].toInt()
            val closeMinute = closingTime.split(":")[1].toInt()

            var currentHour = openHour
            var currentMinute = openMinute

            while (currentHour < closeHour || (currentHour == closeHour && currentMinute < closeMinute)) {
                val startTime = String.format("%02d:%02d", currentHour, currentMinute)

                var endHour = currentHour
                var endMinute = currentMinute + slotDurationMinutes
                if (endMinute >= 60) {
                    endHour += 1
                    endMinute -= 60
                }
                val endTime = String.format("%02d:%02d", endHour, endMinute)

                val displayTime = formatTo12Hour(startTime)
                val isBooked = bookedSlots.contains(displayTime) || bookedSlots.contains(startTime)

                slots.add(
                    TimeSlot(
                        slotId = "${date}_$startTime",
                        startTime = startTime,
                        endTime = endTime,
                        isAvailable = !isBooked,
                        date = date
                    )
                )

                currentMinute += slotDurationMinutes
                if (currentMinute >= 60) {
                    currentHour += 1
                    currentMinute -= 60
                }
            }
        } catch (e: Exception) {
            Log.e("ShopRepository", "❌ Error generating time slots", e)
        }

        return slots
    }

    private fun formatTo12Hour(time24: String): String {
        try {
            val parts = time24.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1]
            val amPm = if (hour >= 12) "PM" else "AM"

            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12

            return String.format("%d:%s %s", hour, minute, amPm)
        } catch (e: Exception) {
            return time24
        }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not logged in")
    }
}