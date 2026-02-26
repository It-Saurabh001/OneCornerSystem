package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.ShopAnalytics
import com.saurabh.onecornersystem.data.model.ProductStat
import com.saurabh.onecornersystem.data.model.CustomReport
import com.saurabh.onecornersystem.domain.usecase.analytics.GetShopAnalyticsUseCase
import com.saurabh.onecornersystem.domain.usecase.analytics.GetTopProductsUseCase
import com.saurabh.onecornersystem.domain.usecase.analytics.GenerateReportUseCase
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing shop analytics and reports
 * Handles analytics data retrieval and report generation
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getShopAnalyticsUseCase: GetShopAnalyticsUseCase,
    private val getTopProductsUseCase: GetTopProductsUseCase,
    private val generateReportUseCase: GenerateReportUseCase
) : ViewModel() {

    private val _analyticsState = MutableStateFlow<Resource<ShopAnalytics>>(Resource.Loading)
    val analyticsState: StateFlow<Resource<ShopAnalytics>> = _analyticsState.asStateFlow()

    private val _topProductsState = MutableStateFlow<Resource<List<ProductStat>>>(Resource.Loading)
    val topProductsState: StateFlow<Resource<List<ProductStat>>> = _topProductsState.asStateFlow()

    private val _reportState = MutableStateFlow<Resource<CustomReport>?>(null)
    val reportState: StateFlow<Resource<CustomReport>?> = _reportState.asStateFlow()

    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()

    private val _totalOrders = MutableStateFlow(0)
    val totalOrders: StateFlow<Int> = _totalOrders.asStateFlow()

    private val _totalCustomers = MutableStateFlow(0)
    val totalCustomers: StateFlow<Int> = _totalCustomers.asStateFlow()

    fun loadAnalytics(shopId: String) {
        viewModelScope.launch {
            getShopAnalyticsUseCase.execute(shopId).collect { result ->
                _analyticsState.value = result
                if (result is Resource.Success) {
                    _totalRevenue.value = result.data.totalRevenue
                    _totalOrders.value = result.data.totalOrders
                    _totalCustomers.value = result.data.totalCustomers
                }
            }
        }
    }

    fun loadTopProducts(shopId: String, limit: Int = 10) {
        viewModelScope.launch {
            getTopProductsUseCase.execute(shopId, limit).collect { result ->
                _topProductsState.value = result
            }
        }
    }

    fun generateReport(shopId: String, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _reportState.value = Resource.Loading
            generateReportUseCase.execute(shopId, startDate, endDate).collect { result ->
                _reportState.value = result
            }
        }
    }

    fun loadFullAnalytics(shopId: String) {
        viewModelScope.launch {
            loadAnalytics(shopId)
            loadTopProducts(shopId)
        }
    }

    fun clearReportState() {
        _reportState.value = null
    }

    fun refreshAnalytics(shopId: String) {
        loadFullAnalytics(shopId)
    }
}

