package com.saurabh.onecornersystem.domain.usecase.inventory

import com.saurabh.onecornersystem.data.repository.InventoryRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for syncing inventory across variants and products
 */
class SyncInventoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : BaseUseCase {

    fun execute(shopId: String): Flow<Resource<Boolean>> = flow {
        if (shopId.isBlank()) {
            emit(Resource.Error("Shop ID cannot be empty"))
            return@flow
        }

        // Listener to inventory changes - this synchronizes the data
        try {
            emit(Resource.Loading)
            // Get shop inventory to sync
            inventoryRepository.getShopInventory(shopId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        emit(Resource.Success(true))
                    }
                    is Resource.Error -> {
                        emit(Resource.Error(result.message))
                    }
                    else -> { }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Sync failed"))
        }
    }
}

