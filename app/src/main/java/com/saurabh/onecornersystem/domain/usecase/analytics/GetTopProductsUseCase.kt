package com.saurabh.onecornersystem.domain.usecase.analytics

import com.saurabh.onecornersystem.data.model.ProductStat
import com.saurabh.onecornersystem.data.repository.AnalyticsRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting top selling products
 */
class GetTopProductsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : BaseUseCase {

    fun execute(shopId: String, limit: Int = 10): Flow<Resource<List<ProductStat>>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        if (limit <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Limit must be greater than 0"))
            }
        }

        return analyticsRepository.getTopProducts(shopId, limit)
    }
}

