package com.coffestore.app.repositories

import com.coffestore.app.data.Order
import com.coffestore.app.data.OrderDao
import kotlinx.coroutines.flow.Flow


class OrderRepository(private val orderDao: OrderDao) {

    val allOpenOrders: Flow<List<Order>> = orderDao.getAllOpenOrders()

    val latestOpenOrder: Flow<Order?> = orderDao.getLatestOpenOrder()

    suspend fun insert(order: Order) {
        orderDao.insert(order)
    }

    suspend fun getOrderById(id: Int): Order? {
        return orderDao.getOrderById(id)
    }

    suspend fun update(order: Order) {
        orderDao.update(order)
    }
}
