package com.example.kbocchiv2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import java.util.Locale

class ModificarCita : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fechaeditar : TextView
    private lateinit var horaeditar : TextView
    private lateinit var modalidadeditar : TextView
    private lateinit var spineditar : Spinner
    private lateinit var editardireccion: EditText
    private lateinit var buscardireccion: Button
    private lateinit var mapView : MapView
    private lateinit var geocoder: Geocoder
    private lateinit var domiciliotext : TextView
    private lateinit var editarcita : Button
    private val client = OkHttpClient()
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private lateinit var datebtn : Button
    private lateinit var horabtn : Button
    private lateinit var googleMap : GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var regresarbtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_cita)
        fechaeditar = findViewById(R.id.editarfecha)
        horaeditar = findViewById(R.id.editarhora)
        modalidadeditar = findViewById(R.id.editarmodalidad)
        spineditar = findViewById(R.id.editarspin)
        editardireccion = findViewById(R.id.editTextAddress2)
        buscardireccion = findViewById(R.id.buttonSearch2)
        mapView = findViewById(R.id.mapView2)
        domiciliotext = findViewById(R.id.domicilioAgendar2)
        editarcita = findViewById(R.id.btneditarcita)
        datebtn = findViewById(R.id.fechapicker2)
        horabtn = findViewById(R.id.horapicker2)
        regresarbtn = findViewById(R.id.regresar)


        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        buscardireccion.setOnClickListener {
            val address = editardireccion.text.toString()
            if (address.isNotEmpty()) {
                GeocodingTask().execute(address)
            }else{
                Toast.makeText(this, "Ingresa una dirección", Toast.LENGTH_SHORT).show()
            }
        }


        datebtn.setOnClickListener {
            showDatePicker()
        }
        horabtn.setOnClickListener {
            showTimePicker()
        }
        val adaptador = ArrayAdapter.createFromResource(
            this, R.array.modalidad, android.R.layout.simple_spinner_item
        )
        spineditar.setAdapter(adaptador)

        spineditar.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(part2: AdapterView<*>, view: View, post2: Int, id2: Long) {
                modalidadeditar.setText(part2.getItemAtPosition(post2).toString())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })

        editarcita.setOnClickListener {
            editarCita()
        }

        regresarbtn.setOnClickListener {
            val intent = Intent(this@ModificarCita, MostrarCitas::class.java)
            startActivity(intent)
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
                fechaeditar.text = selectedDate

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
            horaeditar.text = selectedTime

        },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }


    private fun editarCita() {
        val cita = JSONObject()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this)
        val citaid = sharedPreferences2.getInt("idcitaeditar", 0)

        val sharedPreferences3 = PreferenceManager.getDefaultSharedPreferences(this)
        val pacientid = sharedPreferences3.getInt("idpacient", 0)

        cita.put("id", citaid)
        cita.put("lng", latitude )
        cita.put("lat",longitude)
        cita.put("id_paciente", pacientid)
        cita.put("id_terapeuta", token)
        cita.put("fecha", fechaeditar.text.toString() + " " + horaeditar.text.toString())
        cita.put("modalidad", modalidadeditar.text.toString())
        cita.put("domicilio", domiciliotext.text.toString())

        Log.e("ID NOTA ", "ID : $pacientid", )
        Log.e("ID CITA ", "ID : $citaid", )


        val requestBody = cita.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/citas")
            .patch(requestBody)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread{
                    Toast.makeText(this@ModificarCita, "Error en la llamada de red", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null){
                    val citaactualizada = JSONObject (responseData)
                    runOnUiThread{
                        Toast.makeText(this@ModificarCita, "Cita modificada", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val errorMessage = responseData
                    runOnUiThread {
                        Toast.makeText(this@ModificarCita, "Error en la API: $errorMessage", Toast.LENGTH_SHORT).show()
                        Log.d("ERROR MESSAGE:", "Error: $errorMessage")
                    }
                }

            }
        })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private inner class GeocodingTask : AsyncTask<String, Void, List<Address>>() {
        override fun doInBackground(vararg addresses: String): List<Address>? {
            val geocoder = Geocoder(this@ModificarCita, Locale.getDefault())
            try {
                return geocoder.getFromLocationName(addresses[0], 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(results: List<Address>?) {
            if (results != null && results.isNotEmpty()) {
                val address = results[0]
                val formattedAddress = address.getAddressLine(0)
                domiciliotext.text = formattedAddress

                latitude = address.latitude
                longitude = address.longitude

                val location = LatLng(address.latitude, address.longitude)

                googleMap.addMarker(
                    com.google.android.gms.maps.model.MarkerOptions().position(location)
                        .title(formattedAddress)
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            } else {
                domiciliotext.text = "No se encontró ninguna dirección."
            }
        }
    }

}