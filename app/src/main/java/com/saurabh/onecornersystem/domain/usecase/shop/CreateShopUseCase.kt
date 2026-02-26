package com.saurabh.onecornersystem.domain.usecase.shop

import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.repository.ShopRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for creating a new shop
 * Validates shop data and delegates to repository
 */
class CreateShopUseCase @Inject constructor(
    private val shopRepository: ShopRepository
) : BaseUseCase {

    fun execute(shop: Shop): Flow<Resource<Shop>> {
        // Validation
        if (shop.shopName.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop name cannot be empty"))
            }
        }

        if (shop.shopName.length < 3) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop name must be at least 3 characters"))
            }
        }

        if (shop.contactNumber.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Contact number is required"))
            }
        }

        // Delegate to repository
        return shopRepository.createShop(shop)
    }
}

