package com.saurabh.onecornersystem.domain.usecase.product

import com.saurabh.onecornersystem.data.model.ProductVariant
import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for adding a product variant
 */
class AddProductVariantUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : BaseUseCase {

    fun execute(
        productId: String,
        shopId: String,
        variant: ProductVariant
    ): Flow<Resource<ProductVariant>> {
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

        if (variant.variantId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Variant ID cannot be empty"))
            }
        }

        if (variant.price <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Variant price must be greater than 0"))
            }
        }

        if (variant.stockQuantity < 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Stock quantity cannot be negative"))
            }
        }

        return productRepository.addVariant(productId, shopId, variant)
    }
}

