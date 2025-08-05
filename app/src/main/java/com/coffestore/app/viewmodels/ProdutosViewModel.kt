package com.coffestore.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coffestore.app.data.Product
import com.coffestore.app.repositories.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProdutosViewModel(private val repository: ProductRepository) : ViewModel() {

    // Expõe um StateFlow com a lista de produtos para a UI observar.
    // O stateIn converte o Flow normal do repositório num StateFlow, que
    // guarda o último valor e é ideal para o estado da UI.
    val allProducts: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Para a tela de novo pedido
    val allActiveProducts: StateFlow<List<Product>> = repository.allActiveProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
    }

    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
    }

    suspend fun getProductById(id: Int): Product? {
        return repository.getProductById(id)
    }

}

class ProdutosViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProdutosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProdutosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}