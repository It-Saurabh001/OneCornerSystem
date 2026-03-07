package com.saurabh.onecornersystem.data.repository

import android.net.Uri
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow


interface ShopItemRepository {

    // ============= CREATE =============
    fun createItem(
        item: ShopItem,
        imageUri: Uri? = null
    ): Flow<Resource<ShopItem>>

    // ============= READ =============
    fun getItemById(itemId: String): Flow<Resource<ShopItem>>

    fun getItemsByShop(shopId: String): Flow<Resource<List<ShopItem>>>

    fun getItemsByShopAndType(
        shopId: String,
        itemType: ShopType
    ): Flow<Resource<List<ShopItem>>>

    // ============= SEARCH =============
    fun searchServices(
        query: String,
        shopType: ShopType = ShopType.SERVICE
    ): Flow<Resource<List<ShopItem>>>

    // ============= UPDATE =============
    fun updateItem(
        itemId: String,
        updates: Map<String, Any>
    ): Flow<Resource<Boolean>>

    fun updateItemAvailability(
        itemId: String,
        isAvailable: Boolean
    ): Flow<Resource<Boolean>>

    // For products
    fun updateStock(
        itemId: String,
        stockQuantity: Int
    ): Flow<Resource<Boolean>>

    // ============= DELETE =============
    fun deleteItem(itemId: String): Flow<Resource<Boolean>>

    // ============= IMAGES =============
    fun uploadItemImage(
        itemId: String,
        imageUri: Uri
    ): Flow<Resource<String>>

    fun removeItemImage(
        itemId: String,
        imageUrl: String
    ): Flow<Resource<Boolean>>
}