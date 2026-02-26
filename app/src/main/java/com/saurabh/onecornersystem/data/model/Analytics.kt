package com.saurabh.onecornersystem.data.model

import com.google.firebase.Timestamp

data class Inventory(
    val inventoryId: String = "",
    val shopId: String = "",
    val totalProducts: Int = 0,
    val activeProducts: Int = 0,
    val inStockProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val outOfStockProducts: Int = 0,
    val lowStockThreshold: Int = 5, // Alert when stock falls below this
    val lastSyncTime: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class StockAlert(
    val alertId: String = "",
    val shopId: String = "",
    val productId: String = "",
    val productName: String = "",
    val currentStock: Int = 0,
    val threshold: Int = 5,
    val alertType: String = "low_stock", // low_stock, out_of_stock
    val isResolved: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val resolvedAt: Timestamp? = null
)

data class ShopAnalytics(
    val analyticsId: String = "",
    val shopId: String = "",
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val totalCustomers: Int = 0,
    val repeatCustomerRate: Double = 0.0,
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val peakHours: Map<String, Int> = emptyMap(), // hour (0-23) -> count
    val topProducts: List<ProductStat> = emptyList(),
    val weeklyStats: List<DailyStat> = emptyList(),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class ProductStat(
    val productId: String = "",
    val productName: String = "",
    val orderCount: Int = 0,
    val quantitySold: Int = 0,
    val revenue: Double = 0.0,
    val rating: Double = 0.0
)

data class DailyStat(
    val date: String = "", // YYYY-MM-DD format
    val orderCount: Int = 0,
    val revenue: Double = 0.0,
    val customerCount: Int = 0,
    val averageOrderValue: Double = 0.0
)

data class MonthlyStat(
    val month: String = "", // YYYY-MM format
    val orderCount: Int = 0,
    val revenue: Double = 0.0,
    val customerCount: Int = 0,
    val averageOrderValue: Double = 0.0
)

data class ShopDashboard(
    val shopId: String = "",
    val shopName: String = "",
    val totalOrders: Int = 0,
    val todayOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val todayRevenue: Double = 0.0,
    val activeProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val averageRating: Double = 0.0,
    val pendingOrders: Int = 0,
    val totalCustomers: Int = 0,
    val lastUpdated: Timestamp = Timestamp.now()
)

data class CustomerMetrics(
    val totalCustomers: Int = 0,
    val newCustomersThisMonth: Int = 0,
    val repeatCustomers: Int = 0,
    val repeatCustomerRate: Double = 0.0,
    val averageCustomerValue: Double = 0.0,
    val totalReturningCustomers: Int = 0
)

data class CategoryStat(
    val category: String = "",
    val orderCount: Int = 0,
    val revenue: Double = 0.0,
    val percentageOfTotal: Double = 0.0
)

data class CustomReport(
    val reportId: String = "",
    val shopId: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val totalCustomers: Int = 0,
    val topProducts: List<ProductStat> = emptyList(),
    val dailyStats: List<DailyStat> = emptyList(),
    val generatedAt: Timestamp = Timestamp.now()
)
