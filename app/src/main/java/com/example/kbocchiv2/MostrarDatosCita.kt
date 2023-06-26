package com.example.kbocchiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MostrarDatosCita : AppCompatActivity() {

    var nombrepacient : TextView? = null
    var fechapacient : TextView? = null
    var modalidadpacient : TextView? = null
    var domiciliopacient : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_datos_cita)
        nombrepacient = findViewById(R.id.pacientetext)
        fechapacient = findViewById(R.id.fechatext)
        modalidadpacient = findViewById(R.id.modalidadtext)
        domiciliopacient = findViewById(R.id.domiciliotext)

        val intent = intent
        if (intent != null) {
            val pacientecita = intent.getStringExtra("nombre")
            val fechacita = intent.getStringExtra("fecha")
            val modalidadcita = intent.getStringExtra("modalidad")
            val domiciliocita = intent.getStringExtra(("domicilio"))

            nombrepacient?.setText(pacientecita)
            fechapacient?.setText(fechacita)
            modalidadpacient?.setText(modalidadcita)
            domiciliopacient?.setText(domiciliocita)


        }


    }
}