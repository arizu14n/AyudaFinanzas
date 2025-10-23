package com.zulian.ayudafinanzas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.zulian.ayudafinanzas.data.ChequeEntidad
import com.zulian.ayudafinanzas.data.ChequeEntidadResponse
import com.zulian.ayudafinanzas.data.ChequeResponse
import com.zulian.ayudafinanzas.data.ErrorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChequesDenunciadosActivity : AppCompatActivity() {

    private lateinit var spinnerEntidades: Spinner
    private lateinit var editTextNumeroCheque: EditText
    private lateinit var buttonConsultar: Button
    private lateinit var layoutResultados: LinearLayout
    private lateinit var layoutExito: LinearLayout
    private lateinit var layoutDenuncia: LinearLayout
    private lateinit var textViewExitoMensaje: TextView
    private lateinit var textViewDenunciaHeader: TextView
    private lateinit var textViewDenunciaDetalle: TextView
    private lateinit var textViewStatus: TextView

    private val entidades = mutableListOf<ChequeEntidad>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheques_denunciados)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de Vistas
        spinnerEntidades = findViewById(R.id.spinnerEntidades)
        editTextNumeroCheque = findViewById(R.id.editTextNumeroCheque)
        buttonConsultar = findViewById(R.id.buttonConsultarCheque)
        layoutResultados = findViewById(R.id.layoutChequeResultados)
        layoutExito = findViewById(R.id.layoutExito)
        layoutDenuncia = findViewById(R.id.layoutDenuncia)
        textViewExitoMensaje = findViewById(R.id.textViewExitoMensaje)
        textViewDenunciaHeader = findViewById(R.id.textViewDenunciaHeader)
        textViewDenunciaDetalle = findViewById(R.id.textViewDenunciaDetalle)
        textViewStatus = findViewById(R.id.textViewChequeStatus)

        setupListeners()
        fetchEntidades()
    }

    private fun setupListeners() {
        buttonConsultar.setOnClickListener { consultarCheque() }

        // Limpiar resultados al cambiar la selección del Spinner
        spinnerEntidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) { resetUI() }
            override fun onNothingSelected(parent: AdapterView<*>?) { resetUI() }
        }

        // Limpiar resultados al escribir en el EditText
        editTextNumeroCheque.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { resetUI() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchEntidades() {
        ApiClient.apiService.getChequeEntidades().enqueue(object : Callback<ChequeEntidadResponse> {
            override fun onResponse(call: Call<ChequeEntidadResponse>, response: Response<ChequeEntidadResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    entidades.clear()
                    entidades.addAll(response.body()!!.results.sortedBy { it.denominacion })
                    val adapter = ArrayAdapter(this@ChequesDenunciadosActivity, android.R.layout.simple_spinner_item, entidades.map { it.denominacion })
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerEntidades.adapter = adapter
                } else {
                    showError("Error al cargar las entidades bancarias.")
                }
            }

            override fun onFailure(call: Call<ChequeEntidadResponse>, t: Throwable) {
                showError("Error de red: ${t.message}")
            }
        })
    }

    private fun consultarCheque() {
        if (entidades.isEmpty()) {
            Toast.makeText(this, "Espere a que las entidades carguen", Toast.LENGTH_SHORT).show()
            return
        }
        val numeroChequeStr = editTextNumeroCheque.text.toString()
        if (numeroChequeStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese un número de cheque", Toast.LENGTH_SHORT).show()
            return
        }

        resetUI()

        val selectedEntidad = entidades[spinnerEntidades.selectedItemPosition]
        val numeroCheque = numeroChequeStr.toLong()

        ApiClient.apiService.getChequeDenunciado(selectedEntidad.codigoEntidad, numeroCheque).enqueue(object : Callback<ChequeResponse> {
            override fun onResponse(call: Call<ChequeResponse>, response: Response<ChequeResponse>) {
                if (response.isSuccessful) {
                    val chequeResponse = response.body()
                    layoutResultados.visibility = View.VISIBLE

                    if (chequeResponse?.results != null && chequeResponse.results.denunciado) {
                        // CHEQUE DENUNCIADO
                        layoutDenuncia.visibility = View.VISIBLE
                        val result = chequeResponse.results
                        val detalle = result.detalles.firstOrNull()
                        textViewDenunciaHeader.text = "Cheque N° ${result.numeroCheque} - ¡DENUNCIADO!"
                        textViewDenunciaDetalle.text = "Entidad: ${result.denominacionEntidad}\n" +
                                "Fecha de Procesamiento: ${result.fechaProcesamiento}\n" +
                                (if(detalle != null) "Sucursal: ${detalle.sucursal}\nCuenta: ${detalle.numeroCuenta}\nCausal: ${detalle.causal}" else "")
                    } else {
                        // CHEQUE NO DENUNCIADO O NO ENCONTRADO
                        layoutExito.visibility = View.VISIBLE
                        textViewExitoMensaje.text = "¡Qué bien! No se registran denuncias para el cheque N° $numeroCheque del banco ${selectedEntidad.denominacion}."
                    }
                } else {
                    handleApiError(response)
                }
            }

            override fun onFailure(call: Call<ChequeResponse>, t: Throwable) {
                showError("Error de red: ${t.message}")
            }
        })
    }

    private fun resetUI() {
        layoutResultados.visibility = View.GONE
        layoutExito.visibility = View.GONE
        layoutDenuncia.visibility = View.GONE
        textViewStatus.visibility = View.GONE
    }

    private fun <T> handleApiError(response: Response<T>) {
        val errorBody = response.errorBody()?.string()
        try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            if (errorResponse != null && errorResponse.errorMessages.isNotEmpty()) {
                showError("Error de API: ${errorResponse.errorMessages.joinToString()}")
                return
            }
        } catch (e: Exception) { /* Ignorar */ }
        showError("Error en la respuesta de la API: ${response.code()}")
    }

    private fun showError(message: String) {
        resetUI()
        textViewStatus.text = message
        textViewStatus.visibility = View.VISIBLE
    }
}