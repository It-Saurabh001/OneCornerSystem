package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ShopRepository {

    fun createShop(shop: Shop): Flow<Resource<Shop>>

    fun getShopDetails(shopId: String): Flow<Resource<Shop>>

    fun getShopByOwner(ownerId: String): Flow<Resource<Shop>>

    fun updateShopProfile(shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>>

    fun updateShopActiveStatus(shopId: String, isActive: Boolean): Flow<Resource<Boolean>>

    fun getShopRating(shopId: String): Flow<Resource<Double>>

    fun listenToShopDetails(shopId: String): Flow<Resource<Shop>>

    fun deleteShop(shopId: String): Flow<Resource<Boolean>>

    fun updateShopStats(shopId: String, totalProducts: Int, totalOrders: Int, totalRevenue: Double): Flow<Resource<Boolean>>
}

