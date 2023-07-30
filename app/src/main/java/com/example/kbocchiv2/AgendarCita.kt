package com.example.kbocchiv2

import POJO.RequestPacientes
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
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
import java.util.Locale


class AgendarCita : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback{
    private lateinit var buttonCita : Button
    private val client = OkHttpClient()
    private lateinit var agendarFecha : TextView
    private lateinit var agendarDomicilio : TextView
    private lateinit var agendarHora : TextView
    private lateinit var agendarModalidad : TextView
    private lateinit var FechaButton : Button
    private lateinit var HoraButton : Button
    private lateinit var AgendarPaciente : TextView
    private lateinit var mapView: MapView
    private lateinit var geocoder: Geocoder
    private lateinit var btnbuscar : Button
    private lateinit var direccion : EditText
    private lateinit var googleMap : GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var idpaciente: Int = 0
    private lateinit var spinnerSelect : Spinner
    private lateinit var spinnerSelect2 : Spinner
    private var pacientes: List<RequestPacientes> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var toolbar: Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        agendarFecha = findViewById(R.id.fechaAgendar)
        agendarHora = findViewById<TextView>(R.id.hourAgendar)
        agendarDomicilio = findViewById(R.id.domicilioAgendar)
        buttonCita = findViewById(R.id.btnagendar)
        spinnerSelect = findViewById(R.id.selectspin)
        agendarModalidad = findViewById(R.id.modalidadAgendar)
        FechaButton = findViewById(R.id.fechapicker)
        HoraButton = findViewById(R.id.horapicker)
        AgendarPaciente = findViewById(R.id.pacienteagendar)
        spinnerSelect2 = findViewById(R.id.selectspin2)
        btnbuscar = findViewById(R.id.buttonSearch)
        direccion = findViewById(R.id.editTextAddress)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        btnbuscar.setOnClickListener {
            val address = direccion.text.toString()
            if (address.isNotEmpty()) {
                GeocodingTask().execute(address)
            }else{
                Toast.makeText(this, "Ingresa una dirección", Toast.LENGTH_SHORT).show()
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout?.closeDrawer(GravityCompat.START)
        navigationView?.setNavigationItemSelectedListener(this)

        val toogle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_close, R.string.navigation_drawer_close)
        drawerLayout?.addDrawerListener(toogle)
        toogle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val navHeaderView = navigationView.getHeaderView(0)
        val imageView = navHeaderView.findViewById<CircleImageView>(R.id.navheaderFoto)
        val usertext = navHeaderView.findViewById<TextView>(R.id.user_name)
        val emailtext = navHeaderView.findViewById<TextView>(R.id.user_email)

        val sharedPreferences = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
        val fototerapeuta = sharedPreferences.getString("foto_perfil", "")
        val email = sharedPreferences.getString("email", "")
        val nombre = sharedPreferences.getString("nombre", "")

        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"

        usertext.text = nombre
        emailtext.text = email
        val fotoNav = fototerapeuta

        val imagePath = fotoNav ?: ""

        val storageReference = if(!imagePath.isNullOrEmpty()){
            storage.reference.child(imagePath)
        }else{
            null
        }
        if(!imagePath.isNullOrEmpty()) {
            storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(imageView, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                        }
                        override fun onError(e: Exception?) {
                        }
                    })
            }?.addOnFailureListener { exception ->
            }
        } else {
            imageView.visibility = View.GONE
            imageView.setImageResource(R.drawable.perfil)
        }


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
                agendarFecha.text = selectedDate

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
               agendarHora.text = selectedTime

            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun crearCita() {
        val citaData = JSONObject()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        citaData.put("fecha", agendarFecha.text.toString() + " " + agendarHora.text.toString())
        citaData.put("lng", longitude)
        citaData.put("lat", latitude)
        citaData.put("id_paciente", idpaciente)
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
                    idpaciente= paciente.id
                    AgendarPaciente.text = paciente.nombre
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item0 -> {
                //Ir a la actividad principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item1 -> {
                //Ir a la agenda
                val intent = Intent(this, MostrarCitas::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_citas -> {
                //Ir a agendar citas
                val intent = Intent(this, AgendarCita::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item2 -> {
                //Ir al chat
                val intent = Intent(this, mainChat::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item3 -> {
                //Ir al maps
                val intent = Intent(this, Maps::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item4 -> {
                //Ir al expediente
                val intent = Intent(this, Expediente::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_pacientes -> {
                //Ir a ver pacientes
                val intent = Intent(this, Pacientes::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_perfil -> {
                //Ir a ver perfil
                val intent = Intent(this, Perfil::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_logout -> {
                //Cerrar Sesión
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = sharedPreferences.edit()
                editor.remove("token")
                editor.apply()
                val intent2 = Intent(this, LogIn::class.java)
                startActivity(intent2)
                finish()
            }

        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    companion object {
        private const val REQUEST_PERMISSION = 1
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
            val geocoder = Geocoder(this@AgendarCita, Locale.getDefault())
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
                agendarDomicilio.text = formattedAddress

                latitude = address.latitude
                longitude = address.longitude

                val location = LatLng(address.latitude, address.longitude)

                googleMap.addMarker(com.google.android.gms.maps.model.MarkerOptions().position(location).title(formattedAddress))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            } else {
                agendarDomicilio.text = "No se encontró ninguna dirección."
            }
        }
    }
}


