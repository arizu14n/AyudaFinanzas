package com.zulian.ayudafinanzas

import android.content.Context
import com.zulian.ayudafinanzas.data.auth.AuthRequest
import com.zulian.ayudafinanzas.data.auth.LoginResponse
import com.zulian.ayudafinanzas.data.auth.RegisterResponse
import com.zulian.ayudafinanzas.data.check.Check
import com.zulian.ayudafinanzas.data.check.CheckResponse
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface YourApiService {
    // AUTH
    @POST("api/usuarios/registrar")
    fun registerUser(@Body authRequest: AuthRequest): Call<RegisterResponse>

    @POST("api/usuarios/login")
    fun loginUser(@Body authRequest: AuthRequest): Call<LoginResponse>

    // CHEQUES
    @Multipart
    @POST("api/cheques")
    fun uploadCheck(
        @Part("nro") nro: RequestBody,
        @Part("banco") banco: RequestBody,
        @Part("librador") librador: RequestBody,
        @Part("fecha_emision") fechaEmision: RequestBody,
        @Part("importe") importe: RequestBody,
        @Part("estado") estado: RequestBody,
        @Part imagen: MultipartBody.Part
    ): Call<CheckResponse>

    @GET("api/cheques")
    fun getMyCheques(): Call<List<Check>>

    @PUT("api/cheques/{id}")
    fun updateCheque(
        @Path("id") id: Int,
        @Body updateRequest: Map<String, String>
    ): Call<CheckResponse>

    @DELETE("api/cheques/{id}")
    fun deleteCheque(@Path("id") id: Int): Call<Unit> // No esperamos cuerpo en la respuesta
}

// ... el resto del archivo se mantiene igual ...

object YourApiClient {
    private const val BASE_URL = "https://api-ayudafinanzas.onrender.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: YourApiService = retrofit.create(YourApiService::class.java)
}

object AuthApiClient {
    private const val BASE_URL = "https://api-ayudafinanzas.onrender.com/"

    fun getApiService(context: Context): YourApiService {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val builder = originalRequest.newBuilder()
            if (token != null) {
                builder.header("Authorization", "Bearer $token")
            }
            val newRequest = builder.build()
            chain.proceed(newRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(YourApiService::class.java)
    }
}