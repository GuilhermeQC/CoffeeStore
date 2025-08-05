package com.coffestore.app

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffestore.app.adapters.SelecaoProdutosAdapter
import com.coffestore.app.data.AppDatabase
import com.coffestore.app.data.ItemSelecaoProduto
import com.coffestore.app.data.Order
import com.coffestore.app.repositories.OrderRepository
import com.coffestore.app.repositories.ProductRepository
import com.coffestore.app.viewmodels.PedidosViewModel
import com.coffestore.app.viewmodels.PedidosViewModelFactory
import com.coffestore.app.viewmodels.ProdutosViewModel
import com.coffestore.app.viewmodels.ProdutosViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class NovoPedidoActivity : AppCompatActivity() {

    // ViewModel para buscar os produtos
    private val produtosViewModel: ProdutosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = ProductRepository(database.productDao())
        ProdutosViewModelFactory(repository)
    }

    // ViewModel para salvar o novo pedido
    private val pedidosViewModel: PedidosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = OrderRepository(database.orderDao())
        PedidosViewModelFactory(repository)
    }

    private lateinit var selecaoAdapter: SelecaoProdutosAdapter
    private lateinit var nomeClienteEditText: EditText
    private lateinit var mesaEditText: EditText
    private lateinit var valorTotalTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novo_pedido)

        // Encontra as views do formulário
        nomeClienteEditText = findViewById(R.id.edit_text_nome_cliente)
        mesaEditText = findViewById(R.id.edit_text_mesa)
        valorTotalTextView = findViewById(R.id.text_view_valor_total)
        val finalizarButton = findViewById<MaterialButton>(R.id.button_finalizar_pedido)
        val voltarButton = findViewById<ImageView>(R.id.button_voltar)

        setupRecyclerView()
        observeProdutos()

        finalizarButton.setOnClickListener {
            finalizarPedido()
        }

        voltarButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // Inicializa o adapter, passando a função que será chamada quando uma quantidade mudar
        selecaoAdapter = SelecaoProdutosAdapter { itemAlterado ->
            // Toda vez que a quantidade de um item muda, recalculamos o valor total
            calcularValorTotal()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_selecao_produtos)
        recyclerView.adapter = selecaoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeProdutos() {
        lifecycleScope.launch {
            // Observa a lista de produtos ativos do ViewModel
            produtosViewModel.allActiveProducts.collectLatest { produtos ->
                // Converte a lista de Product para uma lista de ItemSelecaoProduto
                val itensParaSelecao = produtos.map { produto ->
                    ItemSelecaoProduto(produto = produto, quantidade = 0)
                }
                // Envia a nova lista para o adapter
                selecaoAdapter.submitList(itensParaSelecao)
            }
        }
    }

    private fun calcularValorTotal() {
        // Pega a lista atual de itens do adapter
        val itensAtuais = selecaoAdapter.currentList
        var valorTotal = 0.0f

        // Itera sobre a lista e soma o valor dos itens selecionados
        itensAtuais.forEach { item ->
            valorTotal += item.produto.value * item.quantidade
        }

        // Formata e exibe o valor total
        valorTotalTextView.text = getString(R.string.formato_valor_reais, valorTotal)
    }

    private fun finalizarPedido() {
        val nomeCliente = nomeClienteEditText.text.toString().trim()
        val mesaStr = mesaEditText.text.toString().trim()

        if (nomeCliente.isEmpty()) {
            Toast.makeText(this, "O nome do cliente é obrigatório.", Toast.LENGTH_SHORT).show()
            return
        }

        val mesa = if (mesaStr.isNotEmpty()) mesaStr.toInt() else 0

        val itensSelecionados = selecaoAdapter.currentList.filter { it.quantidade > 0 }

        if (itensSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um produto.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria uma lista apenas com os IDs dos produtos selecionados
        val listaDeIdsDosItens = mutableListOf<Int>()
        var valorTotal = 0.0f
        itensSelecionados.forEach { item ->
            repeat(item.quantidade) {
                listaDeIdsDosItens.add(item.produto.id)
            }
            valorTotal += item.produto.value * item.quantidade
        }

        // Cria o objeto Order
        val novoPedido = Order(
            orderTime = Date(),
            clientName = nomeCliente,
            items = listaDeIdsDosItens,
            totalValue = valorTotal,
            tableNumber = mesa
        )

        // Usa o PedidosViewModel para salvar o novo pedido
        pedidosViewModel.insert(novoPedido)

        Toast.makeText(this, "Pedido finalizado com sucesso!", Toast.LENGTH_LONG).show()
        finish() // Fecha a tela e volta para a lista de pedidos
    }
}
