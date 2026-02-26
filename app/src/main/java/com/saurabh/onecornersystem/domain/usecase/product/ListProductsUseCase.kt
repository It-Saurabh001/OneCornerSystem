package com.saurabh.onecornersystem.domain.usecase.product

import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.domain.usecase.BaseUseCase
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for listing products with optional filtering
 */
class ListProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) : BaseUseCase {

    fun execute(
        shopId: String,
        category: String? = null,
        searchQuery: String? = null
    ): Flow<Resource<List<Product>>> {
        if (shopId.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Shop ID cannot be empty"))
            }
        }

        return when {
            !category.isNullOrBlank() -> {
                productRepository.getProductsByCategory(shopId, category)
            }
            !searchQuery.isNullOrBlank() -> {
                productRepository.searchProducts(shopId, searchQuery)
            }
            else -> {
                productRepository.getShopProducts(shopId)
            }
        }
    }
}

