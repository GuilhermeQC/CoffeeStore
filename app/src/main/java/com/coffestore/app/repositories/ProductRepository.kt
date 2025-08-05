package com.coffestore.app.repositories

import com.coffestore.app.data.Product
import com.coffestore.app.data.ProductDao
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    // Para a tela de gestão de produtos (mostra todos)
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    // Para a tela de novo pedido (mostra apenas os ativos)
    val allActiveProducts: Flow<List<Product>> = productDao.getAllActiveProducts()

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    suspend fun update(product: Product) {
        productDao.update(product)
    }
}
