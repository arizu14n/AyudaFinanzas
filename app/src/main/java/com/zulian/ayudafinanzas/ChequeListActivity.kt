package com.zulian.ayudafinanzas

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.check.Check
import com.zulian.ayudafinanzas.data.check.CheckResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChequeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var chequeAdapter: ChequeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheque_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewCheques)
        emptyTextView = findViewById(R.id.textViewEmptyList)

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        fetchCheques()
    }

    private fun setupRecyclerView() {
        chequeAdapter = ChequeListAdapter(emptyList(),
            onUpdateClick = { cheque -> showUpdateDialog(cheque) },
            onDeleteClick = { cheque -> showDeleteDialog(cheque) }
        )
        recyclerView.adapter = chequeAdapter
    }

    private fun fetchCheques() {
        AuthApiClient.getApiService(this).getMyCheques().enqueue(object : Callback<List<Check>> {
            override fun onResponse(call: Call<List<Check>>, response: Response<List<Check>>) {
                if (response.isSuccessful) {
                    val cheques = response.body()
                    if (cheques.isNullOrEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyTextView.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyTextView.visibility = View.GONE
                        chequeAdapter.updateData(cheques)
                    }
                } else {
                    showToast("Error al obtener los cheques: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Check>>, t: Throwable) {
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun showUpdateDialog(cheque: Check) {
        val estados = arrayOf("En cartera", "Endosado", "Depositado", "Rechazado", "Reclamado", "Denunciado", "Extraviado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, estados)

        AlertDialog.Builder(this)
            .setTitle("Actualizar Estado")
            .setAdapter(adapter) { dialog, which ->
                val nuevoEstado = estados[which]
                updateCheque(cheque, nuevoEstado)
            }
            .show()
    }

    private fun updateCheque(cheque: Check, nuevoEstado: String) {
        val updateRequest = mapOf("estado" to nuevoEstado)
        AuthApiClient.getApiService(this).updateCheque(cheque.id, updateRequest).enqueue(object : Callback<CheckResponse> {
            override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
                if (response.isSuccessful) {
                    showToast("Cheque actualizado exitosamente")
                    fetchCheques() // Recargar la lista
                } else {
                    showToast("Error al actualizar el cheque: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun showDeleteDialog(cheque: Check) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cheque")
            .setMessage("¿Estás seguro de que deseas eliminar el cheque N° ${cheque.nro}?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCheque(cheque)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCheque(cheque: Check) {
        AuthApiClient.getApiService(this).deleteCheque(cheque.id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    showToast("Cheque eliminado exitosamente")
                    fetchCheques() // Recargar la lista
                } else {
                    showToast("Error al eliminar el cheque: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}