package com.coffestore.app;

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
import com.coffestore.app.adapters.ProdutosAdapter
import com.coffestore.app.data.AppDatabase
import com.coffestore.app.data.Product
import com.coffestore.app.repositories.ProductRepository
import com.coffestore.app.viewmodels.ProdutosViewModel
import com.coffestore.app.viewmodels.ProdutosViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProdutosActivity : AppCompatActivity() {

    // 1. Inicializa o ViewModel usando a delegação 'by viewModels' e nossa Factory.
    private val produtosViewModel: ProdutosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = ProductRepository(database.productDao())
        ProdutosViewModelFactory(repository)
    }

    // Declara o adapter.
    private lateinit var produtosAdapter: ProdutosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.produtos)

        // Configura o modo de tela cheia.
        setupImmersiveMode()

        //Chama as funções para configurar a tela.
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()

        //Teste inserção de dados
        //insertTestData()

        setupNavigation()
    }

    private fun setupNavigation() {
        val navPedidos = findViewById<LinearLayout>(R.id.nav_pedidos)
        val navNovoPedido = findViewById<LinearLayout>(R.id.nav_novo_pedido)
        val navProdutos = findViewById<LinearLayout>(R.id.nav_produtos)

        // Produtos
        navPedidos.setOnClickListener {
            val intent = Intent(this, PedidosActivity::class.java)
            // Esta flag evita que uma nova tela seja criada se ela já estiver aberta,
            // simplesmente a trazendo para a frente.
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        // Novo Pedido
        navNovoPedido.setOnClickListener {
            val intent = Intent(this, NovoPedidoActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupRecyclerView() {
        produtosAdapter = ProdutosAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_produtos)
        recyclerView.adapter = produtosAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        // Observa a lista de produtos do ViewModel.
        lifecycleScope.launch {
            produtosViewModel.allProducts.collectLatest { products ->
                // Quando a lista muda, a envia para o adapter.
                produtosAdapter.submitList(products)
            }
        }
    }

    private fun setupClickListeners() {
        val novoProdutoButton = findViewById<MaterialButton>(R.id.button_novo_produto)
        novoProdutoButton.setOnClickListener {
            // Inicia a NovoProdutoActivity
            val intent = Intent(this, NovoProdutoActivity::class.java)
            startActivity(intent)
        }
    }

// Inserção de produtos testes
//    private fun insertTestData() {
//        lifecycleScope.launch {
//            // Para evitar inserir os dados toda vez, podemos checar se a lista está vazia.
//            if (produtosViewModel.allActiveProducts.value.isEmpty()) {
//                produtosViewModel.insert(Product(name = "Espresso Duplo", photoUri = "", description = "Café forte e encorpado, dose dupla de energia.", value = 8.50f, isActive = true))
//                produtosViewModel.insert(Product(name = "Cappuccino Cremoso", photoUri = "", description = "A combinação perfeita de café, leite vaporizado e espuma.", value = 12.00f, isActive = true))
//                produtosViewModel.insert(Product(name = "Pão de Queijo", photoUri = "", description = "Tradicional e quentinho, direto do forno.", value = 5.00f, isActive = false))
//                produtosViewModel.insert(Product(name = "Torta de Frango", photoUri = "", description = "Fatia generosa de torta com recheio cremoso.", value = 15.00f, isActive = true))
//            }
//        }
//    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val mainLayout = findViewById<View>(R.id.main_layout_produtos)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
