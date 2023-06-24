package com.example.kbocchiv2

import POJO.RequestPacientes
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Pacientes : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var apellidos : TextView? = null
    var nombre: TextView? = null

    private lateinit var listView: ListView
    private var pacientes: List<RequestPacientes> = ArrayList()

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null

    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null

    var toolbar: Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacientes)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)


        listView = findViewById<ListView>(R.id.listapacientes)
        pacientes = ArrayList()

        obtenerDatosDeAPI()

        drawerLayout?.closeDrawer(GravityCompat.START)
        mAuth = FirebaseAuth.getInstance()
        navigationView?.setNavigationItemSelectedListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val toogle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_close, R.string.navigation_drawer_open)
        drawerLayout?.addDrawerListener(toogle)
        toogle.syncState()

    }

    private fun obtenerDatosDeAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kbocchi.onrender.com") // Reemplaza con la URL base de tu API REST
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java) // Reemplaza "ApiService" con el nombre de tu interfaz de servicio
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val call = apiService.obtenerPacientes(token) // Reemplaza "obtenerPacientes" con el nombre del método de tu API para obtener los datos de los pacientes

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
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

}

