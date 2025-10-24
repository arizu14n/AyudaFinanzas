package com.zulian.ayudafinanzas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zulian.ayudafinanzas.data.check.Check

class ChequeListAdapter(
    private var cheques: List<Check>,
    private val onUpdateClick: (Check) -> Unit,
    private val onDeleteClick: (Check) -> Unit
) : RecyclerView.Adapter<ChequeListAdapter.ChequeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChequeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cheque, parent, false)
        return ChequeViewHolder(view, onUpdateClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ChequeViewHolder, position: Int) {
        holder.bind(cheques[position])
    }

    override fun getItemCount(): Int = cheques.size

    fun updateData(newCheques: List<Check>) {
        cheques = newCheques
        notifyDataSetChanged()
    }

    class ChequeViewHolder(
        itemView: View,
        private val onUpdateClick: (Check) -> Unit,
        private val onDeleteClick: (Check) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nroTextView: TextView = itemView.findViewById(R.id.textViewChequeNro)
        private val bancoTextView: TextView = itemView.findViewById(R.id.textViewChequeBanco)
        private val importeTextView: TextView = itemView.findViewById(R.id.textViewChequeImporte)
        private val estadoTextView: TextView = itemView.findViewById(R.id.textViewChequeEstado)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewCheque)
        private val updateButton: Button = itemView.findViewById(R.id.buttonUpdate)
        private val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(cheque: Check) {
            nroTextView.text = "Nro: ${cheque.nro}"
            bancoTextView.text = cheque.banco
            importeTextView.text = "$${cheque.importe}"
            estadoTextView.text = "Estado: ${cheque.estado}"

            Glide.with(itemView.context)
                .load(cheque.imagenUrl)
                .placeholder(R.drawable.ic_launcher_background) // Imagen de carga
                .into(imageView)

            updateButton.setOnClickListener { onUpdateClick(cheque) }
            deleteButton.setOnClickListener { onDeleteClick(cheque) }
        }
    }
}