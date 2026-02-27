package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp

data class ShopItem(
    val itemId: String = "",
    val shopId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val itemType: ShopType = ShopType.PRODUCT,  // ✅ Using existing ShopType

    // Common fields
    val price: Double = 0.0,
    val discountedPrice: Double = 0.0,
    val images: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val isActive: Boolean = true,

    // PRODUCT-specific fields
    val sku: String = "",
    val brand: String = "",
    val stockQuantity: Int = 0,
    val unit: String = "piece",
    val minOrderQuantity: Int = 1,
    val maxOrderQuantity: Int = 100,
    val hasVariants: Boolean = false,

    // SERVICE-specific fields
    val duration: String = "",
    val isHomeService: Boolean = false,
    val requiresAppointment: Boolean = false,

    // Stats
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val totalSold: Int = 0,
    val totalBookings: Int = 0,

    // Timestamps
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
