package com.saurabh.onecornersystem.data.repository

import com.saurabh.onecornersystem.data.model.Order
import com.saurabh.onecornersystem.data.model.OrderItem
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    /**
     * Get all orders for a shop (with optional status filter)
     */
    fun getShopOrders(shopId: String, status: String? = null): Flow<Resource<List<Order>>>

    /**
     * Get orders with pagination
     */
    fun getShopOrdersPaginated(shopId: String, pageSize: Int, lastDocument: Any? = null): Flow<Resource<Pair<List<Order>, Any?>>>

    /**
     * Get order details
     */
    fun getOrderDetails(orderId: String, shopId: String): Flow<Resource<Order>>

    /**
     * Get order items for an order
     */
    fun getOrderItems(orderId: String): Flow<Resource<List<OrderItem>>>

    /**
     * Update order status
     */
    fun updateOrderStatus(orderId: String, shopId: String, newStatus: String): Flow<Resource<Boolean>>

    /**
     * Accept an order
     */
    fun acceptOrder(orderId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Reject an order with reason
     */
    fun rejectOrder(orderId: String, shopId: String, reason: String): Flow<Resource<Boolean>>

    /**
     * Mark order as preparing
     */
    fun markOrderAsPreparing(orderId: String, shopId: String, estimatedTime: Long): Flow<Resource<Boolean>>

    /**
     * Mark order as ready for pickup/delivery
     */
    fun markOrderAsReady(orderId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Mark order as out for delivery
     */
    fun markOrderAsOutForDelivery(orderId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Mark order as delivered
     */
    fun markOrderAsDelivered(orderId: String, shopId: String): Flow<Resource<Boolean>>

    /**
     * Cancel order with reason
     */
    fun cancelOrder(orderId: String, shopId: String, reason: String): Flow<Resource<Boolean>>

    /**
     * Real-time listener for shop orders
     */
    fun listenToShopOrders(shopId: String, status: String? = null): Flow<List<Order>>

    /**
     * Real-time listener for specific order
     */
    fun listenToOrderDetails(orderId: String): Flow<Order?>

    /**
     * Get today's orders count
     */
    fun getTodayOrdersCount(shopId: String): Flow<Resource<Int>>

    /**
     * Get pending orders for shop
     */
    fun getPendingOrders(shopId: String): Flow<Resource<List<Order>>>

    /**
     * Get orders by date range
     */
    fun getOrdersByDateRange(shopId: String, startDate: Long, endDate: Long): Flow<Resource<List<Order>>>

    /**
     * Get order by customer for shop context
     */
    fun getCustomerOrders(shopId: String, customerId: String): Flow<Resource<List<Order>>>
}

