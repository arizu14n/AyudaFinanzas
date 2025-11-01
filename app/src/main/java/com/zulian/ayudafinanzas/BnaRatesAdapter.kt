package com.zulian.ayudafinanzas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.BnaRate

class BnaRatesAdapter(private var rates: List<BnaRate>) : RecyclerView.Adapter<BnaRatesAdapter.BnaRateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BnaRateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bna_rate, parent, false)
        return BnaRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: BnaRateViewHolder, position: Int) {
        holder.bind(rates[position])
    }

    override fun getItemCount(): Int = rates.size

    fun updateRates(newRates: List<BnaRate>) {
        rates = newRates
        notifyDataSetChanged()
    }

    class BnaRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val currencyTextView: TextView = itemView.findViewById(R.id.textViewCurrency)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val buyValueTextView: TextView = itemView.findViewById(R.id.textViewBuyValue)
        private val sellValueTextView: TextView = itemView.findViewById(R.id.textViewSellValue)

        fun bind(rate: BnaRate) {
            currencyTextView.text = rate.currency
            dateTextView.text = rate.date
            buyValueTextView.text = rate.buy
            sellValueTextView.text = rate.sell
        }
    }
}
