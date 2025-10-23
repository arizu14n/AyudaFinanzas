package com.zulian.ayudafinanzas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zulian.ayudafinanzas.data.CotizacionDisplay
import com.zulian.ayudafinanzas.data.CotizacionesResponse
import com.zulian.ayudafinanzas.data.CotizacionesResult
import com.zulian.ayudafinanzas.data.Divisa
import com.zulian.ayudafinanzas.data.DivisaResponse
import com.zulian.ayudafinanzas.data.ErrorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EstadisticasCambiariasActivity : AppCompatActivity() {

    private lateinit var spinnerDivisas: Spinner
    private lateinit var editTextDateFrom: EditText
    private lateinit var editTextDateTo: EditText
    private lateinit var buttonConsultar: Button
    private lateinit var recyclerViewCotizaciones: RecyclerView
    private lateinit var textViewStatus: TextView
    private lateinit var textViewLabelDateFrom: TextView
    private lateinit var textViewLabelDateTo: TextView

    private lateinit var cotizacionesAdapter: CotizacionesAdapter
    private val divisas = mutableListOf<Divisa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas_cambiarias)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerDivisas = findViewById(R.id.spinnerDivisas)
        editTextDateFrom = findViewById(R.id.editTextDateFrom)
        editTextDateTo = findViewById(R.id.editTextDateTo)
        buttonConsultar = findViewById(R.id.buttonConsultar)
        recyclerViewCotizaciones = findViewById(R.id.recyclerViewCotizaciones)
        textViewStatus = findViewById(R.id.textViewStatus)
        textViewLabelDateFrom = findViewById(R.id.textViewLabelDateFrom)
        textViewLabelDateTo = findViewById(R.id.textViewLabelDateTo)

        recyclerViewCotizaciones.layoutManager = LinearLayoutManager(this)
        cotizacionesAdapter = CotizacionesAdapter(emptyList())
        recyclerViewCotizaciones.adapter = cotizacionesAdapter

        setupDatePickerListeners()
        buttonConsultar.setOnClickListener { consultarCotizaciones() }

        fetchDivisas()
    }

    private fun setupDatePickerListeners() {
        val calendar = Calendar.getInstance()
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uiDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val dateSetListenerFrom = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editTextDateFrom.setText(uiDateFormat.format(calendar.time))
            editTextDateFrom.tag = apiDateFormat.format(calendar.time)
        }

        val dateSetListenerTo = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editTextDateTo.setText(uiDateFormat.format(calendar.time))
            editTextDateTo.tag = apiDateFormat.format(calendar.time)
        }

        editTextDateFrom.setOnClickListener {
            DatePickerDialog(this, dateSetListenerFrom, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        editTextDateTo.setOnClickListener {
            DatePickerDialog(this, dateSetListenerTo, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun fetchDivisas() {
        ApiClient.apiService.getDivisas().enqueue(object : Callback<DivisaResponse> {
            override fun onResponse(call: Call<DivisaResponse>, response: Response<DivisaResponse>) {
                if (response.isSuccessful) {
                    val divisaResponse = response.body()
                    if (divisaResponse != null) {
                        divisas.clear()
                        divisas.add(Divisa("TODAS", "TODAS"))
                        val divisasOrdenadas = divisaResponse.results.sortedBy { it.denominacion }
                        divisas.addAll(divisasOrdenadas)
                        setupSpinner()
                        textViewStatus.visibility = View.GONE
                    } else {
                        showError("La respuesta de la API de Divisas está vacía.")
                    }
                } else {
                    handleApiError(response)
                }
            }

            override fun onFailure(call: Call<DivisaResponse>, t: Throwable) {
                showError("Error de red: ${t.message}")
            }
        })
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, divisas.map { it.denominacion })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDivisas.adapter = adapter
        spinnerDivisas.setSelection(0)

        spinnerDivisas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cotizacionesAdapter.updateData(emptyList())
                textViewStatus.visibility = View.GONE

                val selectedDivisa = divisas[position]
                if (selectedDivisa.codigo == "TODAS") {
                    textViewLabelDateFrom.text = "Fecha:"
                    textViewLabelDateTo.visibility = View.GONE
                    editTextDateTo.visibility = View.GONE
                } else {
                    textViewLabelDateFrom.text = "Fecha Desde:"
                    textViewLabelDateTo.visibility = View.VISIBLE
                    editTextDateTo.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun consultarCotizaciones() {
        if (divisas.isEmpty()) {
            Toast.makeText(this, "Espere a que las divisas terminen de cargar", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDivisa = divisas[spinnerDivisas.selectedItemPosition]
        val fechaDesde = editTextDateFrom.tag?.toString()
        val fechaHasta = editTextDateTo.tag?.toString()

        if (selectedDivisa.codigo == "TODAS") {
            if (fechaDesde.isNullOrEmpty()) {
                Toast.makeText(this, "Por favor, seleccione la fecha a consultar", Toast.LENGTH_SHORT).show()
                return
            }
            ApiClient.apiService.getCotizaciones(fechaDesde).enqueue(apiCallback)
        } else {
            if (fechaDesde.isNullOrEmpty() || fechaHasta.isNullOrEmpty()) {
                Toast.makeText(this, "Por favor, seleccione fecha desde y hasta", Toast.LENGTH_SHORT).show()
                return
            }
            ApiClient.apiService.getCotizacionesPorDivisa(selectedDivisa.codigo, fechaDesde, fechaHasta).enqueue(apiCallback)
        }
    }

    private val apiCallback = object : Callback<CotizacionesResponse> {
        override fun onResponse(call: Call<CotizacionesResponse>, response: Response<CotizacionesResponse>) {
            if (response.isSuccessful) {
                val cotizacionesResponse = response.body()
                if (cotizacionesResponse != null) {
                    try {
                        val displayList = parseAndTransform(cotizacionesResponse)
                        
                        if (displayList.isEmpty()) {
                            showError("No se encontraron cotizaciones para la fecha seleccionada.")
                        } else {
                            cotizacionesAdapter.updateData(displayList)
                            textViewStatus.visibility = View.GONE
                        }

                    } catch (e: Exception) {
                        showError("Error al procesar la respuesta: ${e.message}")
                    }
                } else {
                    showError("La respuesta de la API de Cotizaciones está vacía.")
                }
            } else {
                handleApiError(response)
            }
        }

        override fun onFailure(call: Call<CotizacionesResponse>, t: Throwable) {
            showError("Error de red: ${t.message}")
        }
    }

    private fun parseAndTransform(response: CotizacionesResponse): List<CotizacionDisplay> {
        val gson = Gson()
        val resultsJson = gson.toJson(response.results)
        val resultsList = mutableListOf<CotizacionesResult>()

        if (response.results is List<*>) {
            val typeToken = object : TypeToken<List<CotizacionesResult>>() {}.type
            resultsList.addAll(gson.fromJson(resultsJson, typeToken))
        } else {
            resultsList.add(gson.fromJson(resultsJson, CotizacionesResult::class.java))
        }

        val displayList = mutableListOf<CotizacionDisplay>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        resultsList.forEach { result ->
            if (result.fecha != null) {
                val date = inputFormat.parse(result.fecha)
                val formattedDate = if (date != null) outputFormat.format(date) else ""
                result.detalle.forEach {
                    displayList.add(
                        CotizacionDisplay(
                            fecha = formattedDate,
                            descripcion = it.descripcion,
                            tipoCotizacion = it.tipoCotizacion
                        )
                    )
                }
            }
        }
        return displayList
    }

    private fun <T> handleApiError(response: Response<T>) {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                if (errorResponse != null && errorResponse.errorMessages.isNotEmpty()) {
                    showError("Error de API: ${errorResponse.errorMessages.joinToString()}")
                    return
                }
            } catch (e: Exception) {
                // Ignorar
            }
        }
        showError("Error en la respuesta de la API: ${response.code()}")
    }

    private fun showError(message: String) {
        textViewStatus.text = message
        textViewStatus.visibility = View.VISIBLE
        cotizacionesAdapter.updateData(emptyList())
    }
}