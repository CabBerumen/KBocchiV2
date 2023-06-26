package com.example.kbocchiv2

import POJO.RequestCitas
import POJO.RequestPacientes
import POJO.ResultCita
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AgendarCita : AppCompatActivity() {
    private lateinit var buttonCita : Button
    private val client = OkHttpClient()
    private lateinit var agendarFecha : EditText
    private lateinit var agendarDomicilio : EditText
    private lateinit var agendarID : TextView
    private lateinit var agendarModalidad : EditText


    private lateinit var spinnerSelect : Spinner
    private var pacientes: List<RequestPacientes> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita)
        agendarFecha = findViewById(R.id.fechaAgendar)
        agendarDomicilio = findViewById(R.id.domicilioAgendar)
        buttonCita = findViewById(R.id.btnagendar)
        spinnerSelect = findViewById(R.id.selectspin)
        agendarID = findViewById(R.id.IDAgendar)
        agendarModalidad = findViewById(R.id.modalidadAgendar)


        //Spinner
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSelect.adapter = adapter
        //llamar a la función donde se obtienen los datos
        obtenerDatosDeAPI()


        buttonCita.setOnClickListener {
            crearCita()
        }

    }

    private fun crearCita() {
        val citaData = JSONObject()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)


        citaData.put("fecha", agendarFecha.text.toString())
        citaData.put("lng", -103.4574)
        citaData.put("lat", 20.456545334)
        citaData.put("id_paciente", agendarID.text.toString())
        citaData.put("id_terapeuta", token)
        citaData.put("domicilio", agendarDomicilio.text.toString())
        citaData.put("modalidad", agendarModalidad.text.toString())

        val requestBody = citaData.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/citas")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al crear la cita", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    val citaCreada = JSONObject(responseData)
                    runOnUiThread {

                        Toast.makeText(applicationContext, "Cita creada exitosamente", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al crear la cita", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun obtenerDatosDeAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kbocchi.onrender.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val call = apiService.obtenerPacientes(token)

        call.enqueue(object : retrofit2.Callback<List<RequestPacientes>> {
            override fun onResponse(call: retrofit2.Call<List<RequestPacientes>>, response: retrofit2.Response<List<RequestPacientes>>) {
                if (response.isSuccessful) {
                    pacientes = response.body()!!
                    mostrarNombresPacientes()
                    Toast.makeText(this@AgendarCita, "Datos recibidos", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@AgendarCita, "Datos no recibidos", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<List<RequestPacientes>>, t: Throwable) {
            }
        })
    }

    private fun mostrarNombresPacientes() {
        val nombres = ArrayList<String>()
        nombres.add("Selecciona un paciente")
        for (paciente in pacientes) {
            nombres.add(paciente.nombre)
        }

        adapter.clear()
        adapter.addAll(nombres)
        adapter.notifyDataSetChanged()


        spinnerSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val paciente = pacientes.getOrNull(position -1)
                if(paciente != null) {
                    val pacientid = paciente.id
                    agendarID.text = pacientid.toString()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

}
