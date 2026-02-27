package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "customer", // "customer" ya "shop_owner"
    val shopType: ShopType? = null,  // null for customers
    val profileImage: String = "",
    val fcmToken: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp = Timestamp.now(),
    val isActive: Boolean = true // For shop owner active/inactive status
)

enum class ShopType {
    PRODUCT,    // Product-based shop (cloth store, grocery)
    SERVICE     // Service-based shop (mechanic, salon)
}
