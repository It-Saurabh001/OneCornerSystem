package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp

data class Product(
    val productId: String = "",
    val shopId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val sku: String = "", // Stock Keeping Unit - unique identifier
    val brand: String = "",
    val price: Double = 0.0,
    val discountedPrice: Double = 0.0,
    val images: List<String> = emptyList(),
    val stockQuantity: Int = 0,
    val unit: String = "piece", // piece, kg, dozen, etc
    val minOrderQuantity: Int = 1,
    val maxOrderQuantity: Int = 100,
    val hasVariants: Boolean = false,
    val isAvailable: Boolean = true,
    val isActive: Boolean = true,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val totalSold: Int = 0, // Track units sold for analytics
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class ProductVariant(
    val variantId: String = "",
    val productId: String = "",
    val shopId: String = "",
    val skuVariant: String = "", // e.g., "PROD001-RED-M"
    val variantName: String = "", // e.g., "Red - Medium"
    val variantAttributes: Map<String, String> = emptyMap(), // e.g., {"color": "red", "size": "M"}
    val price: Double = 0.0,
    val stockQuantity: Int = 0,
    val stockStatus: String = "in_stock", // in_stock, low_stock, out_of_stock
    val isActive: Boolean = true,
    val images: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class ProductReview(
    val reviewId: String = "",
    val productId: String = "",
    val shopId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5, // 1-5 stars
    val title: String = "",
    val comment: String = "",
    val images: List<String> = emptyList(),
    val helpful: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
