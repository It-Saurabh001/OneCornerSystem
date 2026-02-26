package com.saurabh.onecornersystem.domain.usecase.product

import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating an existing product
 */
class UpdateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : BaseUseCase {

    fun execute(productId: String, shopId: String, updates: Map<String, Any>): Flow<Resource<Boolean>> {
        // Validation
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

        if (updates.isEmpty()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("No updates provided"))
            }
        }

        // Delegate to repository
        return productRepository.updateProduct(productId, shopId, updates)
    }
}

