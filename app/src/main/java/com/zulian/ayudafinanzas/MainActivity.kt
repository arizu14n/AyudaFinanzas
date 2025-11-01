package com.zulian.ayudafinanzas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnUploadCheck = findViewById<Button>(R.id.btnUploadCheck)
        val btnMyCheques = findViewById<Button>(R.id.btnMyCheques)
        val btnCentralDeudores = findViewById<Button>(R.id.btnCentralDeudores)
        val btnChequesDenunciados = findViewById<Button>(R.id.btnChequesDenunciados)
        val btnEstadisticasCambiarias = findViewById<Button>(R.id.btnEstadisticasCambiarias)
        val btnBnaRates = findViewById<Button>(R.id.btnBnaRates)
        val btnLogout = findViewById<Button>(R.id.buttonLogout)

        btnUploadCheck.setOnClickListener {
            startActivity(Intent(this, UploadCheckActivity::class.java))
        }

        btnMyCheques.setOnClickListener {
            startActivity(Intent(this, ChequeListActivity::class.java))
        }

        btnCentralDeudores.setOnClickListener {
            startActivity(Intent(this, CentralDeudoresActivity::class.java))
        }

        btnChequesDenunciados.setOnClickListener {
            startActivity(Intent(this, ChequesDenunciadosActivity::class.java))
        }

        btnEstadisticasCambiarias.setOnClickListener {
            startActivity(Intent(this, EstadisticasCambiariasActivity::class.java))
        }

        btnBnaRates.setOnClickListener {
            startActivity(Intent(this, BnaRatesActivity::class.java))
        }

        btnLogout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("jwt_token").apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}