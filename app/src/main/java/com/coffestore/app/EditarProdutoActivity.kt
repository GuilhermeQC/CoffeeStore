package com.coffestore.app
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.coffestore.app.data.AppDatabase
import com.coffestore.app.data.Product
import com.coffestore.app.repositories.ProductRepository
import com.coffestore.app.viewmodels.ProdutosViewModel
import com.coffestore.app.viewmodels.ProdutosViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class EditarProdutoActivity : AppCompatActivity() {

    private val produtosViewModel: ProdutosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = ProductRepository(database.productDao())
        ProdutosViewModelFactory(repository)
    }

    // Variáveis para guardar os dados do produto
    private var produtoId: Int = -1
    private var produtoAtual: Product? = null
    private var imagemSelecionadaUri: Uri? = null

    // Views do layout
    private lateinit var nomeEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var valorEditText: EditText
    private lateinit var inativarCheckBox: CheckBox
    private lateinit var tituloTextView: TextView
    private lateinit var fotoImageView: ImageView

    // Lançador para a galeria de imagens
    private val selecionarImagemLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Pede permissão persistente para o URI selecionado
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imagemSelecionadaUri = it
            fotoImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novo_produto) // Reutilizamos o mesmo layout

        setupImmersiveMode()

        produtoId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)

        if (produtoId == -1) {
            Toast.makeText(this, "Erro: ID do produto não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Encontra as Views
        nomeEditText = findViewById(R.id.edit_text_nome_produto)
        descricaoEditText = findViewById(R.id.edit_text_descricao)
        valorEditText = findViewById(R.id.edit_text_valor)
        inativarCheckBox = findViewById(R.id.checkbox_inativar)
        // Lembre-se de dar um ID ao TextView do título no seu novo_produto.xml
        tituloTextView = findViewById(R.id.text_view_titulo)
        fotoImageView = findViewById(R.id.image_view_selecionar_foto)
        val salvarButton = findViewById<MaterialButton>(R.id.button_salvar_produto)
        val voltarButton = findViewById<ImageView>(R.id.button_voltar)

        // Ajusta o título e o texto do botão
        tituloTextView.text = "Editar Produto"
        salvarButton.text = "Salvar Alterações"

        carregarDadosDoProduto()

        // Configura o clique para permitir a troca da imagem
        fotoImageView.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        salvarButton.setOnClickListener {
            salvarAlteracoes()
        }

        voltarButton.setOnClickListener {
            finish()
        }

    }

    private fun carregarDadosDoProduto() {
        lifecycleScope.launch {
            produtoAtual = produtosViewModel.getProductById(produtoId)
            produtoAtual?.let { product ->
                // Preenche os campos de texto
                nomeEditText.setText(product.name)
                descricaoEditText.setText(product.description)
                valorEditText.setText(product.value.toString())
                inativarCheckBox.isChecked = !product.isActive

                // Carrega a imagem existente, se houver
                if (product.photoUri.isNotEmpty()) {
                    try {
                        val fotoUri = Uri.parse(product.photoUri)
                        imagemSelecionadaUri = fotoUri // Guarda o URI atual
                        fotoImageView.setImageURI(fotoUri)
                    } catch (e: Exception) {
                        // Se houver erro ao carregar a imagem, usa a padrão
                        fotoImageView.setImageResource(R.drawable.image_include)
                    }
                }
            }
        }
    }

    private fun salvarAlteracoes() {
        val nome = nomeEditText.text.toString().trim()
        val descricao = descricaoEditText.text.toString().trim()
        val valorStr = valorEditText.text.toString().trim()

        if (nome.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Nome e valor são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        val valor = valorStr.toFloatOrNull()
        if (valor == null) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        val estaAtivo = !inativarCheckBox.isChecked

        // Usa o URI da nova imagem se uma foi selecionada, senão mantém o antigo
        val fotoUriString = imagemSelecionadaUri?.toString() ?: ""

        val produtoAtualizado = Product(
            id = produtoId,
            name = nome,
            description = descricao,
            value = valor,
            isActive = estaAtivo,
            photoUri = fotoUriString
        )

        produtosViewModel.update(produtoAtualizado)

        Toast.makeText(this, "Produto atualizado com sucesso!", Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    private fun setupImmersiveMode() {
        // Permite que o app desenhe sob as barras do sistema.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Esconde as barras do sistema.
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        // Define o comportamento para quando o utilizador desliza o dedo das bordas.
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Adiciona um listener para ajustar o padding do layout, evitando que o conteúdo
        // fique sob as barras do sistema e corrigindo problemas de toque.
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Ajusta o padding para o conteúdo não ficar sob as barras do sistema.
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

}