package com.example.kbocchiv2

import POJO.RequestPacientes
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.kbocchiv2.Interfaces.ApiService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar


class AgendarCita : AppCompatActivity() {
    private lateinit var buttonCita : Button
    private val client = OkHttpClient()
    private lateinit var agendarFecha : TextView
    private lateinit var agendarDomicilio : EditText
    private lateinit var agendarID : TextView
    private lateinit var agendarModalidad : TextView
    private lateinit var FechaButton : Button
    private lateinit var HoraButton : Button
    private lateinit var AgendarPaciente : TextView



    private lateinit var spinnerSelect : Spinner
    private lateinit var spinnerSelect2 : Spinner
    private var pacientes: List<RequestPacientes> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>

    private var selectedDate: String = ""
    private var selectedTime: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita)
        agendarFecha = findViewById(R.id.fechaAgendar)
        agendarDomicilio = findViewById(R.id.domicilioAgendar)
        buttonCita = findViewById(R.id.btnagendar)
        spinnerSelect = findViewById(R.id.selectspin)
        agendarID = findViewById(R.id.IDAgendar)
        agendarModalidad = findViewById(R.id.modalidadAgendar)
        FechaButton = findViewById(R.id.fechapicker)
        HoraButton = findViewById(R.id.horapicker)
        AgendarPaciente = findViewById(R.id.pacienteagendar)
        spinnerSelect2 = findViewById(R.id.selectspin2)


        //Spinner
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSelect.adapter = adapter
        //llamar a la función donde se obtienen los datos
        obtenerDatosDeAPI()

        FechaButton.setOnClickListener {
            showDatePicker()
        }

        HoraButton.setOnClickListener {
            showTimePicker()
        }
        val adaptador2 = ArrayAdapter.createFromResource(this, R.array.modalidad, android.R.layout.simple_spinner_item
        )
        spinnerSelect2.setAdapter(adaptador2)

        spinnerSelect2.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(part2: AdapterView<*>, view: View, post2: Int, id2: Long) {
                agendarModalidad.setText(part2.getItemAtPosition(post2).toString())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })


        buttonCita.setOnClickListener {
            crearCita()
        }

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                updateResultTextView()
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, selectedHour, selectedMinute ->
               selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
               updateResultTextView()

            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun updateResultTextView() {
        agendarFecha.text = "$selectedDate $selectedTime"
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
                    AgendarPaciente.text = paciente.nombre
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

}
