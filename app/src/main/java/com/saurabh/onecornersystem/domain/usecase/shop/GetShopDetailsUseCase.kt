package com.saurabh.onecornersystem.domain.usecase.shop

import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching shop details
 */
class GetShopDetailsUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) : BaseUseCase {

    fun execute(shopId: String): Flow<Resource<Shop>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        return shopRepository.getShopDetails(shopId)
    }
}

