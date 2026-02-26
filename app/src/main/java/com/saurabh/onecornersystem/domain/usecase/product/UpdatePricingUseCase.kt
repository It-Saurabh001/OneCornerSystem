package com.saurabh.onecornersystem.domain.usecase.product

import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating product pricing
 */
class UpdatePricingUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : BaseUseCase {

    fun execute(
        productId: String,
        shopId: String,
        price: Double,
        discountedPrice: Double? = null
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

        if (price <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Price must be greater than 0"))
            }
        }

        if (discountedPrice != null && discountedPrice <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Discounted price must be greater than 0"))
            }
        }

        if (discountedPrice != null && discountedPrice >= price) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Discounted price must be less than original price"))
            }
        }

        val updates = mutableMapOf<String, Any>("price" to price)
        if (discountedPrice != null) {
            updates["discountedPrice"] = discountedPrice
        }

        return productRepository.updateProduct(productId, shopId, updates)
    }
}

