package com.coffestore.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class EditarPedidoActivity : AppCompatActivity() {

    private val produtosViewModel: ProdutosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = ProductRepository(database.productDao())
        ProdutosViewModelFactory(repository)
    }

    private val pedidosViewModel: PedidosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = OrderRepository(database.orderDao())
        PedidosViewModelFactory(repository)
    }

    private lateinit var selecaoAdapter: SelecaoProdutosAdapter
    private lateinit var nomeClienteEditText: EditText
    private lateinit var mesaEditText: EditText
    private lateinit var valorTotalTextView: TextView
    private lateinit var tituloTextView: TextView

    private var pedidoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novo_pedido)

        pedidoId = intent.getIntExtra(EXTRA_ORDER_ID, -1)
        if (pedidoId == -1) {
            Toast.makeText(this, "Erro: ID do pedido não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        nomeClienteEditText = findViewById(R.id.edit_text_nome_cliente)
        mesaEditText = findViewById(R.id.edit_text_mesa)
        valorTotalTextView = findViewById(R.id.text_view_valor_total)
        tituloTextView = findViewById(R.id.text_view_titulo)
        val finalizarButton = findViewById<MaterialButton>(R.id.button_finalizar_pedido)
        val voltarButton = findViewById<ImageView>(R.id.button_voltar)
        val confirmarHeaderButton = findViewById<ImageView>(R.id.button_confirmar)

        tituloTextView.text = "Editar Pedido"
        finalizarButton.visibility = View.GONE
        confirmarHeaderButton.visibility = View.VISIBLE

        setupRecyclerView()
        carregarDadosDoPedido()

        confirmarHeaderButton.setOnClickListener {
            mostrarDialogoDeConfirmacao()
        }

        voltarButton.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDialogoDeConfirmacao() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmacao, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()

        val buttonSim = dialogView.findViewById<Button>(R.id.button_sim)
        val buttonNao = dialogView.findViewById<Button>(R.id.button_nao)

        buttonSim.setOnClickListener {
            salvarAlteracoes()
            dialog.dismiss()
        }

        buttonNao.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun setupRecyclerView() {
        selecaoAdapter = SelecaoProdutosAdapter {
            calcularValorTotal()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_selecao_produtos)
        recyclerView.adapter = selecaoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun carregarDadosDoPedido() {
        lifecycleScope.launch {
            val pedido = pedidosViewModel.getOrderById(pedidoId)
            if (pedido == null) {
                Toast.makeText(this@EditarPedidoActivity, "Erro ao carregar o pedido.", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val todosOsProdutos = produtosViewModel.allActiveProducts
                .filter { it.isNotEmpty() }
                .first()

            nomeClienteEditText.setText(pedido.clientName)
            mesaEditText.setText(pedido.tableNumber.toString())

            val contagemDeItens = pedido.items.groupingBy { it }.eachCount()

            val itensParaSelecao = todosOsProdutos.map { produto ->
                val quantidade = contagemDeItens[produto.id] ?: 0
                ItemSelecaoProduto(produto = produto, quantidade = quantidade)
            }

            selecaoAdapter.submitList(itensParaSelecao)
            calcularValorTotal()
        }
    }

    private fun calcularValorTotal() {
        val itensAtuais = selecaoAdapter.currentList
        var valorTotal = 0.0f
        itensAtuais.forEach { item ->
            valorTotal += item.produto.value * item.quantidade
        }
        valorTotalTextView.text = getString(R.string.formato_valor_reais, valorTotal)
    }

    /**
     * Esta função agora cria um objeto Order com o status "Finalizado".
     */
    private fun salvarAlteracoes() {
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

        val listaDeIdsDosItens = mutableListOf<Int>()
        var valorTotal = 0.0f
        itensSelecionados.forEach { item ->
            repeat(item.quantidade) {
                listaDeIdsDosItens.add(item.produto.id)
            }
            valorTotal += item.produto.value * item.quantidade
        }

        // --- PONTO CHAVE DA FUNCIONALIDADE ---
        // Criamos o objeto Order com o status "Finalizado".
        val pedidoAtualizado = Order(
            id = pedidoId,
            orderTime = Date(),
            clientName = nomeCliente,
            items = listaDeIdsDosItens,
            totalValue = valorTotal,
            tableNumber = mesa,
            status = "Finalizado" // Define o status como finalizado
        )

        // O ViewModel irá ATUALIZAR o pedido na base de dados.
        pedidosViewModel.update(pedidoAtualizado)

        Toast.makeText(this, "Pedido finalizado com sucesso!", Toast.LENGTH_LONG).show()
        finish() // Fecha a tela de edição
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}
