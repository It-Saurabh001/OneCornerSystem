package com.saurabh.onecornersystem.presentation.shopowner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.onecornersystem.data.model.ShopAnalytics
import com.saurabh.onecornersystem.data.model.Order
import com.saurabh.onecornersystem.domain.usecase.analytics.GetShopAnalyticsUseCase
import com.saurabh.onecornersystem.domain.usecase.order.GetShopOrdersUseCase
import com.saurabh.onecornersystem.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing shop owner dashboard
 * Handles display of shop analytics and recent orders
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getShopAnalyticsUseCase: GetShopAnalyticsUseCase,
    private val getShopOrdersUseCase: GetShopOrdersUseCase
) : ViewModel() {

    private val _analyticsState = MutableStateFlow<Resource<ShopAnalytics>>(Resource.Loading)
    val analyticsState: StateFlow<Resource<ShopAnalytics>> = _analyticsState.asStateFlow()

    private val _ordersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading)
    val ordersState: StateFlow<Resource<List<Order>>> = _ordersState.asStateFlow()

    private val _pendingOrdersCount = MutableStateFlow(0)
    val pendingOrdersCount: StateFlow<Int> = _pendingOrdersCount.asStateFlow()

    fun loadDashboard(shopId: String) {
        viewModelScope.launch {
            loadAnalytics(shopId)
            loadPendingOrders(shopId)
        }
    }

    private fun loadAnalytics(shopId: String) {
        viewModelScope.launch {
            getShopAnalyticsUseCase.execute(shopId).collect { result ->
                _analyticsState.value = result
            }
        }
    }

    private fun loadPendingOrders(shopId: String) {
        viewModelScope.launch {
            getShopOrdersUseCase.execute(shopId, "pending").collect { result ->
                _ordersState.value = result
                if (result is Resource.Success) {
                    _pendingOrdersCount.value = result.data.size
                }
            }
        }
    }

    fun refreshDashboard(shopId: String) {
        loadDashboard(shopId)
    }
}

