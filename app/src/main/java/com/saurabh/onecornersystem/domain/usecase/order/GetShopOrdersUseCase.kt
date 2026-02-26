package com.saurabh.onecornersystem.domain.usecase.order

import com.saurabh.onecornersystem.data.model.Order
import com.saurabh.onecornersystem.data.repository.OrderRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching shop orders with filtering
 */
class GetShopOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) : BaseUseCase {

    fun execute(
        shopId: String,
        status: String? = null
    ): Flow<Resource<List<Order>>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        return orderRepository.getShopOrders(shopId, status)
    }
}

