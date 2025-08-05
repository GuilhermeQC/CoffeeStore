package com.coffestore.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.coffestore.app.data.AppDatabase
import com.coffestore.app.data.Product
import com.coffestore.app.repositories.ProductRepository
import com.coffestore.app.viewmodels.ProdutosViewModel
import com.coffestore.app.viewmodels.ProdutosViewModelFactory
import com.google.android.material.button.MaterialButton

class NovoProdutoActivity : AppCompatActivity() {

    private val produtosViewModel: ProdutosViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = ProductRepository(database.productDao())
        ProdutosViewModelFactory(repository)
    }

    private lateinit var nomeEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var valorEditText: EditText
    private lateinit var inativarCheckBox: CheckBox
    private lateinit var fotoImageView: ImageView

    private var imagemSelecionadaUri: Uri? = null

    private val selecionarImagemLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // --- CORREÇÃO APLICADA AQUI ---
            // Pegamos a permissão persistente para este URI.
            val contentResolver = applicationContext.contentResolver
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // --------------------------------

            imagemSelecionadaUri = it
            fotoImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novo_produto)

        nomeEditText = findViewById(R.id.edit_text_nome_produto)
        descricaoEditText = findViewById(R.id.edit_text_descricao)
        valorEditText = findViewById(R.id.edit_text_valor)
        inativarCheckBox = findViewById(R.id.checkbox_inativar)
        fotoImageView = findViewById(R.id.image_view_selecionar_foto)
        val salvarButton = findViewById<MaterialButton>(R.id.button_salvar_produto)
        val voltarButton = findViewById<ImageView>(R.id.button_voltar)

        fotoImageView.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        salvarButton.setOnClickListener {
            salvarProduto()
        }

        voltarButton.setOnClickListener {
            finish()
        }
    }

    private fun salvarProduto() {
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
        val fotoUriString = imagemSelecionadaUri?.toString() ?: ""

        val novoProduto = Product(
            name = nome,
            description = descricao,
            value = valor,
            isActive = estaAtivo,
            photoUri = fotoUriString
        )

        produtosViewModel.insert(novoProduto)

        Toast.makeText(this, "Produto salvo com sucesso!", Toast.LENGTH_LONG).show()
        finish()
    }
}
