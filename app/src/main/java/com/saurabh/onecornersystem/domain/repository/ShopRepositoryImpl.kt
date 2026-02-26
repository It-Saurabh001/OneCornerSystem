package com.saurabh.onecornersystem.domain.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.utils.ImageUtils
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShopRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentResolver: ContentResolver
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


    override fun createShopWithImages(
        shop: Shop,
        logoUri: Uri?,
        coverUri: Uri?
    ): Flow<Resource<Shop>> = flow {
        emit(Resource.Loading)
        try {
            var updatedShop = shop.copy(
                shopId = firestore.collection("shops").document().id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                hasLogo = false,
                hasCover = false
            )

            // Convert logo to Base64 if provided
            if (logoUri != null) {
                val logoBase64 = ImageUtils.uriToBase64(logoUri, contentResolver)
                if (logoBase64 != null) {
                    updatedShop = updatedShop.copy(
                        logo = logoBase64,
                        hasLogo = true
                    )
                }
            }

            // Convert cover to Base64 if provided
            if (coverUri != null) {
                val coverBase64 = ImageUtils.uriToBase64(coverUri, contentResolver)
                if (coverBase64 != null) {
                    updatedShop = updatedShop.copy(
                        coverImage = coverBase64,
                        hasCover = true
                    )
                }
            }

            // Save to Firestore
            firestore.collection("shops")
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


    override fun uploadShopLogo(shopId: String, imageUri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            // Convert to Base64
            val logoBase64 = ImageUtils.uriToBase64(imageUri, contentResolver)
                ?: throw Exception("Failed to convert image")

            // Update Firestore
            firestore.collection("shops")
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
            // Convert to Base64
            val coverBase64 = ImageUtils.uriToBase64(imageUri, contentResolver)
                ?: throw Exception("Failed to convert image")

            // Update Firestore
            firestore.collection("shops")
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

    override fun listenToShopDetails(shopId: String): Flow<Resource<Shop>> = callbackFlow {

        var listener : ListenerRegistration? = null

        try {
            listener = firestore.collection("shops")
                .document(shopId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Listen Failed"))
                        Log.e("ShopRepository", "Listen to shop details error", error)
                        return@addSnapshotListener
                    }
                    try {
                        when{
                            snapshot == null -> {
                                trySend(Resource.Error("Null Snapshot received"))
                            }
                            !snapshot.exists()-> {
                                trySend(Resource.Error("Shop not found"))
                            }
                            else->{
                                val shop = snapshot.toObject(Shop::class.java)
                                if (shop != null){

                                    trySend(Resource.Success(shop))
                                }else{
                                    trySend(Resource.Error("Shop data is invalid"))
                                }
                            }
                        }
                    }catch (e: Exception){
                        trySend(Resource.Error(e.message ?: "Listen Failed Data processing error"))
                        Log.e("ShopRepository", "Listen to shop data  details error", e)
                    }
                }
            awaitClose {
                try {
                    listener?.remove()
                    Log.d("ShopRepository", "Shop details listener removed")
                } catch (e: Exception) {
                    Log.e("ShopRepository", "Error removing listener",e)
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


    override fun removeShopLogo(shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("shops")
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
            firestore.collection("shops")
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


}

