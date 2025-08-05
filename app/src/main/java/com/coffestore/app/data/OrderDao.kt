package com.coffestore.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order)

    @Update
    suspend fun update(order: Order)

    @Query("SELECT * FROM order_table WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?

    @Query("SELECT * FROM order_table WHERE status = 'Aberto' ORDER BY orderTime DESC")
    fun getAllOpenOrders(): Flow<List<Order>>

    @Query("SELECT * FROM order_table WHERE status = 'Aberto' ORDER BY orderTime DESC LIMIT 1")
    fun getLatestOpenOrder(): Flow<Order?>
}