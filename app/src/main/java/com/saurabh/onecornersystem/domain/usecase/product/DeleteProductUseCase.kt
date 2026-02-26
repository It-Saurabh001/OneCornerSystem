package com.saurabh.onecornersystem.domain.usecase.product

import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for deleting a product
 */
class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : BaseUseCase {

    fun execute(productId: String, shopId: String): Flow<Resource<Boolean>> {
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

        return productRepository.deleteProduct(productId, shopId)
    }
}

