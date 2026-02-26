package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "", // Reference to Customer
    val shopId: String = "", // Reference to Shop
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val totalAmount: Double = 0.0,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0,
    val status: String = "pending", // pending, accepted, rejected, preparing, ready, out_for_delivery, delivered, cancelled
    val paymentStatus: String = "pending", // pending, completed, failed, refunded
    val paymentMethod: String = "cash_on_delivery", // cash_on_delivery, card, upi, wallet
    val deliveryAddress: String = "",
    val deliveryCity: String = "",
    val deliveryPincode: String = "",
    val estimatedDelivery: Timestamp? = null,
    val deliveredAt: Timestamp? = null,
    val cancelledAt: Timestamp? = null,
    val cancellationReason: String = "",
    val specialInstructions: String = "",
    val itemCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class OrderItem(
    val orderItemId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val variantId: String = "", // Empty if no variant
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0, // Price at time of order
    val totalPrice: Double = 0.0,
    val unit: String = "piece",
    val createdAt: Timestamp = Timestamp.now()
)

data class OrderStatus(
    val orderId: String = "",
    val status: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class OrderTimeline(
    val orderId: String = "",
    val statuses: List<OrderStatus> = emptyList()
)

