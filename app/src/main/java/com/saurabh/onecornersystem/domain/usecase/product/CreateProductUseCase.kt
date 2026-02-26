package com.saurabh.onecornersystem.domain.usecase.product

import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for creating a new product
 * Validates product data and delegates to repository
 */
class CreateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : BaseUseCase {

    fun execute(product: Product): Flow<Resource<Product>> {
        // Validation
        if (product.name.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Product name cannot be empty"))
            }
        }

        if (product.name.length < 3) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Product name must be at least 3 characters"))
            }
        }

        if (product.price <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Product price must be greater than 0"))
            }
        }

        if (product.shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID is required"))
            }
        }

        if (product.images.isEmpty()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("At least one product image is required"))
            }
        }

        // Delegate to repository
        return productRepository.createProduct(product)
    }
}

