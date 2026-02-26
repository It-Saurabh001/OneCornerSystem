package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.ShopAnalytics
import com.saurabh.onecornersystem.data.model.ShopDashboard
import com.saurabh.onecornersystem.data.model.DailyStat
import com.saurabh.onecornersystem.data.model.MonthlyStat
import com.saurabh.onecornersystem.data.model.ProductStat
import com.saurabh.onecornersystem.data.model.CustomerMetrics
import com.saurabh.onecornersystem.data.model.CategoryStat
import com.saurabh.onecornersystem.data.model.CustomReport
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getShopAnalytics(shopId: String): Flow<Resource<ShopAnalytics>>
    fun getShopDashboard(shopId: String): Flow<Resource<ShopDashboard>>
    fun getTodayStats(shopId: String): Flow<Resource<DailyStat>>
    fun getWeeklyStats(shopId: String): Flow<Resource<List<DailyStat>>>
    fun getMonthlyStats(shopId: String): Flow<Resource<List<DailyStat>>>
    fun getYearlyStats(shopId: String): Flow<Resource<List<MonthlyStat>>>
    fun getTopProducts(shopId: String, limit: Int = 10): Flow<Resource<List<ProductStat>>>
    fun getCustomerMetrics(shopId: String): Flow<Resource<CustomerMetrics>>
    fun getPeakHours(shopId: String): Flow<Resource<Map<String, Int>>>
    fun getRevenueTrends(shopId: String): Flow<Resource<List<DailyStat>>>
    fun getOrderTrends(shopId: String): Flow<Resource<List<DailyStat>>>
    fun getCategoryWiseSales(shopId: String): Flow<Resource<List<CategoryStat>>>
    fun getPaymentMethodStats(shopId: String): Flow<Resource<Map<String, Int>>>
    fun getOrderStatusStats(shopId: String): Flow<Resource<Map<String, Int>>>
    fun updateAnalyticsFromOrder(shopId: String, order: com.saurabh.onecornersystem.data.model.Order): Flow<Resource<Boolean>>
    fun generateCustomReport(shopId: String, startDate: Long, endDate: Long): Flow<Resource<CustomReport>>
    fun listenToDashboard(shopId: String): Flow<ShopDashboard?>
    fun listenToAnalytics(shopId: String): Flow<ShopAnalytics?>
    fun exportAnalytics(shopId: String, format: String = "csv"): Flow<Resource<String>>
}


