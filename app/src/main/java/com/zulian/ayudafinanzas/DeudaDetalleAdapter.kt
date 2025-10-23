package com.zulian.ayudafinanzas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeudaDetalleAdapter(private var detalles: List<Pair<String, Boolean>>) : RecyclerView.Adapter<DeudaDetalleAdapter.DetalleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deuda_detalle, parent, false)
        return DetalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        holder.bind(detalles[position])
    }

    override fun getItemCount(): Int = detalles.size

    fun updateData(newDetalles: List<Pair<String, Boolean>>) {
        detalles = newDetalles
        notifyDataSetChanged()
    }

    class DetalleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.textViewDetalleLabel)
        private val valorImageView: ImageView = itemView.findViewById(R.id.imageViewDetalleValor)

        fun bind(detalle: Pair<String, Boolean>) {
            labelTextView.text = detalle.first
            val resourceId = if (detalle.second) R.drawable.ic_stop else R.drawable.ic_pass
            valorImageView.setImageResource(resourceId)
        }
    }
}