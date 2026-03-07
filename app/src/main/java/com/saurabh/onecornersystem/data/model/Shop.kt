package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName


data class Shop(
    val shopId: String = "",
    val ownerId: String = "",
    val shopName: String = "",
    val shopType: ShopType = ShopType.PRODUCT,
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
    @get:PropertyName("isOpen") @set:PropertyName("isOpen")
    var open: Boolean = true,
    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var active: Boolean = true,
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val totalItems: Int = 0,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val operatingHours: Map<String, OperatingHour> = emptyMap(),
    val hasLogo: Boolean = false,
    val hasCover: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
){
    fun toFirestoreMap() : Map<String,Any>{
        return mapOf(
            "isActive" to active,  // Sirf isActive
            "isOpen" to open       // Sirf isOpen
        )
    }
}

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

data class CategoryWithType(
    val categoryName: String,
    val shopType: ShopType
)

