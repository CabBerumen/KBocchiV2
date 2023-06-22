package com.example.kbocchiv2

import POJO.RequestPacientes
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kbocchiv2.Interfaces.ApiService
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Pacientes : AppCompatActivity() {
    var apellidos : TextView? = null
    var nombre: TextView? = null

    private lateinit var listView: ListView
    private var pacientes: List<RequestPacientes> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacientes)

        listView = findViewById<ListView>(R.id.listapacientes)
        pacientes = ArrayList()

        obtenerDatosDeAPI()

    }

    private fun obtenerDatosDeAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kbocchi.onrender.com") // Reemplaza con la URL base de tu API REST
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java) // Reemplaza "ApiService" con el nombre de tu interfaz de servicio

        val call = apiService.obtenerPacientes(218) // Reemplaza "obtenerPacientes" con el nombre del método de tu API para obtener los datos de los pacientes

        call.enqueue(object : Callback<List<RequestPacientes>> {
            override fun onResponse(call: Call<List<RequestPacientes>>, response: Response<List<RequestPacientes>>) {
                if (response.isSuccessful) {
                    pacientes = response.body()!!
                    mostrarNombresPacientes()
                    Toast.makeText(this@Pacientes, "Datos recibidos", Toast.LENGTH_SHORT).show()
                } else {
                    // Manejo de errores en caso de una respuesta no exitosa de la API
                    Toast.makeText(this@Pacientes, "Datos no recibidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RequestPacientes>>, t: Throwable) {
                // Manejo de errores en caso de una falla en la llamada a la API
            }
        })
    }

    private fun mostrarNombresPacientes() {
        val nombres = ArrayList<String>()
        for (paciente in pacientes) {
            nombres.add(paciente.nombre)
        }

        for (nombre in nombres) {
            Log.d("NOMBRE_PACIENTE", nombre)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val paciente = pacientes[position]
            mostrarDatosCompletos(paciente)
        }
    }

    private fun mostrarDatosCompletos(paciente: RequestPacientes) {
        val intent = Intent(this@Pacientes, DatosPacientes::class.java)
        intent.putExtra("nombre", paciente.nombre)
        intent.putExtra("fotoPerfil", paciente.fotoPerfil)
        intent.putExtra("email", paciente.email)
        intent.putExtra("telefono", paciente.telefono)
        startActivity(intent)

    }

}

