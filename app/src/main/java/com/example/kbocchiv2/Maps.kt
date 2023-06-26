package com.example.kbocchiv2

import POJO.RequestCitas
import POJO.RequestPacientes
import POJO.ResultCita
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.sql.Struct
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Maps : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var originEditText: TextView
    private lateinit var destinationEditText: TextView
    private lateinit var ubicacionActualBtn: Button
    private var isLocationPermissionGranted = false

    private lateinit var spinnerUbicacion : Spinner
    private var pacientes: List<RequestCitas> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>

    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var toolbar: Toolbar? = null
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        drawerLayout?.closeDrawer(GravityCompat.START)
        mAuth = FirebaseAuth.getInstance()
        navigationView?.setNavigationItemSelectedListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val toogle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_close,
            R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toogle)
        toogle.syncState()


        originEditText = findViewById(R.id.originEditText)
        destinationEditText = findViewById(R.id.destinationEditText)
        val searchButton: Button = findViewById(R.id.searchButton)
        ubicacionActualBtn = findViewById(R.id.currentLocationButton)
        spinnerUbicacion = findViewById(R.id.ubispin)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        searchButton.setOnClickListener { searchRoute() }
        ubicacionActualBtn.setOnClickListener { requestLocationPermission() }


        //Spinner
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUbicacion.adapter = adapter
        //llamar a la función donde se obtienen los datos
        obtenerDatosDeAPI()


    }

    //llama a la ruta de la API para obtener las citas de los pacientes y obtener los datos de nombre de paciente y dirección
    // ResultCita es la clase principal para el arreglo para obtener los datos de la cita
    //RequestCita obtiene los datos que hay dentro del arreglo
    private fun obtenerDatosDeAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kbocchi.onrender.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val call = apiService.obtenerCitas(token)

        Log.d("Token", "Datos recibidos: $token")

        call.enqueue(object : Callback<ResultCita> {
            override fun onResponse(
                call: Call<ResultCita>, response: Response<ResultCita>
            ) {
                Log.d("API_Response", "Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val resultCita = response.body()

                    //resultCita guarda el arreglo con los datos de la cita y pacientes obtiene los datos con la clase RequestCita
                    // que es la clase dentro del arreglo de ResulCita
                    pacientes = resultCita?.getCitas() ?: emptyList()
                    mostrarNombresPacientes()
                } else {
                    Log.e("MostrarCitas", "Error en la respuesta: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResultCita>, t: Throwable) {
                Log.e("MostrarCitas", "Error en la llamada a la API: ${t.message}")
            }
        })
    }

    //Muestra los nombres de los pacientes en el spinner y al seleccionar un nombre muestra la dirección en el textview
    // ResultCita es la clase principal para el arreglo para obtener los datos de la cita
    //RequestCita obtiene los datos que hay dentro del arreglo
    private fun mostrarNombresPacientes() {
        val nombres = HashSet<String>()
        nombres.add("")
        nombres.add("Selecciona un paciente")
        for (paciente in pacientes) {
            nombres.add(paciente.nombre)
        }

        adapter.clear()
        adapter.addAll(nombres)
        adapter.notifyDataSetChanged()

        spinnerUbicacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val paciente = pacientes.getOrNull(position -2)
                if(paciente != null) {
                    val direccion = paciente.domicilio
                    destinationEditText.text = direccion
                } else{
                    destinationEditText.text = ""
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                destinationEditText.text = ""

            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.lastLocation?.let { lastLocation ->
                    getAddressFromLocation(lastLocation)
                    stopLocationUpdates()
                }
            }
        }
    }

    private fun searchRoute() {
        val origin = originEditText.text.toString().trim()
        val destination = destinationEditText.text.toString().trim()

        if (origin.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Ingresa una ubicación de origen y destino", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("https://www.google.com/maps/dir/$origin/$destination")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, "No se encontró la aplicación Google Maps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted()) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showDialogForPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permiso de ubicación")
            .setMessage("Se necesita el permiso de ubicación para proporcionar la ubicación actual")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "No se otorgó el permiso de ubicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation?.addOnSuccessListener { location ->
                    if (location != null) {
                        getAddressFromLocation(location)
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                showDialogForPermission()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = try {
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }

        if (addresses != null && addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            val addressText = address.getAddressLine(0)
            runOnUiThread {
                originEditText.text = addressText
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isLocationPermissionGranted) {
            startLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "No se otorgó el permiso de ubicación. Puedes otorgarlo en la configuración de la aplicación.",
                    Toast.LENGTH_LONG
                ).show()

                val appSettingsIntent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + packageName)
                )
                startActivity(appSettingsIntent)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item3 -> {
                val intent = Intent(this, Maps::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item0 -> {
                //Ir a la actividad principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_logout -> {
                //Cerrar sesión de Google
                mAuth!!.signOut()
                mGoogleSignInClient!!.signOut()
                val intent = Intent(this, LogIn::class.java)
                startActivity(intent)
                finish()
                //Cerrar Sesión
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = sharedPreferences.edit()
                editor.remove("token")
                editor.apply()
                val intent2 = Intent(this, LogIn::class.java)
                startActivity(intent2)
                finish()

            }
            R.id.nav_pacientes -> {
                val intent = Intent(this, Pacientes::class.java)
                startActivity(intent)
            }
            R.id.nav_perfil -> {
                val intent = Intent(this, Perfil::class.java)
                startActivity(intent)
            }
            R.id.nav_item2 -> {
                val intent = Intent(this, mainChat::class.java)
                startActivity(intent)


            }
            R.id.nav_item1 -> {
                val intent = Intent(this, MostrarCitas::class.java)
                startActivity(intent)
            }
            R.id.nav_citas -> {
                val intent = Intent(this, AgendarCita::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

}