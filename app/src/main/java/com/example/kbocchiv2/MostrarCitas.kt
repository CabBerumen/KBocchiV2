package com.example.kbocchiv2

import POJO.RequestCitas
import POJO.RequestPacientes
import POJO.ResultCita
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import okhttp3.Request
import okio.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MostrarCitas : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PacienteAdapter
    private var pacient: List<RequestCitas> = ArrayList()
    var toolbar: Toolbar? = null

    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_citas)
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

        val sharedPreferences2 = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
        val fototerapeuta = sharedPreferences2.getString("foto_perfil", "")
        val email = sharedPreferences2.getString("email", "")
        val nombre = sharedPreferences2.getString("nombre", "")

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

        recyclerView = findViewById(R.id.recyclerCita)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PacienteAdapter(pacient)
        recyclerView.adapter = adapter

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val pacientesJson = sharedPreferences.getString("pacientes", null)

        if (pacientesJson != null) {
            val pacientes = Gson().fromJson<List<RequestCitas>>(pacientesJson, object : TypeToken<List<RequestCitas>>() {}.type)
            adapter.actualizarLista(pacientes)
        }

        obtenerDatosDeAPI()

    }

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
                call: Call<ResultCita>, response: Response<ResultCita>) {
                Log.d("API_Response", "Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val resultCita = response.body()

                    val SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MostrarCitas)
                    val editor = SharedPreferences.edit()
                    val pacientesJson = Gson().toJson(pacient)
                    editor.putString("CitaPacientes", pacientesJson)
                    editor.apply()

                    pacient = resultCita?.getCitas() ?: emptyList()


                    adapter.actualizarLista(pacient)

                    Log.d("MostrarCitas", "Datos recibidos: $pacient")
                    Toast.makeText(this@MostrarCitas, "Datos recibidos", Toast.LENGTH_SHORT).show()
                } else {

                    Log.e("MostrarCitas", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(this@MostrarCitas, "Datos no recibidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultCita>, t: Throwable) {

                Log.e("MostrarCitas", "Error en la llamada a la API: ${t.message}")
            }
        })
    }

    inner class PacienteAdapter(private var pacientessss: List<RequestCitas>) :
    RecyclerView.Adapter<PacienteViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.citasviewholder, parent, false)
            return PacienteViewHolder(itemView)
        }
        override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) {
            val paciente = pacient[position]
            holder.bind(paciente)
            holder.itemView.setOnClickListener {
                val intent = Intent(this@MostrarCitas, MostrarDatosCita::class.java)
                intent.putExtra("nombre", paciente.nombre)
                intent.putExtra("fecha", paciente.fecha)
                intent.putExtra("domicilio", paciente.domicilio)
                intent.putExtra("modalidad", paciente.modalidad)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return pacient.size
        }

        fun actualizarLista(nuevaLista: List<RequestCitas>) {
            pacient = nuevaLista
            notifyDataSetChanged()
        }
    }

    inner class PacienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var firebaseStorage: FirebaseStorage? = null

        private val cardView: CardView = itemView.findViewById(R.id.cardCita)
        private val fechaText: TextView = itemView.findViewById(R.id.FechaCita)
        private val nombreText: TextView = itemView.findViewById(R.id.nombrePaciente)
        private val fotoRv : CircleImageView = itemView.findViewById(R.id.fotorecycler)
        private val horaText : TextView = itemView.findViewById(R.id.HoraCita)

        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"

        fun bind(paciente: RequestCitas) {

            val fechaStr : String = paciente.fecha
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fechaChida = formatoFecha.parse(fechaStr)
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
            val formatoHoraSalida = SimpleDateFormat("h:mm a", Locale("es", "ES"))
            val fechaFormateada = formatoSalida.format(fechaChida)
            val horaFormateada = formatoHoraSalida.format(fechaChida)


            fechaText.text = fechaFormateada
            horaText.text = horaFormateada
            nombreText.text = paciente.nombre

            val fotoview = paciente.fotoPerfil

            val imagePath = fotoview ?: ""

            val storageReference = if(!imagePath.isNullOrEmpty()){
                storage.reference.child(imagePath)
            }else{
                null
            }
            if(!imagePath.isNullOrEmpty()) {
                storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(fotoRv, object : com.squareup.picasso.Callback {
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
                fotoRv.visibility = View.GONE
                fotoRv.setImageResource(R.drawable.placeholder_image)
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

