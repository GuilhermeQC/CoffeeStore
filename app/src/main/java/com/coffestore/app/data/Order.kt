package com.coffestore.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "order_table")
@TypeConverters(Converters::class)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderTime: Date,
    val clientName: String,
    val items: List<Int>,
    val totalValue: Float,
    val tableNumber: Int,

    val status: String = "Aberto"
)
