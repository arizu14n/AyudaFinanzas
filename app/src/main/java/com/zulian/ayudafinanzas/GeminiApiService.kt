package com.zulian.ayudafinanzas

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- DATA CLASSES FOR GEMINI API ---

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mime_type")
    val mimeType: String,
    val data: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)

// --- DATA CLASS FOR THE EXPECTED JSON FROM GEMINI ---

data class ChequeDetails(
    @SerializedName("nro_cheque")
    val nroCheque: String?,
    val importe: String?,
    @SerializedName("fecha_pago")
    val fechaPago: String?,
    val librador: String?
)

// --- RETROFIT SERVICE INTERFACE ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    fun getChequeDetails(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}

// --- API CLIENT OBJECT ---

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Crear un interceptor de logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configurar un cliente con timeouts más largos y el interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
