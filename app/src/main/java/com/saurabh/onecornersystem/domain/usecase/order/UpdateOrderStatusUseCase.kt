package com.saurabh.onecornersystem.domain.usecase.order

import com.saurabh.onecornersystem.data.repository.OrderRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating order status with workflow validation
 */
class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) : BaseUseCase {

    fun execute(orderId: String, shopId: String, newStatus: String): Flow<Resource<Boolean>> {
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

        val validStatuses = listOf(
            "pending", "accepted", "rejected", "preparing",
            "ready", "out_for_delivery", "delivered", "cancelled"
        )

        if (newStatus.isBlank() || !validStatuses.contains(newStatus)) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Invalid order status: $newStatus"))
            }
        }

        return orderRepository.updateOrderStatus(orderId, shopId, newStatus)
    }
}

