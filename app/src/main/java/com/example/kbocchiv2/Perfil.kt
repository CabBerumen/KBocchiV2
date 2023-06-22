package com.example.kbocchiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.gson.Gson

class Perfil : AppCompatActivity() {

    private lateinit var emailText: TextView
    private lateinit var nombreText: TextView
    private lateinit var telText: TextView
    private lateinit var domiText: TextView
    private lateinit var consultText: TextView
    private lateinit var cedulatText: TextView
    private lateinit var pagomaxText: TextView
    private lateinit var pagominText: TextView
    private lateinit var rangoText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        emailText = findViewById(R.id.id_email)
        nombreText = findViewById(R.id.id_nombre)
        telText = findViewById(R.id.id_tel)
        domiText = findViewById(R.id.id_domicilio)
        consultText = findViewById(R.id.id_consultorio)
        cedulatText = findViewById(R.id.id_cedula)
        pagomaxText = findViewById(R.id.id_pagomax)
        pagominText = findViewById(R.id.id_pagomin)
        rangoText = findViewById(R.id.id_rango)


    }
}