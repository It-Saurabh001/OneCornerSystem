package com.saurabh.onecornersystem.domain.usecase.inventory

import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.repository.InventoryRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking low stock and getting alerts
 */
class CheckLowStockUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : BaseUseCase {

    fun execute(shopId: String, threshold: Int = 5): Flow<Resource<List<Product>>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        if (threshold < 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Threshold cannot be negative"))
            }
        }

        return inventoryRepository.getLowStockProducts(shopId, threshold)
    }
}

