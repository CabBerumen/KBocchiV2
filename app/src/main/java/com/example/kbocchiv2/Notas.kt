package com.example.kbocchiv2

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceFragmentCompat
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.squareup.picasso.Callback
import java.text.SimpleDateFormat
import java.util.Locale

class Notas : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var autorText : TextView
    private lateinit var diagnosticoText : EditText
    private lateinit var observacionText : EditText
    private lateinit var tratamientoText : EditText
    private lateinit var evolucionText : EditText
    private lateinit var fechacreaText : TextView
    private lateinit var fechamod: TextView
    private lateinit var fotoPerfilNotas : CircleImageView
    private lateinit var tituloText : TextView
    private lateinit var editarBtn : Button
    private lateinit var guardarBtn : Button
    private val client = OkHttpClient()

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)
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


        autorText = findViewById(R.id.nombreTerapeutaNotas)
        diagnosticoText = findViewById(R.id.diagnosticoNotas)
        observacionText = findViewById(R.id.observacionesNotas)
        tratamientoText = findViewById(R.id.tratamientoNotas)
        evolucionText = findViewById(R.id.evolucionNotas)
        fechacreaText = findViewById(R.id.fechacreacionNotas)
        fechamod = findViewById(R.id.fechamodNotas)
        fotoPerfilNotas = findViewById(R.id.fotoNotas)
        tituloText = findViewById(R.id.tituloNotas)
        editarBtn = findViewById(R.id.editButton)
        guardarBtn = findViewById(R.id.guardarButton)

        val intent = intent
        if (intent != null) {
            val autor = intent.getStringExtra("nombre")
            val diagn = intent.getStringExtra("diagnostico")
            val trata = intent.getStringExtra("tratamiento")
            val observar = intent.getStringExtra("observaciones")
            val evol = intent.getStringExtra(("evolucion"))
            val creacion = intent.getStringExtra(("fecha_creacion"))
            val edicion = intent.getStringExtra(("fecha_edicion"))
            val titulo = intent.getStringExtra(("titulo"))
            val fotito = intent.getStringExtra(("foto_perfil"))

            val fechaStr : String? = creacion
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fechaChida = formatoFecha.parse(fechaStr)
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
            val formatoHoraSalida = SimpleDateFormat("h:mm a", Locale("es", "ES"))
            val fechaFormateada = formatoSalida.format(fechaChida)
            val horaFormateada = formatoHoraSalida.format(fechaChida)

            val fechaStr2 : String? = edicion
            val formatoFecha2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fechaChida2 = formatoFecha2.parse(fechaStr2)
            val formatoSalida2 = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
            val formatoHoraSalida2 = SimpleDateFormat("h:mm a", Locale("es", "ES"))
            val fechaFormateada2 = formatoSalida2.format(fechaChida2)
            val horaFormateada2 = formatoHoraSalida2.format(fechaChida2)


            val imagePath = fotito ?: ""

            val storageReference = if(!imagePath.isNullOrEmpty()){
                storage.reference.child(imagePath)
            }else{
                null
            }

            autorText.setText(autor)
            diagnosticoText.setText(diagn)
            observacionText.setText(observar)
            tratamientoText.setText(trata)
            evolucionText.setText(evol)
            fechacreaText.setText(fechaFormateada)
            fechamod.setText(fechaFormateada2)
            tituloText.setText(titulo)


            if(!imagePath.isNullOrEmpty()) {
                storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(fotoPerfilNotas, object : Callback {
                            override fun onSuccess() {
                                // La imagen se ha cargado correctamente
                            }

                            override fun onError(e: Exception?) {
                                Log.e("DatosPacientes", "Error al cargar la imagen", e)
                            }
                        })

                }?.addOnFailureListener { exception ->
                    Log.e(
                        "DatosPacientes",
                        "Error al obtener la URL de descarga de la imagen",
                        exception
                    )
                }
            } else {
                fotoPerfilNotas.visibility = View.GONE
                fotoPerfilNotas.setImageResource(R.drawable.placeholder_image)
            }


        }

        editarBtn.setOnClickListener{

            diagnosticoText.isEnabled =true

            observacionText.isEnabled = true

            evolucionText.isEnabled = true

            tratamientoText.isEnabled = true

        }

        guardarBtn.setOnClickListener {
            guardarnota()
            diagnosticoText.isEnabled =false

            observacionText.isEnabled = false

            evolucionText.isEnabled = false

            tratamientoText.isEnabled = false

        }
    }

    private fun guardarnota(){
        val nota = JSONObject()
        val notadta = JSONObject()
        val intent = intent
        val notaid = intent.getStringExtra("id")
        //val citaid = intent.getStringExtra("id_cita",)
        //val titulo = intent.getStringExtra("titulo")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this)
        val citaid = sharedPreferences2.getInt("idcitaNota", 0)


        nota.put("diagnostico",diagnosticoText.text.toString())
        nota.put("observaciones", observacionText.text.toString())
        nota.put("tratamiento", tratamientoText.text.toString())
        nota.put("evolucion", evolucionText.text.toString())
        nota.put("id", notaid)
        nota.put("id_cita", citaid)
        nota.put("titulo", tituloText.text.toString())
        Log.e("ID NOTA ", "ID : $notaid", )
        Log.e("ID CITA ", "ID : $citaid", )


        notadta.put("id", notaid)
        notadta.put("id_terapeuta", token)
        notadta.put("nota", nota)


        val requestBody = notadta.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/notas")
            .patch(requestBody)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread{
                    Toast.makeText(this@Notas, "Error en la llamada de red", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null){
                    val notaactualizada = JSONObject (responseData)
                    runOnUiThread{
                        Toast.makeText(this@Notas, "Nota actualizada", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val errorMessage = responseData
                    runOnUiThread {
                        Toast.makeText(this@Notas, "Error en la API: $errorMessage", Toast.LENGTH_SHORT).show()
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