package com.saurabh.onecornersystem.data.repository

import android.net.Uri
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ShopRepository {

    fun createShop(shop: Shop): Flow<Resource<Shop>>

    fun createShopWithImages( shop: Shop,logoUri: Uri? = null, coverUri: Uri? = null ): Flow<Resource<Shop>>

    fun searchShops(query: String, shopType: ShopType? = null ): Flow<Resource<List<Shop>>>

    fun getShopDetails(shopId: String): Flow<Resource<Shop>>

    fun getShopByOwner(ownerId: String): Flow<Resource<Shop>>

    fun updateShopProfile(shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>

    fun updateShopActiveStatus(shopId: String, isActive: Boolean): Flow<Resource<Boolean>>

    fun getShopRating(shopId: String): Flow<Resource<Double>>

    fun listenToShopDetails(shopId: String): Flow<Resource<Shop>>

    fun deleteShop(shopId: String): Flow<Resource<Boolean>>

    fun updateShopStats(
        shopId: String,
        totalItems: Int,        // ✅ Changed parameter name
        totalOrders: Int,
        totalRevenue: Double
    ): Flow<Resource<Boolean>>
    fun uploadShopLogo(shopId: String, imageUri: Uri): Flow<Resource<String>>

    fun uploadShopCover(shopId: String, imageUri: Uri): Flow<Resource<String>>

    fun removeShopLogo(shopId: String): Flow<Resource<Boolean>>

    fun removeShopCover(shopId: String): Flow<Resource<Boolean>>

    fun getNearbyShops(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<Resource<List<Shop>>>

    fun getNearbyShopsByType(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double,
        shopType: ShopType
    ): Flow<Resource<List<Shop>>>

    fun getShopsByCategory(
        category: String,
        shopType: ShopType? = null
    ): Flow<Resource<List<Shop>>>

    fun getAllCategoriesWithType(): Flow<Resource<List<CategoryWithType>>>

    fun getProductCategoriesWithType(): Flow<Resource<List<CategoryWithType>>>

    fun getServiceCategoriesWithType(): Flow<Resource<List<CategoryWithType>>>

    fun addToFavorites(shop: Shop): Flow<Resource<Boolean>>

    fun removeFromFavorites(shopId: String): Flow<Resource<Boolean>>

    fun getFavoriteShops(): Flow<Resource<List<Shop>>>

    fun getFavoriteShopsByType(shopType: ShopType): Flow<Resource<List<Shop>>>



}

