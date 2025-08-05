package com.coffestore.app.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffestore.app.EditarPedidoActivity
import com.coffestore.app.R
import com.coffestore.app.data.Order
import java.text.SimpleDateFormat
import java.util.Locale

class PedidosAdapter : ListAdapter<Order, PedidosAdapter.PedidoViewHolder>(PedidosDiffCallback()) {

    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numeroPedidoView: TextView = itemView.findViewById(R.id.text_view_numero_pedido)
        private val horaClienteView: TextView = itemView.findViewById(R.id.text_view_hora_cliente)
        private val itensView: TextView = itemView.findViewById(R.id.text_view_itens)
        private val valorView: TextView = itemView.findViewById(R.id.text_view_valor)
        private val mesaView: TextView = itemView.findViewById(R.id.text_view_mesa)
        private val editarView: ImageView = itemView.findViewById(R.id.image_view_editar)

        fun bind(order: Order) {
            val context = itemView.context
            val horaFormatada = SimpleDateFormat("HH:mm", Locale.getDefault()).format(order.orderTime)

            numeroPedidoView.text = context.getString(R.string.formato_numero_pedido, order.id)
            horaClienteView.text = context.getString(R.string.formato_hora_cliente, horaFormatada, order.clientName)

            itensView.text = context.getString(R.string.formato_itens, order.items.joinToString(", "))

            valorView.text = context.getString(R.string.formato_valor, order.totalValue)
            mesaView.text = context.getString(R.string.formato_mesa, order.tableNumber)

            // --- LÓGICA DE EDIÇÃO ADICIONADA AQUI ---
            editarView.setOnClickListener {
                val intent = Intent(context, EditarPedidoActivity::class.java)
                // Anexa o ID do pedido à intenção
                intent.putExtra(EditarPedidoActivity.EXTRA_ORDER_ID, order.id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }
}

class PedidosDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}
