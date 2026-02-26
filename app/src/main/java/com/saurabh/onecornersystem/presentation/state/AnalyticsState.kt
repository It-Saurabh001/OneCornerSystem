package com.saurabh.onecornersystem.presentation.state

import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.model.ShopAnalytics

/**
 * Sealed class representing different states for analytics operations
 */
sealed class AnalyticsState {
    object Idle : AnalyticsState()
    object Loading : AnalyticsState()
    data class Success(val analytics: ShopAnalytics) : AnalyticsState()
    data class TopProductsSuccess(val products: List<Product>) : AnalyticsState()
    data class ReportSuccess(val report: String) : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}

/**
 * Analytics UI state holding all analytics-related state flows
 */
data class AnalyticsUiState(
    val analyticsState: AnalyticsState = AnalyticsState.Idle,
    val isLoadingAnalytics: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val errorMessage: String? = null,
    val analytics: ShopAnalytics? = null,
    val topProducts: List<Product> = emptyList()
)

