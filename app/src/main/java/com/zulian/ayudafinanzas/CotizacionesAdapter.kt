package com.zulian.ayudafinanzas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.CotizacionDisplay

class CotizacionesAdapter(private var cotizaciones: List<CotizacionDisplay>) : RecyclerView.Adapter<CotizacionesAdapter.CotizacionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CotizacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cotizacion, parent, false)
        return CotizacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CotizacionViewHolder, position: Int) {
        holder.bind(cotizaciones[position])
    }

    override fun getItemCount(): Int = cotizaciones.size

    fun updateData(newCotizaciones: List<CotizacionDisplay>) {
        cotizaciones = newCotizaciones
        notifyDataSetChanged()
    }

    class CotizacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fechaTextView: TextView = itemView.findViewById(R.id.textViewFecha)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val tipoCotizacionTextView: TextView = itemView.findViewById(R.id.textViewTipoCotizacion)

        fun bind(cotizacion: CotizacionDisplay) {
            fechaTextView.text = cotizacion.fecha
            descripcionTextView.text = cotizacion.descripcion
            tipoCotizacionTextView.text = String.format("%.2f", cotizacion.tipoCotizacion)
        }
    }
}