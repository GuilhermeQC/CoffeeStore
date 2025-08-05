package com.coffestore.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffestore.app.R
import com.coffestore.app.adapters.PedidosAdapter
import com.coffestore.app.data.AppDatabase
import com.coffestore.app.data.Order
import com.coffestore.app.repositories.OrderRepository
import com.coffestore.app.viewmodels.PedidosViewModel
import com.coffestore.app.viewmodels.PedidosViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidosActivity : AppCompatActivity() {

    private val pedidosViewModel: PedidosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = OrderRepository(database.orderDao())
        PedidosViewModelFactory(repository)
    }

    private lateinit var pedidosAdapter: PedidosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedidos)


        setupImmersiveMode()

        setupRecyclerView()
        observeViewModel()

        setupNavigation()
    }

    private fun setupNavigation() {
        val navPedidos = findViewById<LinearLayout>(R.id.nav_pedidos)
        val navNovoPedido = findViewById<LinearLayout>(R.id.nav_novo_pedido)
        val navProdutos = findViewById<LinearLayout>(R.id.nav_produtos)

        // Produtos
        navProdutos.setOnClickListener {
            val intent = Intent(this, ProdutosActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        navNovoPedido.setOnClickListener {
            val intent = Intent(this, NovoPedidoActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupImmersiveMode() {

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val mainLayout = findViewById<View>(R.id.main_layout)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupRecyclerView() {
        pedidosAdapter = PedidosAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_todos_pedidos)
        recyclerView.adapter = pedidosAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            pedidosViewModel.allOpenOrders.collectLatest { orders ->
                pedidosAdapter.submitList(orders)
            }
        }

        lifecycleScope.launch {
            pedidosViewModel.latestOpenOrder.collectLatest { latestOrder ->
                bindLatestOrderCard(latestOrder)
            }
        }
    }

    private fun bindLatestOrderCard(order: Order?) {
        val ultimoPedidoCard = findViewById<View>(R.id.card_ultimo_pedido)
        if (order == null) {
            ultimoPedidoCard.visibility = View.GONE
            return
        }
        ultimoPedidoCard.visibility = View.VISIBLE
        val numeroPedidoView: TextView = ultimoPedidoCard.findViewById(R.id.text_view_numero_pedido)
        val horaClienteView: TextView = ultimoPedidoCard.findViewById(R.id.text_view_hora_cliente)
        val itensView: TextView = ultimoPedidoCard.findViewById(R.id.text_view_itens)
        val valorView: TextView = ultimoPedidoCard.findViewById(R.id.text_view_valor)
        val mesaView: TextView = ultimoPedidoCard.findViewById(R.id.text_view_mesa)

        val horaFormatada = SimpleDateFormat("HH:mm", Locale.getDefault()).format(order.orderTime)
        numeroPedidoView.text = getString(R.string.formato_numero_pedido, order.id)
        horaClienteView.text = getString(R.string.formato_hora_cliente, horaFormatada, order.clientName)
        itensView.text = getString(R.string.formato_itens, order.items.joinToString(", "))
        valorView.text = getString(R.string.formato_valor, order.totalValue)
        mesaView.text = getString(R.string.formato_mesa, order.tableNumber)
    }

}