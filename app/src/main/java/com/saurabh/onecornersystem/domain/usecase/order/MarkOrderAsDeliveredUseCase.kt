package com.saurabh.onecornersystem.domain.usecase.order

import com.saurabh.onecornersystem.data.repository.OrderRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for marking an order as delivered
 */
class MarkOrderAsDeliveredUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) : BaseUseCase {

    fun execute(orderId: String, shopId: String): Flow<Resource<Boolean>> {
        if (orderId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Order ID cannot be empty"))
            }
        }

        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        return orderRepository.markOrderAsDelivered(orderId, shopId)
    }
}

