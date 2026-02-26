package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.Product
import com.saurabh.onecornersystem.data.model.ProductVariant
import com.saurabh.onecornersystem.data.repository.ProductRepository
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    override fun createProduct(product: Product): Flow<Resource<Product>> = flow {
        emit(Resource.Loading)
        try {
            val productWithId = product.copy(
                productId = firestore.collection("products").document().id
            )

            firestore.collection("shops")
                .document(product.shopId)
                .collection("products")
                .document(productWithId.productId)
                .set(productWithId)
                .await()

            // Also add to global products index for search
            firestore.collection("products")
                .document(productWithId.productId)
                .set(productWithId)
                .await()

            emit(Resource.Success(productWithId))
            Log.d("ProductRepository", "Product created: ${productWithId.productId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create product"))
            Log.e("ProductRepository", "Create product error", e)
        }
    }

    override fun getProduct(productId: String, shopId: String): Flow<Resource<Product>> = flow {
        emit(Resource.Loading)
        try {
            val productDoc = firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .get()
                .await()

            val product = productDoc.toObject(Product::class.java)
            if (product != null) {
                emit(Resource.Success(product))
            } else {
                emit(Resource.Error("Product not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get product"))
            Log.e("ProductRepository", "Get product error", e)
        }
    }

    override fun updateProduct(
        productId: String,
        shopId: String,
        updates: Map<String, Any>
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = com.google.firebase.Timestamp.now()

            // Update in shop collection
            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .update(updateData)
                .await()

            // Update in global index
            firestore.collection("products")
                .document(productId)
                .update(updateData)
                .await()

            emit(Resource.Success(true))
            Log.d("ProductRepository", "Product updated: $productId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update product"))
            Log.e("ProductRepository", "Update product error", e)
        }
    }

    override fun deleteProduct(productId: String, shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            // Soft delete - mark as inactive
            val updates = mapOf(
                "isActive" to false,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .update(updates)
                .await()

            firestore.collection("products")
                .document(productId)
                .update(updates)
                .await()

            emit(Resource.Success(true))
            Log.d("ProductRepository", "Product deleted: $productId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete product"))
            Log.e("ProductRepository", "Delete product error", e)
        }
    }

    override fun getShopProducts(shopId: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val products = querySnapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop products"))
            Log.e("ProductRepository", "Get shop products error", e)
        }
    }

    override fun getShopProductsPaginated(
        shopId: String,
        pageSize: Int,
        lastDocument: Any?
    ): Flow<Resource<Pair<List<Product>, Any?>>> = flow {
        emit(Resource.Loading)
        try {
            var query: Query = firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument as com.google.firebase.firestore.DocumentSnapshot)
            }

            val querySnapshot = query.get().await()
            val products = querySnapshot.documents.mapNotNull { it.toObject(Product::class.java) }

            val nextLastDoc = if (products.size == pageSize) {
                querySnapshot.documents.lastOrNull()
            } else {
                null
            }

            emit(Resource.Success(Pair(products, nextLastDoc)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get products"))
            Log.e("ProductRepository", "Get products paginated error", e)
        }
    }

    override fun searchProducts(shopId: String, query: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .whereEqualTo("isActive", true)
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThan("name", query + "\uf8ff")
                .orderBy("name")
                .get()
                .await()

            val products = querySnapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to search products"))
            Log.e("ProductRepository", "Search products error", e)
        }
    }

    override fun addVariant(
        productId: String,
        shopId: String,
        variant: ProductVariant
    ): Flow<Resource<ProductVariant>> = flow {
        emit(Resource.Loading)
        try {
            val variantWithId = variant.copy(
                variantId = firestore.collection("variants").document().id
            )

            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .collection("variants")
                .document(variantWithId.variantId)
                .set(variantWithId)
                .await()

            emit(Resource.Success(variantWithId))
            Log.d("ProductRepository", "Variant added: ${variantWithId.variantId}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add variant"))
            Log.e("ProductRepository", "Add variant error", e)
        }
    }

    override fun updateVariant(
        productId: String,
        variantId: String,
        shopId: String,
        updates: Map<String, Any>
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = com.google.firebase.Timestamp.now()

            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .collection("variants")
                .document(variantId)
                .update(updateData)
                .await()

            emit(Resource.Success(true))
            Log.d("ProductRepository", "Variant updated: $variantId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update variant"))
            Log.e("ProductRepository", "Update variant error", e)
        }
    }

    override fun deleteVariant(
        productId: String,
        variantId: String,
        shopId: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .collection("variants")
                .document(variantId)
                .delete()
                .await()

            emit(Resource.Success(true))
            Log.d("ProductRepository", "Variant deleted: $variantId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete variant"))
            Log.e("ProductRepository", "Delete variant error", e)
        }
    }

    override fun getProductVariants(
        productId: String,
        shopId: String
    ): Flow<Resource<List<ProductVariant>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .collection("variants")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val variants = querySnapshot.documents.mapNotNull { it.toObject(ProductVariant::class.java) }
            emit(Resource.Success(variants))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get variants"))
            Log.e("ProductRepository", "Get variants error", e)
        }
    }

    override fun listenToShopProducts(shopId: String): Flow<List<Product>> = flow {
        try {
            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ProductRepository", "Listen to products error", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val products = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
                        // Emit is called but limited by Flow's design
                        // For real-time: use MutableStateFlow in ViewModel instead
                    }
                }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Listen to products error", e)
        }
    }

    override fun updateProductAvailability(
        productId: String,
        shopId: String,
        isAvailable: Boolean
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .document(productId)
                .update(
                    mapOf(
                        "isAvailable" to isAvailable,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("ProductRepository", "Product availability updated: $productId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update availability"))
            Log.e("ProductRepository", "Update availability error", e)
        }
    }

    override fun bulkUpdateProductsStatus(
        shopId: String,
        productIds: List<String>,
        isActive: Boolean
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val batch = firestore.batch()

            for (productId in productIds) {
                val docRef = firestore.collection("shops")
                    .document(shopId)
                    .collection("products")
                    .document(productId)

                batch.update(docRef, mapOf(
                    "isActive" to isActive,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ))
            }

            batch.commit().await()
            emit(Resource.Success(true))
            Log.d("ProductRepository", "Bulk products updated: ${productIds.size}")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to bulk update products"))
            Log.e("ProductRepository", "Bulk update error", e)
        }
    }

    override fun getProductsByCategory(
        shopId: String,
        category: String
    ): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("shops")
                .document(shopId)
                .collection("products")
                .whereEqualTo("category", category)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val products = querySnapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get products by category"))
            Log.e("ProductRepository", "Get by category error", e)
        }
    }
}

