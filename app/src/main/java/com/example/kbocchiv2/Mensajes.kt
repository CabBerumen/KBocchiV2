package com.example.kbocchiv2
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView




class Mensajes : AppCompatActivity() {

    var nombree : TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)
        nombree = findViewById(R.id.nameChat)

        val intent = intent

        if (intent != null) {
            val nombre = intent.getStringExtra("nombre")
            nombree?.setText(nombre)
            Log.d("DatosPacientes", "Nombre: $nombre")

        }



    }



}