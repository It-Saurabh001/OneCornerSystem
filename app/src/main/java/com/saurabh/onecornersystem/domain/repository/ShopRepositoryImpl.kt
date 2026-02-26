package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShopRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ShopRepository {

    override fun createShop(shop: Shop): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            val shopWithId = shop.copy(shopId = firestore.collection("shops").document().id)
            firestore.collection("shops")
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

    override fun getShopDetails(shopId: String): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            val shopDoc = firestore.collection("shops")
                .document(shopId)
                .get()
                .await()

            val shop = shopDoc.toObject(Shop::class.java)
            if (shop != null) {
                emit(Resource.Success(shop))
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
            val querySnapshot = firestore.collection("shops")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val shop = querySnapshot.documents[0].toObject(Shop::class.java)
                if (shop != null) {
                    emit(Resource.Success(shop))
                } else {
                    emit(Resource.Error("Shop data is invalid"))
                }
            } else {
                emit(Resource.Error("Shop not found for owner"))
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
            updateData["updatedAt"] = com.google.firebase.Timestamp.now()

            firestore.collection("shops")
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
            firestore.collection("shops")
                .document(shopId)
                .update(
                    mapOf(
                        "isActive" to isActive,
                        "updatedAt" to com.google.firebase.Timestamp.now()
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
            val shopDoc = firestore.collection("shops")
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

    override fun listenToShopDetails(shopId: String): Flow<Shop?> = flow {
        try {
            firestore.collection("shops")
                .document(shopId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ShopRepository", "Listen to shop details error", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val shop = snapshot.toObject(Shop::class.java)
                        // Emit inside coroutine scope
                        // This is a limitation of Flow - we'll use a MutableStateFlow in ViewModel instead
                    }
                }
        } catch (e: Exception) {
            Log.e("ShopRepository", "Listen to shop details error", e)
        }
    }

    override fun deleteShop(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            // Soft delete - mark as inactive
            firestore.collection("shops")
                .document(shopId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to com.google.firebase.Timestamp.now()
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
        totalProducts: Int,
        totalOrders: Int,
        totalRevenue: Double
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("shops")
                .document(shopId)
                .update(
                    mapOf(
                        "totalProducts" to totalProducts,
                        "totalOrders" to totalOrders,
                        "totalRevenue" to totalRevenue,
                        "updatedAt" to com.google.firebase.Timestamp.now()
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
}

