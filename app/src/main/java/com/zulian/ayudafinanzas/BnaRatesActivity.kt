package com.zulian.ayudafinanzas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zulian.ayudafinanzas.data.BnaRate
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BnaRatesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: BnaRatesAdapter
    private lateinit var datePickerButton: Button

    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bna_rates)

        recyclerView = findViewById(R.id.recyclerViewRates)
        progressBar = findViewById(R.id.progressBar)
        datePickerButton = findViewById(R.id.buttonDatePicker)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BnaRatesAdapter(emptyList())
        recyclerView.adapter = adapter

        datePickerButton.setOnClickListener {
            showDatePickerDialog()
        }

        fetchBnaRates(Date())
    }

    private fun fetchBnaRates(date: Date) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(date)

        BNAApiClient.apiService.getHistoricoCotizaciones(fecha = formattedDate).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let {
                        val html = it.string()
                        val rates = parseBnaHtml(html)
                        if (rates.isEmpty()) {
                            showToast("Disculpa, no encontré valores para la fecha seleccionada.")
                        } else {
                            adapter.updateRates(rates)
                            recyclerView.visibility = View.VISIBLE
                        }
                    } ?: run {
                        showError()
                    }
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError()
            }
        })
    }

    private fun parseBnaHtml(html: String): List<BnaRate> {
        val document = Jsoup.parse(html)
        val rates = mutableListOf<BnaRate>()

        val dolarTable = document.select("#tablaDolar tbody tr")
        for (row in dolarTable) {
            val columns = row.select("td")
            if (columns.size == 4) {
                rates.add(
                    BnaRate(
                        currency = columns[0].text(),
                        buy = columns[1].text(),
                        sell = columns[2].text(),
                        date = columns[3].text()
                    )
                )
            }
        }

        val euroTable = document.select("#tablaEuro tbody tr")
        for (row in euroTable) {
            val columns = row.select("td")
            if (columns.size == 4) {
                rates.add(
                    BnaRate(
                        currency = columns[0].text(),
                        buy = columns[1].text(),
                        sell = columns[2].text(),
                        date = columns[3].text()
                    )
                )
            }
        }

        return rates
    }

    private fun showDatePickerDialog() {
        val today = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            if (selectedDate.after(today)) {
                showToast("¡Ups! Solo puedes elegir fechas de hoy o anteriores.")
                return@OnDateSetListener
            }

            fetchBnaRates(selectedDate.time)
        }

        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = today.timeInMillis
        datePickerDialog.show()
    }

    private fun showError() {
        Toast.makeText(this, "Error al cargar las cotizaciones", Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}