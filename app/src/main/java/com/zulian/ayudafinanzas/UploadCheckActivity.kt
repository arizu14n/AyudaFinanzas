package com.zulian.ayudafinanzas

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.zulian.ayudafinanzas.data.ChequeEntidad
import com.zulian.ayudafinanzas.data.ChequeEntidadResponse
import com.zulian.ayudafinanzas.data.check.CheckResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UploadCheckActivity : AppCompatActivity() {

    private lateinit var nroChequeEditText: EditText
    private lateinit var bancoSpinner: Spinner
    private lateinit var libradorEditText: EditText
    private lateinit var fechaEmisionEditText: EditText
    private lateinit var importeEditText: EditText
    private lateinit var estadoSpinner: Spinner
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
            processImageWithGemini(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                previewImageView.setImageURI(it)
                previewImageView.visibility = View.VISIBLE
                processImageWithGemini(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_check)

        nroChequeEditText = findViewById(R.id.editTextNroCheque)
        bancoSpinner = findViewById(R.id.spinnerBanco)
        libradorEditText = findViewById(R.id.editTextLibrador)
        fechaEmisionEditText = findViewById(R.id.editTextFechaEmision)
        importeEditText = findViewById(R.id.editTextImporte)
        estadoSpinner = findViewById(R.id.spinnerEstado)
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
        estadoSpinner.setSelection(0)
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

    // --- GEMINI IMPLEMENTATION ---

    private fun processImageWithGemini(uri: Uri) {
        val prompt = "Analiza la imagen de este cheque y extrae el número de cheque (suele estar precedido por 'N°'), el importe (precedido por '$'), la fecha de pago (la fecha más lejana en el tiempo) y el nombre del librador. Devuelve un único objeto JSON con las claves: nro_cheque, importe, fecha_pago (en formato yyyy-MM-dd), y librador."

        val imageBase64 = uriToBase64(uri)
        if (imageBase64 == null) {
            Toast.makeText(this, "No se pudo convertir la imagen.", Toast.LENGTH_SHORT).show()
            return
        }

        val geminiRequest = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = imageBase64))
                ))
            )
        )

        GeminiApiClient.apiService.getChequeDetails(BuildConfig.GEMINI_API_KEY, geminiRequest).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val textResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    parseGeminiResponse(textResponse)
                } else {
                    Toast.makeText(this@UploadCheckActivity, "Error con Gemini: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                Toast.makeText(this@UploadCheckActivity, "Fallo en la conexión con Gemini: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun parseGeminiResponse(jsonString: String) {
        try {
            // Limpiar la respuesta de Gemini para obtener solo el JSON
            val cleanedJson = jsonString.substringAfter("```json").substringBefore("```").trim()
            val chequeDetails = Gson().fromJson(cleanedJson, ChequeDetails::class.java)

            chequeDetails.nroCheque?.let { nroChequeEditText.setText(it) }
            // Limpiar el importe para formato numérico estándar (punto decimal, sin miles)
            chequeDetails.importe?.let {
                val cleanedImporte = it.replace(".", "").replace(",", ".")
                importeEditText.setText(cleanedImporte)
            }
            chequeDetails.fechaPago?.let { fechaEmisionEditText.setText(it) }
            chequeDetails.librador?.let { libradorEditText.setText(it) }

        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo interpretar la respuesta de Gemini.", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadCheckData() {
        val currentImageUri = imageUri
        if (currentImageUri == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen.", Toast.LENGTH_SHORT).show()
            return
        }
        if (entidades.isEmpty()) {
             Toast.makeText(this, "Espere a que las entidades carguen.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Compresión de Imagen ---
        val compressedFile = try {
            val inputStream = contentResolver.openInputStream(currentImageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val file = File(cacheDir, "compressed_image.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al comprimir la imagen.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedBanco = entidades[bancoSpinner.selectedItemPosition]
        val selectedEstado = estadoSpinner.selectedItem.toString()

        val nro = nroChequeEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val banco = selectedBanco.denominacion.toRequestBody(MultipartBody.FORM)
        val librador = libradorEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val fechaEmision = fechaEmisionEditText.text.toString().toRequestBody(MultipartBody.FORM)
        // El importe ya está en formato numérico estándar gracias a parseGeminiResponse
        val importe = importeEditText.text.toString().toRequestBody(MultipartBody.FORM)
        val estado = selectedEstado.toRequestBody(MultipartBody.FORM)

        val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imagen", compressedFile.name, requestFile)

        AuthApiClient.getApiService(this).uploadCheck(nro, banco, librador, fechaEmision, importe, estado, imagePart)
            .enqueue(object : Callback<CheckResponse> {
                override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UploadCheckActivity, "Cheque registrado exitosamente.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@UploadCheckActivity, "Error al registrar: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
                    Toast.makeText(this@UploadCheckActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
