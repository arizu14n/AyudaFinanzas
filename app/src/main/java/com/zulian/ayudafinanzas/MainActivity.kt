package com.zulian.ayudafinanzas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCentralDeudores = findViewById<Button>(R.id.btnCentralDeudores)
        val btnChequesDenunciados = findViewById<Button>(R.id.btnChequesDenunciados)
        val btnEstadisticasCambiarias = findViewById<Button>(R.id.btnEstadisticasCambiarias)

        btnCentralDeudores.setOnClickListener {
            startActivity(Intent(this, CentralDeudoresActivity::class.java))
        }

        btnChequesDenunciados.setOnClickListener {
            startActivity(Intent(this, ChequesDenunciadosActivity::class.java))
        }

        btnEstadisticasCambiarias.setOnClickListener {
            startActivity(Intent(this, EstadisticasCambiariasActivity::class.java))
        }
    }
}