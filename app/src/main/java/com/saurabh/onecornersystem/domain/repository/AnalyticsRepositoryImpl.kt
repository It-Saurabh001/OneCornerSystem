package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.saurabh.onecornersystem.data.model.*
import com.saurabh.onecornersystem.data.repository.AnalyticsRepository
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnalyticsRepository {

    override fun getShopAnalytics(shopId: String): Flow<Resource<ShopAnalytics>> = flow {
        emit(Resource.Loading)
        try {
            val analyticsDoc = firestore.collection("shopAnalytics").document(shopId).get().await()
            val analytics = analyticsDoc.toObject(ShopAnalytics::class.java)
            if (analytics != null) {
                emit(Resource.Success(analytics))
            } else {
                emit(Resource.Success(ShopAnalytics(shopId = shopId)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop analytics"))
            Log.e("AnalyticsRepository", "Get analytics error", e)
        }
    }

    override fun getShopDashboard(shopId: String): Flow<Resource<ShopDashboard>> = flow {
        emit(Resource.Loading)
        try {
            val shopDoc = firestore.collection("shops").document(shopId).get().await()
            val shop = shopDoc.toObject(Shop::class.java)

            val today = System.currentTimeMillis()
            val startOfDay = (today / 86400000) * 86400000

            val todayOrdersSnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startOfDay / 1000, 0))
                .get().await()

            val todayOrders = todayOrdersSnapshot.size()
            var todayRevenue = 0.0
            var pendingOrders = 0

            for (doc in todayOrdersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    todayRevenue += order.totalAmount
                    if (order.status == "pending") pendingOrders++
                }
            }

            val lowStockSnapshot = firestore.collection("shops").document(shopId)
                .collection("products")
                .whereLessThanOrEqualTo("stockQuantity", 5)
                .whereEqualTo("isActive", true)
                .get().await()

            val dashboard = ShopDashboard(
                shopId = shopId,
                shopName = shop?.shopName ?: "Shop",
                totalOrders = shop?.totalOrders ?: 0,
                todayOrders = todayOrders,
                totalRevenue = shop?.totalRevenue ?: 0.0,
                todayRevenue = todayRevenue,
                activeProducts = shop?.totalProducts ?: 0,
                lowStockProducts = lowStockSnapshot.size(),
                averageRating = shop?.rating ?: 0.0,
                pendingOrders = pendingOrders,
                lastUpdated = com.google.firebase.Timestamp.now()
            )

            emit(Resource.Success(dashboard))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get dashboard"))
            Log.e("AnalyticsRepository", "Get dashboard error", e)
        }
    }

    override fun getTodayStats(shopId: String): Flow<Resource<DailyStat>> = flow {
        emit(Resource.Loading)
        try {
            val today = System.currentTimeMillis()
            val startOfDay = (today / 86400000) * 86400000
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(Date(today))

            val ordersSnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startOfDay / 1000, 0))
                .get().await()

            var revenue = 0.0
            val customers = mutableSetOf<String>()

            for (doc in ordersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    revenue += order.totalAmount
                    customers.add(order.userId)
                }
            }

            val stat = DailyStat(
                date = dateStr,
                orderCount = ordersSnapshot.size(),
                revenue = revenue,
                customerCount = customers.size,
                averageOrderValue = if (ordersSnapshot.size() > 0) revenue / ordersSnapshot.size() else 0.0
            )

            emit(Resource.Success(stat))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get today stats"))
            Log.e("AnalyticsRepository", "Get today stats error", e)
        }
    }

    override fun getWeeklyStats(shopId: String): Flow<Resource<List<DailyStat>>> = flow {
        emit(Resource.Loading)
        try {
            val stats = mutableListOf<DailyStat>()
            val today = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (dayOffset in 6 downTo 0) {
                val dayTime = today - (dayOffset * 86400000)
                val startOfDay = (dayTime / 86400000) * 86400000
                val endOfDay = startOfDay + 86400000

                val ordersSnapshot = firestore.collection("orders")
                    .whereEqualTo("shopId", shopId)
                    .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startOfDay / 1000, 0))
                    .whereLessThan("createdAt", com.google.firebase.Timestamp(endOfDay / 1000, 0))
                    .get().await()

                var revenue = 0.0
                val customers = mutableSetOf<String>()

                for (doc in ordersSnapshot.documents) {
                    val order = doc.toObject(Order::class.java)
                    if (order != null) {
                        revenue += order.totalAmount
                        customers.add(order.userId)
                    }
                }

                val stat = DailyStat(
                    date = dateFormat.format(Date(startOfDay)),
                    orderCount = ordersSnapshot.size(),
                    revenue = revenue,
                    customerCount = customers.size,
                    averageOrderValue = if (ordersSnapshot.size() > 0) revenue / ordersSnapshot.size() else 0.0
                )
                stats.add(stat)
            }

            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get weekly stats"))
            Log.e("AnalyticsRepository", "Get weekly stats error", e)
        }
    }

    override fun getMonthlyStats(shopId: String): Flow<Resource<List<DailyStat>>> = flow {
        emit(Resource.Loading)
        try {
            val stats = mutableListOf<DailyStat>()
            val today = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (dayOffset in 29 downTo 0) {
                val dayTime = today - (dayOffset * 86400000)
                val startOfDay = (dayTime / 86400000) * 86400000
                val endOfDay = startOfDay + 86400000

                val ordersSnapshot = firestore.collection("orders")
                    .whereEqualTo("shopId", shopId)
                    .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startOfDay / 1000, 0))
                    .whereLessThan("createdAt", com.google.firebase.Timestamp(endOfDay / 1000, 0))
                    .get().await()

                var revenue = 0.0
                val customers = mutableSetOf<String>()

                for (doc in ordersSnapshot.documents) {
                    val order = doc.toObject(Order::class.java)
                    if (order != null) {
                        revenue += order.totalAmount
                        customers.add(order.userId)
                    }
                }

                val stat = DailyStat(
                    date = dateFormat.format(Date(startOfDay)),
                    orderCount = ordersSnapshot.size(),
                    revenue = revenue,
                    customerCount = customers.size,
                    averageOrderValue = if (ordersSnapshot.size() > 0) revenue / ordersSnapshot.size() else 0.0
                )
                stats.add(stat)
            }

            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get monthly stats"))
            Log.e("AnalyticsRepository", "Get monthly stats error", e)
        }
    }

    override fun getYearlyStats(shopId: String): Flow<Resource<List<MonthlyStat>>> = flow {
        emit(Resource.Loading)
        try {
            val stats = mutableListOf<MonthlyStat>()
            val calendar = Calendar.getInstance()

            for (monthOffset in 11 downTo 0) {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, -monthOffset)

                val monthStart = calendar.timeInMillis / 1000
                calendar.add(Calendar.MONTH, 1)
                val monthEnd = calendar.timeInMillis / 1000

                val ordersSnapshot = firestore.collection("orders")
                    .whereEqualTo("shopId", shopId)
                    .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(monthStart, 0))
                    .whereLessThan("createdAt", com.google.firebase.Timestamp(monthEnd, 0))
                    .get().await()

                var revenue = 0.0
                val customers = mutableSetOf<String>()

                for (doc in ordersSnapshot.documents) {
                    val order = doc.toObject(Order::class.java)
                    if (order != null) {
                        revenue += order.totalAmount
                        customers.add(order.userId)
                    }
                }

                val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val stat = MonthlyStat(
                    month = dateFormat.format(Date(monthStart * 1000)),
                    orderCount = ordersSnapshot.size(),
                    revenue = revenue,
                    customerCount = customers.size,
                    averageOrderValue = if (ordersSnapshot.size() > 0) revenue / ordersSnapshot.size() else 0.0
                )
                stats.add(stat)
            }

            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get yearly stats"))
            Log.e("AnalyticsRepository", "Get yearly stats error", e)
        }
    }

    override fun getTopProducts(shopId: String, limit: Int): Flow<Resource<List<ProductStat>>> = flow {
        emit(Resource.Loading)
        try {
            val productsSnapshot = firestore.collection("shops").document(shopId).collection("products")
                .whereEqualTo("isActive", true)
                .orderBy("totalSold", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            val topProducts = productsSnapshot.documents.mapNotNull { doc ->
                val product = doc.toObject(Product::class.java)
                product?.let {
                    ProductStat(
                        productId = it.productId,
                        productName = it.name,
                        quantitySold = it.totalSold,
                        revenue = it.price * it.totalSold,
                        rating = it.rating
                    )
                }
            }

            emit(Resource.Success(topProducts))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get top products"))
            Log.e("AnalyticsRepository", "Get top products error", e)
        }
    }

    override fun getCustomerMetrics(shopId: String): Flow<Resource<CustomerMetrics>> = flow {
        emit(Resource.Loading)
        try {
            val ordersSnapshot = firestore.collection("orders").whereEqualTo("shopId", shopId).get().await()

            val customers = mutableMapOf<String, Int>()
            var totalRevenue = 0.0

            for (doc in ordersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    customers[order.userId] = customers.getOrDefault(order.userId, 0) + 1
                    totalRevenue += order.totalAmount
                }
            }

            val totalCustomers = customers.size
            val repeatCustomers = customers.values.count { it > 1 }
            val repeatCustomerRate = if (totalCustomers > 0) (repeatCustomers.toDouble() / totalCustomers) * 100 else 0.0
            val averageCustomerValue = if (totalCustomers > 0) totalRevenue / totalCustomers else 0.0

            val metrics = CustomerMetrics(
                totalCustomers = totalCustomers,
                repeatCustomers = repeatCustomers,
                repeatCustomerRate = repeatCustomerRate,
                averageCustomerValue = averageCustomerValue,
                totalReturningCustomers = repeatCustomers
            )

            emit(Resource.Success(metrics))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get customer metrics"))
            Log.e("AnalyticsRepository", "Get customer metrics error", e)
        }
    }

    override fun getPeakHours(shopId: String): Flow<Resource<Map<String, Int>>> = flow {
        emit(Resource.Loading)
        try {
            val ordersSnapshot = firestore.collection("orders").whereEqualTo("shopId", shopId).get().await()

            val peakHours = mutableMapOf<String, Int>()

            for (doc in ordersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = order.createdAt.toDate()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
                    peakHours[hour] = peakHours.getOrDefault(hour, 0) + 1
                }
            }

            emit(Resource.Success(peakHours))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get peak hours"))
            Log.e("AnalyticsRepository", "Get peak hours error", e)
        }
    }

    override fun getRevenueTrends(shopId: String): Flow<Resource<List<DailyStat>>> = getMonthlyStats(shopId)
    override fun getOrderTrends(shopId: String): Flow<Resource<List<DailyStat>>> = getMonthlyStats(shopId)

    override fun getCategoryWiseSales(shopId: String): Flow<Resource<List<CategoryStat>>> = flow {
        emit(Resource.Loading)
        try {
            val ordersSnapshot = firestore.collection("orders").whereEqualTo("shopId", shopId).get().await()
            val categorySales = mutableMapOf<String, Pair<Int, Double>>()

            for (orderDoc in ordersSnapshot.documents) {
                val order = orderDoc.toObject(Order::class.java)
                if (order != null) {
                    val itemsSnapshot = firestore.collection("orders").document(order.orderId).collection("items").get().await()

                    for (itemDoc in itemsSnapshot.documents) {
                        val item = itemDoc.toObject(com.saurabh.onecornersystem.data.model.OrderItem::class.java)
                        if (item != null) {
                            val productDoc = firestore.collection("shops").document(shopId).collection("products").document(item.productId).get().await()
                            val product = productDoc.toObject(Product::class.java)
                            if (product != null) {
                                val current = categorySales.getOrDefault(product.category, Pair(0, 0.0))
                                categorySales[product.category] = Pair(current.first + 1, current.second + item.totalPrice)
                            }
                        }
                    }
                }
            }

            val totalRevenue = categorySales.values.sumOf { it.second }
            val stats = categorySales.map { (category, data) ->
                CategoryStat(
                    category = category,
                    orderCount = data.first,
                    revenue = data.second,
                    percentageOfTotal = if (totalRevenue > 0) (data.second / totalRevenue) * 100 else 0.0
                )
            }

            emit(Resource.Success(stats.sortedByDescending { it.revenue }))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get category wise sales"))
            Log.e("AnalyticsRepository", "Get category wise sales error", e)
        }
    }

    override fun getPaymentMethodStats(shopId: String): Flow<Resource<Map<String, Int>>> = flow {
        emit(Resource.Loading)
        try {
            val ordersSnapshot = firestore.collection("orders").whereEqualTo("shopId", shopId).get().await()
            val methodStats = mutableMapOf<String, Int>()

            for (doc in ordersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    methodStats[order.paymentMethod] = methodStats.getOrDefault(order.paymentMethod, 0) + 1
                }
            }

            emit(Resource.Success(methodStats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get payment method stats"))
            Log.e("AnalyticsRepository", "Get payment method stats error", e)
        }
    }

    override fun getOrderStatusStats(shopId: String): Flow<Resource<Map<String, Int>>> = flow {
        emit(Resource.Loading)
        try {
            val ordersSnapshot = firestore.collection("orders").whereEqualTo("shopId", shopId).get().await()
            val statusStats = mutableMapOf<String, Int>()

            for (doc in ordersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    statusStats[order.status] = statusStats.getOrDefault(order.status, 0) + 1
                }
            }

            emit(Resource.Success(statusStats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get order status stats"))
            Log.e("AnalyticsRepository", "Get order status stats error", e)
        }
    }

    override fun updateAnalyticsFromOrder(shopId: String, order: Order): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val analyticsDoc = firestore.collection("shopAnalytics").document(shopId).get().await()
            val currentAnalytics = analyticsDoc.toObject(ShopAnalytics::class.java)

            val newAnalytics = if (currentAnalytics != null) {
                currentAnalytics.copy(
                    totalOrders = currentAnalytics.totalOrders + 1,
                    totalRevenue = currentAnalytics.totalRevenue + order.totalAmount,
                    averageOrderValue = (currentAnalytics.totalRevenue + order.totalAmount) / (currentAnalytics.totalOrders + 1)
                )
            } else {
                ShopAnalytics(shopId = shopId, totalOrders = 1, totalRevenue = order.totalAmount, averageOrderValue = order.totalAmount)
            }

            firestore.collection("shopAnalytics").document(shopId).set(newAnalytics).await()

            emit(Resource.Success(true))
            Log.d("AnalyticsRepository", "Analytics updated for order: ${order.orderId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update analytics"))
            Log.e("AnalyticsRepository", "Update analytics error", e)
        }
    }

    override fun generateCustomReport(shopId: String, startDate: Long, endDate: Long): Flow<Resource<CustomReport>> = flow {
        emit(Resource.Loading)
        try {
            val ordersSnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startDate / 1000, 0))
                .whereLessThanOrEqualTo("createdAt", com.google.firebase.Timestamp(endDate / 1000, 0))
                .get().await()

            var totalRevenue = 0.0
            val customers = mutableSetOf<String>()

            for (doc in ordersSnapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    totalRevenue += order.totalAmount
                    customers.add(order.userId)
                }
            }

            val report = CustomReport(
                reportId = firestore.collection("reports").document().id,
                shopId = shopId,
                startDate = startDate,
                endDate = endDate,
                totalOrders = ordersSnapshot.size(),
                totalRevenue = totalRevenue,
                averageOrderValue = if (ordersSnapshot.size() > 0) totalRevenue / ordersSnapshot.size() else 0.0,
                totalCustomers = customers.size,
                generatedAt = com.google.firebase.Timestamp.now()
            )

            emit(Resource.Success(report))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to generate report"))
            Log.e("AnalyticsRepository", "Generate report error", e)
        }
    }

    override fun listenToDashboard(shopId: String): Flow<ShopDashboard?> = flow {
        try {
            firestore.collection("shops").document(shopId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AnalyticsRepository", "Listen to dashboard error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // Use MutableStateFlow in ViewModel for real-time updates
                }
            }
        } catch (e: Exception) {
            Log.e("AnalyticsRepository", "Listen to dashboard error", e)
        }
    }

    override fun listenToAnalytics(shopId: String): Flow<ShopAnalytics?> = flow {
        try {
            firestore.collection("shopAnalytics").document(shopId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AnalyticsRepository", "Listen to analytics error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val analytics = snapshot.toObject(ShopAnalytics::class.java)
                    // Use MutableStateFlow in ViewModel for real-time updates
                }
            }
        } catch (e: Exception) {
            Log.e("AnalyticsRepository", "Listen to analytics error", e)
        }
    }

    override fun exportAnalytics(shopId: String, format: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val csv = buildString {
                append("Shop ID,Date,Orders,Revenue,Customers\n")
                // Data would be appended here
            }

            emit(Resource.Success(csv))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to export analytics"))
            Log.e("AnalyticsRepository", "Export analytics error", e)
        }
    }
}

