package com.zulian.ayudafinanzas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val btnCentralDeudores = findViewById<Button>(R.id.btnCentralDeudores)
        val btnChequesDenunciados = findViewById<Button>(R.id.btnChequesDenunciados)
        val btnEstadisticasCambiarias = findViewById<Button>(R.id.btnEstadisticasCambiarias)
        val btnLogout = findViewById<Button>(R.id.buttonLogout)

        btnCentralDeudores.setOnClickListener {
            startActivity(Intent(this, CentralDeudoresActivity::class.java))
        }

        btnChequesDenunciados.setOnClickListener {
            startActivity(Intent(this, ChequesDenunciadosActivity::class.java))
        }

        btnEstadisticasCambiarias.setOnClickListener {
            startActivity(Intent(this, EstadisticasCambiariasActivity::class.java))
        }

        btnLogout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}