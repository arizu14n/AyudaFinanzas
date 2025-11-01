package com.zulian.ayudafinanzas

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.DolarRate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DolarBlueActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: DolarRatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dolar_blue)

        recyclerView = findViewById(R.id.recyclerViewRates)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DolarRatesAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchDolarRates()
    }

    private fun fetchDolarRates() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        DolarApiClient.apiService.getAllDolarRates().enqueue(object : Callback<List<DolarRate>> {
            override fun onResponse(call: Call<List<DolarRate>>, response: Response<List<DolarRate>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let {
                        adapter.updateRates(it)
                        recyclerView.visibility = View.VISIBLE
                    } ?: run {
                        showError("No se recibieron datos de cotización.")
                    }
                } else {
                    showError("Error en la respuesta del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DolarRate>>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError("Error de red o conexión: ${t.message}")
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error al cargar las cotizaciones: $message", Toast.LENGTH_LONG).show()
    }
}
