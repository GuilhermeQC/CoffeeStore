package com.coffestore.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity para marcar como uma tabela do Room.
@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    // Para a foto, vamos armazenar o caminho (URI) da imagem como uma String.
    // A imagem em si ficará no armazenamento do dispositivo.
    val photoUri: String,

    val description: String,

    val value: Float,

    // "active" é uma palavra-chave em alguns sistemas, então usamos "isActive"
    // para evitar qualquer conflito.
    val isActive: Boolean
)