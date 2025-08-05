package com.coffestore.app.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffestore.app.R
import com.coffestore.app.data.ItemSelecaoProduto

class SelecaoProdutosAdapter(
    private val onQuantidadeAlterada: (ItemSelecaoProduto) -> Unit
) : ListAdapter<ItemSelecaoProduto, SelecaoProdutosAdapter.ItemViewHolder>(ItemDiffCallback()) {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagemProdutoView: ImageView = itemView.findViewById(R.id.image_view_produto)
        private val nomeProdutoView: TextView = itemView.findViewById(R.id.text_view_nome_produto)
        private val valorView: TextView = itemView.findViewById(R.id.text_view_valor_produto)
        private val quantidadeView: TextView = itemView.findViewById(R.id.text_view_quantidade)
        private val diminuirButton: ImageButton = itemView.findViewById(R.id.button_diminuir)
        private val aumentarButton: ImageButton = itemView.findViewById(R.id.button_aumentar)

        fun bind(item: ItemSelecaoProduto, onQuantidadeAlterada: (ItemSelecaoProduto) -> Unit) {
            val context = itemView.context
            val produto = item.produto

            // Preenche os dados do produto
            nomeProdutoView.text = produto.name
            valorView.text = context.getString(R.string.formato_valor_produto, produto.value)
            quantidadeView.text = item.quantidade.toString()

            // Carrega a imagem do produto
            if (produto.photoUri.isNotEmpty()) {
                try {
                    imagemProdutoView.setImageURI(Uri.parse(produto.photoUri))
                } catch (e: Exception) {
                    imagemProdutoView.setImageResource(R.drawable.image_include)
                }
            } else {
                imagemProdutoView.setImageResource(R.drawable.image_include)
            }

            // Listener para o botão de aumentar quantidade
            aumentarButton.setOnClickListener {
                item.quantidade++
                quantidadeView.text = item.quantidade.toString()
                onQuantidadeAlterada(item) // Notifica a Activity da alteração
            }

            // Listener para o botão de diminuir quantidade
            diminuirButton.setOnClickListener {
                if (item.quantidade > 0) {
                    item.quantidade--
                    quantidadeView.text = item.quantidade.toString()
                    onQuantidadeAlterada(item) // Notifica a Activity da alteração
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto_selecao, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onQuantidadeAlterada)
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<ItemSelecaoProduto>() {
    override fun areItemsTheSame(oldItem: ItemSelecaoProduto, newItem: ItemSelecaoProduto): Boolean {
        return oldItem.produto.id == newItem.produto.id
    }

    override fun areContentsTheSame(oldItem: ItemSelecaoProduto, newItem: ItemSelecaoProduto): Boolean {
        return oldItem == newItem
    }
}
