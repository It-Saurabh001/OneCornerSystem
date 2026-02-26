package com.saurabh.onecornersystem.domain.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.saurabh.onecornersystem.data.model.Order
import com.saurabh.onecornersystem.data.model.OrderItem
import com.saurabh.onecornersystem.data.repository.OrderRepository
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {
    // Full implementation copied from data/repository/OrderRepositoryImpl.kt
    // with package updated to domain.repository

    override fun getShopOrders(shopId: String, status: String?): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading)
        try {
            var query: Query = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (status != null) {
                query = query.whereEqualTo("status", status)
            }

            val querySnapshot = query.get().await()
            val orders = querySnapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(Resource.Success(orders))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get shop orders"))
            Log.e("OrderRepository", "Get shop orders error", e)
        }
    }

    override fun getShopOrdersPaginated(
        shopId: String,
        pageSize: Int,
        lastDocument: Any?
    ): Flow<Resource<Pair<List<Order>, Any?>>> = flow {
        emit(Resource.Loading)
        try {
            var query: Query = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            if (lastDocument != null) {
                query = query.startAfter(lastDocument as com.google.firebase.firestore.DocumentSnapshot)
            }

            val querySnapshot = query.get().await()
            val orders = querySnapshot.documents.mapNotNull { it.toObject(Order::class.java) }

            val nextLastDoc = if (orders.size == pageSize) {
                querySnapshot.documents.lastOrNull()
            } else {
                null
            }

            emit(Resource.Success(Pair(orders, nextLastDoc)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get paginated orders"))
            Log.e("OrderRepository", "Get paginated orders error", e)
        }
    }

    override fun getOrderDetails(orderId: String, shopId: String): Flow<Resource<Order>> = flow {
        emit(Resource.Loading)
        try {
            val orderDoc = firestore.collection("orders")
                .document(orderId)
                .get()
                .await()

            val order = orderDoc.toObject(Order::class.java)
            if (order != null && order.shopId == shopId) {
                emit(Resource.Success(order))
            } else {
                emit(Resource.Error("Order not found or unauthorized"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get order details"))
            Log.e("OrderRepository", "Get order details error", e)
        }
    }

    override fun getOrderItems(orderId: String): Flow<Resource<List<OrderItem>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("orders")
                .document(orderId)
                .collection("items")
                .get()
                .await()

            val items = querySnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java) }
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get order items"))
            Log.e("OrderRepository", "Get order items error", e)
        }
    }

    override fun updateOrderStatus(orderId: String, shopId: String, newStatus: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order status updated: $orderId -> $newStatus")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update order status"))
            Log.e("OrderRepository", "Update order status error", e)
        }
    }

    override fun acceptOrder(orderId: String, shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "accepted",
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order accepted: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to accept order"))
            Log.e("OrderRepository", "Accept order error", e)
        }
    }

    override fun rejectOrder(orderId: String, shopId: String, reason: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "cancellationReason" to reason,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order rejected: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to reject order"))
            Log.e("OrderRepository", "Reject order error", e)
        }
    }

    override fun markOrderAsPreparing(orderId: String, shopId: String, estimatedTime: Long): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val estimatedDeliveryTime = (System.currentTimeMillis() / 1000) + estimatedTime
            val estimatedDelivery = com.google.firebase.Timestamp(estimatedDeliveryTime, 0)

            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "preparing",
                        "estimatedDelivery" to estimatedDelivery,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order marked as preparing: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark as preparing"))
            Log.e("OrderRepository", "Mark as preparing error", e)
        }
    }

    override fun markOrderAsReady(orderId: String, shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "ready",
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order marked as ready: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark as ready"))
            Log.e("OrderRepository", "Mark as ready error", e)
        }
    }

    override fun markOrderAsOutForDelivery(orderId: String, shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "out_for_delivery",
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order marked as out for delivery: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark as out for delivery"))
            Log.e("OrderRepository", "Mark as out for delivery error", e)
        }
    }

    override fun markOrderAsDelivered(orderId: String, shopId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "delivered",
                        "deliveredAt" to com.google.firebase.Timestamp.now(),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order marked as delivered: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark as delivered"))
            Log.e("OrderRepository", "Mark as delivered error", e)
        }
    }

    override fun cancelOrder(orderId: String, shopId: String, reason: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection("orders")
                .document(orderId)
                .update(
                    mapOf(
                        "status" to "cancelled",
                        "cancellationReason" to reason,
                        "cancelledAt" to com.google.firebase.Timestamp.now(),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()

            emit(Resource.Success(true))
            Log.d("OrderRepository", "Order cancelled: $orderId")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to cancel order"))
            Log.e("OrderRepository", "Cancel order error", e)
        }
    }

    override fun listenToShopOrders(shopId: String, status: String?): Flow<List<Order>> = flow {
        try {
            var query: Query = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (status != null) {
                query = query.whereEqualTo("status", status)
            }

            query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderRepository", "Listen to orders error", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                    // Use MutableStateFlow in ViewModel for real-time updates
                }
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Listen to orders error", e)
        }
    }

    override fun listenToOrderDetails(orderId: String): Flow<Order?> = flow {
        try {
            firestore.collection("orders")
                .document(orderId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("OrderRepository", "Listen to order error", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val order = snapshot.toObject(Order::class.java)
                        // Use MutableStateFlow in ViewModel for real-time updates
                    }
                }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Listen to order error", e)
        }
    }

    override fun getTodayOrdersCount(shopId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val today = System.currentTimeMillis()
            val startOfDay = (today / 86400000) * 86400000

            val querySnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startOfDay / 1000, 0))
                .get()
                .await()

            emit(Resource.Success(querySnapshot.size()))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get today's order count"))
            Log.e("OrderRepository", "Get today's count error", e)
        }
    }

    override fun getPendingOrders(shopId: String): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = querySnapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(Resource.Success(orders))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get pending orders"))
            Log.e("OrderRepository", "Get pending orders error", e)
        }
    }

    override fun getOrdersByDateRange(
        shopId: String,
        startDate: Long,
        endDate: Long
    ): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereGreaterThanOrEqualTo("createdAt", com.google.firebase.Timestamp(startDate / 1000, 0))
                .whereLessThanOrEqualTo("createdAt", com.google.firebase.Timestamp(endDate / 1000, 0))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = querySnapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(Resource.Success(orders))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get orders by date range"))
            Log.e("OrderRepository", "Get orders by date range error", e)
        }
    }

    override fun getCustomerOrders(shopId: String, customerId: String): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading)
        try {
            val querySnapshot = firestore.collection("orders")
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("userId", customerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = querySnapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(Resource.Success(orders))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get customer orders"))
            Log.e("OrderRepository", "Get customer orders error", e)
        }
    }
}

