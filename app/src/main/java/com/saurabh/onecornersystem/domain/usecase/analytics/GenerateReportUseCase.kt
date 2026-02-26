package com.saurabh.onecornersystem.domain.usecase.analytics

import com.saurabh.onecornersystem.data.model.CustomReport
import com.saurabh.onecornersystem.data.repository.AnalyticsRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for generating analytics reports
 */
class GenerateReportUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : BaseUseCase {

    fun execute(
        shopId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Resource<CustomReport>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        if (startDate >= endDate) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Start date must be before end date"))
            }
        }

        return analyticsRepository.generateCustomReport(shopId, startDate, endDate)
    }
}

