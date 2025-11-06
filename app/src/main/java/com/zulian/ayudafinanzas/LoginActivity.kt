package com.zulian.ayudafinanzas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.zulian.ayudafinanzas.data.auth.ApiErrorResponse
import com.zulian.ayudafinanzas.data.auth.AuthRequest
import com.zulian.ayudafinanzas.data.auth.LoginResponse
import com.zulian.ayudafinanzas.data.auth.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        if (sharedPreferences.getString("jwt_token", null) != null) {
            navigateToMain()
            return
        }

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        progressBar = findViewById(R.id.progressBarLogin)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        loginButton.setOnClickListener { handleLogin() }
        registerButton.setOnClickListener { handleRegister() }
    }

    private fun handleRegister() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos.")
            return
        }

        showLoading(true)
        val authRequest = AuthRequest(email, password)
        YourApiClient.apiService.registerUser(authRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    showToast("Registro exitoso. Por favor, inicie sesión.")
                } else {
                    handleApiError(response)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false)
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos.")
            return
        }

        showLoading(true)
        val authRequest = AuthRequest(email, password)
        YourApiClient.apiService.loginUser(authRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        saveToken(token)
                        navigateToMain()
                    } else {
                        showToast("Respuesta de login inválida.")
                    }
                } else {
                    handleApiError(response)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun <T> handleApiError(response: Response<T>) {
        val errorBody = response.errorBody()?.string()
        try {
            val errorResponse = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
            showToast("Error: ${errorResponse.error}")
        } catch (e: Exception) {
            showToast("Error: ${response.code()} ${response.message()}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}