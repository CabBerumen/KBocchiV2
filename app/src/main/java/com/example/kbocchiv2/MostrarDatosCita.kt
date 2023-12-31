package com.example.kbocchiv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MostrarDatosCita : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var nombrepacient : TextView? = null
    var fechapacient : TextView? = null
    var modalidadpacient : TextView? = null
    var domiciliopacient : TextView? = null
    var horapacient: TextView? = null
    private lateinit var btneditar : Button
    private lateinit var btneliminar : Button

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_datos_cita)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout?.closeDrawer(GravityCompat.START)
        mAuth = FirebaseAuth.getInstance()
        navigationView?.setNavigationItemSelectedListener(this)

        nombrepacient = findViewById(R.id.pacientetext)
        fechapacient = findViewById(R.id.fechatext)
        modalidadpacient = findViewById(R.id.modalidadtext)
        domiciliopacient = findViewById(R.id.domiciliotext)
        horapacient = findViewById(R.id.horatext)
        btneditar = findViewById(R.id.editarbtn)
        btneliminar = findViewById(R.id.eliminarbtn)

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


        val intent = intent
            val pacientecita = intent.getStringExtra("nombre")
            val fechacita = intent.getStringExtra("fecha")
            val modalidadcita = intent.getStringExtra("modalidad")
            val domiciliocita = intent.getStringExtra(("domicilio"))
            val citaid = intent.getStringExtra("id")
            val nombrepaciente = intent.getStringExtra("nombre")
            val idpaciente = intent.getStringExtra("id_paciente")

            val fechaStr : String = fechacita.toString()
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fechaChida = formatoFecha.parse(fechaStr)
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
            val formatoHoraSalida = SimpleDateFormat("h:mm a", Locale("es", "ES"))
            val fechaFormateada = formatoSalida.format(fechaChida)
            val horaFormateada = formatoHoraSalida.format(fechaChida)

            nombrepacient?.setText(pacientecita)
            fechapacient?.setText(fechaFormateada)
            horapacient?.setText(horaFormateada)
            modalidadpacient?.setText(modalidadcita)
            domiciliopacient?.setText(domiciliocita)


        btneditar.setOnClickListener {
            val intent = Intent(this@MostrarDatosCita, ModificarCita::class.java)
            startActivity(intent)
        }

        btneliminar.setOnClickListener {
            eliminarCita()
            val intent = Intent(this@MostrarDatosCita, MostrarCitas::class.java)
            startActivity(intent)
        }
    }

    private fun eliminarCita() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this)
        val citaid = sharedPreferences2.getInt("idcitaeditar", 0)

        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/citas/$citaid")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MostrarDatosCita, "Error en la llamada de red", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    runOnUiThread {
                        Toast.makeText(this@MostrarDatosCita, "Cita eliminada", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = responseData
                    runOnUiThread {
                        Toast.makeText(this@MostrarDatosCita, "Error en la API: $errorMessage", Toast.LENGTH_SHORT).show()
                        Log.d("ERROR MESSAGE:", "Error: $errorMessage")
                    }
                }
            }
        })
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

}