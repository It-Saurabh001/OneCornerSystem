package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

/**
 * Service Location Enum
 */
enum class ServiceLocation {
    CUSTOMER_HOME,  // Customer के घर पर service
    SHOP_LOCATION   // Shop पर जाकर service लेनी होगी
}

/**
 * Booking Model for Service-Based Shop
 * Used when customer books a service from shop owner
 */
data class Booking(
    val bookingId: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val shopType: ShopType? = null,
    val serviceId: String = "",
    val serviceName: String = "",
    val servicePrice: Double = 0.0,
    val serviceDuration: String = "",

    // Customer Info
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",

    // Shop Owner Info
    val shopOwnerId: String = "",

    // Booking Details
    val bookingDate: String = "",  // Format: "2026-03-06"
    val bookingTime: String = "",  // Format: "10:00 AM"
    val timeSlot: TimeSlot? = null,
    val duration: String = "",     // Service duration e.g., "30 mins"

    // Location (Updated)
    val serviceLocation: ServiceLocation = ServiceLocation.SHOP_LOCATION,
    val homeService: Boolean = false,  // Whether it's a home service
    val serviceAddress: String = "",     // Customer's address for home service
    val customerCity: String = "",
    val customerPincode: String = "",
    val customerLocation: GeoPoint? = null,
    val shopAddress: String = "",        // Shop address

    // Status
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val totalAmount: Double = 0.0,

    // Additional Info
    val notes: String = "",           // Customer notes/special requests
    val cancellationReason: String = "",
    val cancelledBy: String = "",      // "customer" or "shop_owner"
    val shopResponseNotes: String = "",

    // Timestamps
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null,
    val completedAt: Timestamp? = null
)

/**
 * Time Slot for booking
 */
data class TimeSlot(
    val slotId: String = "",
    val startTime: String = "",   // "09:00"
    val endTime: String = "",     // "09:30"
    val isAvailable: Boolean = true,
    val date: String = ""         // "2026-03-06"
)

/**
 * Booking Status Enum
 */
enum class BookingStatus {
    PENDING,      // Customer ne book kiya, shop owner ne abhi accept nahi kiya
    CONFIRMED,    // Shop owner ne accept kiya
    IN_PROGRESS,  // Service chal rahi hai
    COMPLETED,    // Service complete ho gayi
    CANCELLED,    // Cancelled by customer or shop owner
    REJECTED,     // Shop owner ne reject kiya
    NO_SHOW       // Customer nahi aaya
}

/**
 * Payment Status Enum
 */
enum class PaymentStatus {
    PENDING,      // Payment nahi hua
    PARTIAL,      // Partial payment (advance)
    COMPLETED,    // Full payment done
    REFUNDED,     // Refund ho gaya
    FAILED        // Payment failed
}

/**
 * Available Time Slots for a day
 */
data class DaySchedule(
    val date: String = "",
    val dayOfWeek: String = "",
    val slots: List<TimeSlot> = emptyList(),
    val isHoliday: Boolean = false
)