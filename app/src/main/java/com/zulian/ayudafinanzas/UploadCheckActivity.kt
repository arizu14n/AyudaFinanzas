package com.zulian.ayudafinanzas

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

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
            processImageWithMlKit(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { 
                previewImageView.setImageURI(it)
                previewImageView.visibility = View.VISIBLE
                processImageWithMlKit(it)
            }
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

    private fun processImageWithMlKit(uri: Uri) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val image = InputImage.fromFilePath(this, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    parseTextAndFillFields(visionText.text)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseTextAndFillFields(text: String) {
        // --- Importe ---
        val amountPattern = Pattern.compile("\\$\\s*([\\d.,]+)")
        val matcherAmount = amountPattern.matcher(text)
        if (matcherAmount.find()) {
            try {
                val amountStr = matcherAmount.group(1).replace(".", "").replace(",", ".")
                val amount = amountStr.toDouble()
                importeEditText.setText(String.format(Locale.US, "%.2f", amount))
            } catch (e: NumberFormatException) { /* Ignorar */ }
        }

        // --- Número de Cheque (mejorado) ---
        val checkNumberPattern = Pattern.compile("N°\\s*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE)
        val matcherCheckNumber = checkNumberPattern.matcher(text)
        if (matcherCheckNumber.find()) {
            nroChequeEditText.setText(matcherCheckNumber.group(1))
        } else {
             // Fallback para cheques que no tienen "N°"
            val fallbackCheckPattern = Pattern.compile("\\b(\\d{8,12})\\b")
            val fallbackMatcher = fallbackCheckPattern.matcher(text)
            var lastFoundCheckNumber = ""
            while (fallbackMatcher.find()) {
                lastFoundCheckNumber = fallbackMatcher.group(1)
            }
            if (lastFoundCheckNumber.isNotEmpty()) {
                 nroChequeEditText.setText(lastFoundCheckNumber)
            }
        }

        // --- Fecha (mejorado para múltiples fechas) ---
        val dates = mutableListOf<Date>()
        val datePattern = Pattern.compile("(\\d{1,2})\\s+de\\s+([a-zA-Z]+)\\s+de\\s+(\\d{4})", Pattern.CASE_INSENSITIVE)
        val matcherDate = datePattern.matcher(text)
        while (matcherDate.find()) {
            try {
                val day = matcherDate.group(1).toInt()
                val monthName = matcherDate.group(2).toLowerCase(Locale.ROOT)
                val year = matcherDate.group(3).toInt()
                val month = monthNameToNumber(monthName)
                if (month > 0) {
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month - 1, day)
                    dates.add(calendar.time)
                }
            } catch (e: Exception) { /* Ignorar */ }
        }
        // Seleccionar la fecha más lejana (fecha de pago)
        dates.maxOrNull()?.let {
            val outputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            fechaEmisionEditText.setText(outputSdf.format(it))
        }

        // --- Librador (mejorado) ---
        val drawerPattern = Pattern.compile("(?i)(SOCIEDAD CIVIL|S\\.R\\.L\\.|S\\.A\\.|S\\.A\\.S|SRL|SA)")
        text.lines().forEach { line ->
            val matcher = drawerPattern.matcher(line)
            if (matcher.find()) {
                libradorEditText.setText(line.trim())
                return@forEach
            }
        }
    }

    private fun monthNameToNumber(monthName: String): Int {
        return when (monthName.toLowerCase(Locale.getDefault())) {
            "enero" -> 1
            "febrero" -> 2
            "marzo" -> 3
            "abril" -> 4
            "mayo" -> 5
            "junio" -> 6
            "julio" -> 7
            "agosto" -> 8
            "septiembre" -> 9
            "octubre" -> 10
            "noviembre" -> 11
            "diciembre" -> 12
            else -> 0
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