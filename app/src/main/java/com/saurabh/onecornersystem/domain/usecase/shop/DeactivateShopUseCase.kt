package com.saurabh.onecornersystem.domain.usecase.shop

import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for deactivating a shop
 */
class DeactivateShopUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) : BaseUseCase {

    fun execute(shopId: String): Flow<Resource<Boolean>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        return shopRepository.deleteShop(shopId)
    }
}

