package com.coffestore.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // Insere um novo produto. Se já existir, substitui.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    // Atualiza um produto existente.
    @Update
    suspend fun update(product: Product)

    // Busca um produto específico pelo seu ID.
    @Query("SELECT * FROM product_table WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    //Buscar por ativos (Quando for montar um pedido)
    @Query("SELECT * FROM product_table WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>

    //Buscar por produtos
    @Query("SELECT * FROM product_table ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
}
