package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Shop(
    val shopId: String = "",
    val ownerId: String = "",
    val shopName: String = "",
    val category: String = "",
    val description: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val address: String = "",
    val city: String = "",
    val pincode: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val logo: String = "",
    val coverImage: String = "",
    val openingTime: String = "09:00",
    val closingTime: String = "21:00",
    val isOpen: Boolean = true,
    val isActive: Boolean = true,
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val operatingHours: Map<String, OperatingHour> = emptyMap(),
    val hasLogo: Boolean = false,
    val hasCover: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class OperatingHour(
    val dayOfWeek: String = "", // MON, TUE, WED, etc.
    val openTime: String = "09:00", // HH:mm format
    val closeTime: String = "21:00", // HH:mm format
    val isClosed: Boolean = false
)

data class ShopCategory(
    val categoryId: String = "",
    val categoryName: String = "",
    val description: String = "",
    val icon: String = "" // URL or drawable reference
)

