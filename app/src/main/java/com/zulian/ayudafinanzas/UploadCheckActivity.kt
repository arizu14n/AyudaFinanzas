package com.zulian.ayudafinanzas

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zulian.ayudafinanzas.data.check.CheckResponse
import com.zulian.ayudafinanzas.data.ChequeEntidad
import com.zulian.ayudafinanzas.data.ChequeEntidadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UploadCheckActivity : AppCompatActivity() {

    private lateinit var nroChequeEditText: EditText
    private lateinit var bancoSpinner: Spinner
    private lateinit var libradorEditText: EditText
    private lateinit var fechaEmisionEditText: EditText
    private lateinit var importeEditText: EditText
    private lateinit var estadoSpinner: Spinner // CAMBIADO
    private lateinit var selectImageButton: Button
    private lateinit var uploadButton: Button
    private lateinit var previewImageView: ImageView

    private var imageUri: Uri? = null
    private val entidades = mutableListOf<ChequeEntidad>()

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            previewImageView.setImageURI(it)
            previewImageView.visibility = View.VISIBLE
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            previewImageView.setImageURI(imageUri)
            previewImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_check)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nroChequeEditText = findViewById(R.id.editTextNroCheque)
        bancoSpinner = findViewById(R.id.spinnerBanco)
        libradorEditText = findViewById(R.id.editTextLibrador)
        fechaEmisionEditText = findViewById(R.id.editTextFechaEmision)
        importeEditText = findViewById(R.id.editTextImporte)
        estadoSpinner = findViewById(R.id.spinnerEstado) // CAMBIADO
        selectImageButton = findViewById(R.id.buttonSelectImage)
        uploadButton = findViewById(R.id.buttonUpload)
        previewImageView = findViewById(R.id.imageViewPreview)

        setupListeners()
        setupEstadoSpinner()
        fetchEntidades()
    }

    private fun setupListeners() {
        selectImageButton.setOnClickListener { showImageSourceDialog() }
        uploadButton.setOnClickListener { uploadCheckData() }
        setupDatePicker()
    }

    private fun setupEstadoSpinner() {
        val estados = listOf("En cartera", "Endosado", "Depositado", "Rechazado", "Reclamado", "Denunciado", "Extraviado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = adapter
        estadoSpinner.setSelection(0) // "En cartera" por defecto
    }

    private fun fetchEntidades() {
        ApiClient.apiService.getChequeEntidades().enqueue(object : Callback<ChequeEntidadResponse> {
            override fun onResponse(call: Call<ChequeEntidadResponse>, response: Response<ChequeEntidadResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    entidades.clear()
                    entidades.addAll(response.body()!!.results.sortedBy { it.denominacion })
                    val adapter = ArrayAdapter(this@UploadCheckActivity, android.R.layout.simple_spinner_item, entidades.map { it.denominacion })
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    bancoSpinner.adapter = adapter
                } else {
                    Toast.makeText(this@UploadCheckActivity, "Error al cargar las entidades bancarias.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChequeEntidadResponse>, t: Throwable) {
                Toast.makeText(this@UploadCheckActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar Foto", "Elegir de la Galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun takePicture() {
        val file = File(externalCacheDir, "${System.currentTimeMillis()}.jpg")
        val newImageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
        imageUri = newImageUri
        cameraLauncher.launch(newImageUri)
    }
    
    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            fechaEmisionEditText.setText(dateFormat.format(calendar.time))
        }

        fechaEmisionEditText.setOnClickListener {
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun uploadCheckData() {
        if (imageUri == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen.", Toast.LENGTH_SHORT).show()
            return
        }
        if (entidades.isEmpty()) {
             Toast.makeText(this, "Espere a que las entidades carguen.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedBanco = entidades[bancoSpinner.selectedItemPosition]
        val selectedEstado = estadoSpinner.selectedItem.toString()

        val nro = nroChequeEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val banco = selectedBanco.denominacion.toRequestBody(MultipartBody.FORM)
        val librador = libradorEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val fechaEmision = fechaEmisionEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val importe = importeEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val estado = selectedEstado.toRequestBody(MultipartBody.FORM) // CAMBIADO

        val inputStream = contentResolver.openInputStream(imageUri!!)
        val file = File(cacheDir, "temp_image.jpg")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imagen", file.name, requestFile)

        AuthApiClient.getApiService(this).uploadCheck(nro, banco, librador, fechaEmision, importe, estado, imagePart)
            .enqueue(object : Callback<CheckResponse> {
                override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UploadCheckActivity, "Cheque registrado exitosamente.", Toast.LENGTH_LONG).show()
                        finish() 
                    } else {
                        Toast.makeText(this@UploadCheckActivity, "Error al registrar: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
                    Toast.makeText(this@UploadCheckActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}