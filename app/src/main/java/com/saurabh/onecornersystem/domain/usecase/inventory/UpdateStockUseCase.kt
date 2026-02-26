package com.saurabh.onecornersystem.domain.usecase.inventory

import com.saurabh.onecornersystem.data.repository.InventoryRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating product stock quantity
 */
class UpdateStockUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : BaseUseCase {

    fun execute(
        productId: String,
        shopId: String,
        quantity: Int
    ): Flow<Resource<Boolean>> {
        if (productId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Product ID cannot be empty"))
            }
        }

        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        if (quantity < 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Quantity cannot be negative"))
            }
        }

        return inventoryRepository.updateStockQuantity(productId, shopId, quantity)
    }
}

