package com.coffestore.app.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffestore.app.EditarProdutoActivity
import com.coffestore.app.R
import com.coffestore.app.data.Product

class ProdutosAdapter : ListAdapter<Product, ProdutosAdapter.ProdutoViewHolder>(ProdutosDiffCallback()) {

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagemProdutoView: ImageView = itemView.findViewById(R.id.image_view_produto)
        private val nomeProdutoView: TextView = itemView.findViewById(R.id.text_view_nome_produto)
        private val statusView: TextView = itemView.findViewById(R.id.text_view_status)
        private val descricaoView: TextView = itemView.findViewById(R.id.text_view_descricao)
        private val valorView: TextView = itemView.findViewById(R.id.text_view_valor_produto)
        private val editarView: ImageView = itemView.findViewById(R.id.image_view_editar_produto)

        fun bind(product: Product) {
            val context = itemView.context
            nomeProdutoView.text = product.name
            descricaoView.text = product.description
            valorView.text = context.getString(R.string.formato_valor_produto, product.value)

            if (product.isActive) {
                statusView.text = context.getString(R.string.status_ativo)
                statusView.setTextColor(context.getColor(R.color.status_ativo_color))
            } else {
                statusView.text = context.getString(R.string.status_inativo)
                statusView.setTextColor(context.getColor(R.color.status_inativo_color))
            }

            // --- LÓGICA PARA CARREGAR A IMAGEM (MAIS ROBUSTA) ---
            try {
                if (product.photoUri.isNotEmpty()) {
                    // Se houver um URI guardado, converte a String de volta para Uri e mostra a imagem
                    imagemProdutoView.setImageURI(Uri.parse(product.photoUri))
                } else {
                    // Se não houver foto, mostra a imagem padrão
                    imagemProdutoView.setImageResource(R.drawable.image_include)
                }
            } catch (e: SecurityException) {
                // Se ocorrer um erro de permissão, mostramos a imagem padrão para evitar o crash.
                imagemProdutoView.setImageResource(R.drawable.image_include)
                // Opcional: Logar o erro para depuração
                Log.e("ProdutosAdapter", "Erro de permissão ao carregar a imagem: ${product.photoUri}", e)
            }

            editarView.setOnClickListener {
                val intent = Intent(context, EditarProdutoActivity::class.java)
                intent.putExtra(EditarProdutoActivity.EXTRA_PRODUCT_ID, product.id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }
}

class ProdutosDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}