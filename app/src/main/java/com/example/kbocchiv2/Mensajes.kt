package com.example.kbocchiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.EditText
import android.widget.TextView


var nombrep : TextView? = null
private lateinit var editTextMessage: EditText
private lateinit var buttonSend: Button
private lateinit var textViewChat: TextView


class Mensajes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)
        nombrep = findViewById(R.id.txtnombre)

        val intent = intent
        if (intent != null) {
            val nombre = intent.getStringExtra("nombre")
            nombrep?.setText(nombre)
            Log.d("DatosPacientes", "Nombre: $nombre")

        }

        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        textViewChat = findViewById(R.id.textViewChat)


    }



}