package com.zulian.ayudafinanzas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.zulian.ayudafinanzas.data.DeudaResponse
import com.zulian.ayudafinanzas.data.EntidadDeuda
import com.zulian.ayudafinanzas.data.ErrorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class CentralDeudoresActivity : AppCompatActivity() {

    private lateinit var editTextCuit: EditText
    private lateinit var buttonConsultar: Button
    private lateinit var textViewStatus: TextView
    private lateinit var layoutResultados: LinearLayout
    private lateinit var layoutDetallesAdicionales: LinearLayout
    private lateinit var textViewHeaderInfo: TextView
    private lateinit var recyclerViewDeudas: RecyclerView
    private lateinit var recyclerViewDetalles: RecyclerView

    private lateinit var deudasAdapter: DeudasAdapter
    private lateinit var detalleAdapter: DeudaDetalleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_central_deudores)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de Vistas
        editTextCuit = findViewById(R.id.editTextCuit)
        buttonConsultar = findViewById(R.id.buttonConsultarDeuda)
        textViewStatus = findViewById(R.id.textViewDeudaStatus)
        layoutResultados = findViewById(R.id.layoutResultados)
        layoutDetallesAdicionales = findViewById(R.id.layoutDetallesAdicionales)
        textViewHeaderInfo = findViewById(R.id.textViewHeaderInfo)
        recyclerViewDeudas = findViewById(R.id.recyclerViewDeudas)
        recyclerViewDetalles = findViewById(R.id.recyclerViewDetalles)

        setupRecyclerViews()
        setupListeners()
    }

    private fun setupRecyclerViews() {
        deudasAdapter = DeudasAdapter(emptyList()) { entidadDeuda ->
            showDetallesAdicionales(entidadDeuda)
        }
        recyclerViewDeudas.layoutManager = LinearLayoutManager(this)
        recyclerViewDeudas.adapter = deudasAdapter

        detalleAdapter = DeudaDetalleAdapter(emptyList())
        recyclerViewDetalles.layoutManager = LinearLayoutManager(this)
        recyclerViewDetalles.adapter = detalleAdapter
    }

    private fun setupListeners() {
        buttonConsultar.setOnClickListener { consultarDeuda() }

        editTextCuit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resetUI()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (layoutDetallesAdicionales.visibility == View.VISIBLE) {
                    layoutDetallesAdicionales.visibility = View.GONE
                } else {
                    finish()
                }
            }
        })
    }

    private fun consultarDeuda() {
        val cuit = editTextCuit.text.toString()

        if (cuit.length != 11) {
            showError("El CUIT/CUIL debe tener 11 dígitos.")
            return
        }

        resetUI()

        ApiClient.apiService.getDeudas(cuit.toLong()).enqueue(object : Callback<DeudaResponse> {
            override fun onResponse(call: Call<DeudaResponse>, response: Response<DeudaResponse>) {
                if (response.isSuccessful) {
                    val deudaResponse = response.body()
                    if (deudaResponse?.results != null) {
                        val results = deudaResponse.results
                        if (results.periodos.isNotEmpty()) {
                            layoutResultados.visibility = View.VISIBLE
                            textViewHeaderInfo.text = formatHeader(results.identificacion, results.denominacion, results.periodos.first().periodo)
                            deudasAdapter.updateData(results.periodos.first().entidades)
                        } else {
                            showError("No se encontraron períodos de deuda para el CUIT ingresado.")
                        }
                    } else {
                        showError("No se encontraron datos para el CUIT ingresado.")
                    }
                } else {
                    handleApiError(response)
                }
            }

            override fun onFailure(call: Call<DeudaResponse>, t: Throwable) {
                showError("Error de red: ${t.message}")
            }
        })
    }

    private fun showDetallesAdicionales(entidadDeuda: EntidadDeuda) {
        val detalles = listOf(
            "Días de Atraso en el Pago" to (entidadDeuda.diasAtrasoPago > 0),
            "Tiene refinanciaciones?" to entidadDeuda.refinanciaciones,
            "Recategorización Obligatoria?" to entidadDeuda.recategorizacionOblig,
            "Tiene Juicios?" to entidadDeuda.situacionJuridica,
            "Es Irrecuperable por DT?" to entidadDeuda.irrecDisposicionTecnica,
            "Está en revisión?" to entidadDeuda.enRevision,
            "Está en Proceso judicial?" to entidadDeuda.procesoJud
        )
        
        detalleAdapter.updateData(detalles)
        layoutDetallesAdicionales.visibility = View.VISIBLE
    }
    
    private fun formatHeader(identificacion: Long, denominacion: String, periodo: String): String {
        val formattedPeriodo = try {
            val inputFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(periodo)
            date?.let { outputFormat.format(it).replaceFirstChar { it.uppercase() } } ?: periodo
        } catch (e: Exception) {
            periodo
        }
        return "CUIT/CUIL: $identificacion\nDenominación: $denominacion\nPeríodo: $formattedPeriodo"
    }

    private fun resetUI() {
        layoutResultados.visibility = View.GONE
        layoutDetallesAdicionales.visibility = View.GONE
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