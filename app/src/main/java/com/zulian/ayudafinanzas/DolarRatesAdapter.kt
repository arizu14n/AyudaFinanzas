package com.zulian.ayudafinanzas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.DolarRate

class DolarRatesAdapter(private var rates: List<DolarRate>) : RecyclerView.Adapter<DolarRatesAdapter.DolarRateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DolarRateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dolar_rate, parent, false)
        return DolarRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DolarRateViewHolder, position: Int) {
        holder.bind(rates[position])
    }

    override fun getItemCount(): Int = rates.size

    fun updateRates(newRates: List<DolarRate>) {
        rates = newRates
        notifyDataSetChanged()
    }

    class DolarRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dolarTypeTextView: TextView = itemView.findViewById(R.id.textViewDolarType)
        private val lastUpdateTextView: TextView = itemView.findViewById(R.id.textViewLastUpdate)
        private val buyValueTextView: TextView = itemView.findViewById(R.id.textViewBuyValue)
        private val sellValueTextView: TextView = itemView.findViewById(R.id.textViewSellValue)

        fun bind(rate: DolarRate) {
            dolarTypeTextView.text = rate.nombre
            lastUpdateTextView.text = "Última actualización: ${formatDate(rate.fechaActualizacion)}"
            buyValueTextView.text = String.format("%.2f", rate.compra)
            sellValueTextView.text = String.format("%.2f", rate.venta)
        }

        private fun formatDate(isoDate: String): String {
            // Expected ISO 8601 format: 2023-10-27T10:00:00.000Z
            // Desired format: dd/MM/yyyy HH:mm
            return try {
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = parser.parse(isoDate)
                val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                formatter.format(date)
            } catch (e: Exception) {
                isoDate // Return original if parsing fails
            }
        }
    }
}
