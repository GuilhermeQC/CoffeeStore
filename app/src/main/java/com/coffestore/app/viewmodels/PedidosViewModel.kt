package com.coffestore.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coffestore.app.data.Order
import com.coffestore.app.repositories.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PedidosViewModel(private val repository: OrderRepository) : ViewModel() {

    val allOpenOrders: StateFlow<List<Order>> = repository.allOpenOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Expõe um StateFlow com o último pedido.
    val latestOpenOrder: StateFlow<Order?> = repository.latestOpenOrder.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    fun insert(order: Order) = viewModelScope.launch {
        repository.insert(order)
    }

    suspend fun getOrderById(id: Int): Order? {
        return repository.getOrderById(id)
    }

    fun update(order: Order) = viewModelScope.launch {
        repository.update(order)
    }

}

class PedidosViewModelFactory(private val repository: OrderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PedidosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PedidosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
