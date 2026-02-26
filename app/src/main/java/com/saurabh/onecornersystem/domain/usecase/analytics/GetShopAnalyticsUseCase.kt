package com.saurabh.onecornersystem.domain.usecase.analytics

import com.saurabh.onecornersystem.data.model.ShopAnalytics
import com.saurabh.onecornersystem.data.repository.AnalyticsRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching shop analytics
 */
class GetShopAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : BaseUseCase {

    fun execute(shopId: String): Flow<Resource<ShopAnalytics>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        return analyticsRepository.getShopAnalytics(shopId)
    }
}

