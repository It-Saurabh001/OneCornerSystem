package com.saurabh.onecornersystem.domain.usecase.shop

import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating shop profile
 * Validates update data and delegates to repository
 */
class UpdateShopProfileUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) : BaseUseCase {

    fun execute(shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>> {
        // Validation
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID is required"))
            }
        }

        if (updates.isEmpty()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("No updates provided"))
            }
        }

        // Delegate to repository
        return shopRepository.updateShopProfile(shopId, updates)
    }
}

