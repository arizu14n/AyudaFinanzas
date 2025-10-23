package com.zulian.ayudafinanzas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.EntidadDeuda
import java.text.SimpleDateFormat
import java.util.Locale

class DeudasAdapter(
    private var deudas: List<EntidadDeuda>,
    private val onInfoClickListener: (EntidadDeuda) -> Unit
) : RecyclerView.Adapter<DeudasAdapter.DeudaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeudaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deuda, parent, false)
        return DeudaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeudaViewHolder, position: Int) {
        val deuda = deudas[position]
        holder.bind(deuda, onInfoClickListener)
    }

    override fun getItemCount(): Int = deudas.size

    fun updateData(newDeudas: List<EntidadDeuda>) {
        deudas = newDeudas
        notifyDataSetChanged()
    }

    class DeudaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val entidadTextView: TextView = itemView.findViewById(R.id.textViewEntidad)
        private val situacionTextView: TextView = itemView.findViewById(R.id.textViewSituacion)
        private val fechaSitTextView: TextView = itemView.findViewById(R.id.textViewFechaSit)
        private val montoTextView: TextView = itemView.findViewById(R.id.textViewMonto)

        fun bind(deuda: EntidadDeuda, clickListener: (EntidadDeuda) -> Unit) {
            entidadTextView.text = deuda.entidad
            situacionTextView.text = deuda.situacion.toString()
            
            // FORMATEO DE FECHA
            fechaSitTextView.text = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MM/yy", Locale.getDefault())
                val date = inputFormat.parse(deuda.fechaSit1!!)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                deuda.fechaSit1 ?: "-" // Si falla o es nula, muestra el original o un guión
            }
            
            montoTextView.text = String.format("$%.2f", deuda.monto)

            if (deuda.situacion > 1) {
                situacionTextView.isClickable = true
                situacionTextView.setOnClickListener { clickListener(deuda) }
                situacionTextView.background = ContextCompat.getDrawable(itemView.context, R.drawable.button_background_circle)
                situacionTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            } else {
                situacionTextView.isClickable = false
                situacionTextView.setOnClickListener(null)
                situacionTextView.background = null
                situacionTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.tab_indicator_text))
            }
        }
    }
}