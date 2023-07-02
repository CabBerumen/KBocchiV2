package com.example.kbocchiv2

import POJO.RequestExpediente
import POJO.ResultExpediente
import android.content.Context
import android.content.Intent
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
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Expediente : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var apellidos : TextView? = null
   // var nombre: TextView? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PacienteAdapter
    private var pacients: List<RequestExpediente> = ArrayList()

    private var pacienteActual: RequestExpediente? = null

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expediente)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout?.closeDrawer(GravityCompat.START)
        mAuth = FirebaseAuth.getInstance()
        navigationView?.setNavigationItemSelectedListener(this)

        recyclerView = findViewById(R.id.list_pacientes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PacienteAdapter(pacients)
        recyclerView.adapter = adapter

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


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val pacientsJson = sharedPreferences.getString("pacients", null)

        if (pacientsJson != null) {
            val pacients = Gson().fromJson<List<RequestExpediente>>(pacientsJson, object : TypeToken<List<RequestExpediente>>() {}.type)
            adapter.actualizarLista(pacients)
        }

        obtenerDatosDeAPI()
    }

    private fun obtenerDatosDeAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kbocchi.onrender.com") // Reemplaza con la URL base de tu API REST
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java) // Reemplaza "ApiService" con el nombre de tu interfaz de servicio
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val call = apiService.obtenerExpediente(token)

        call.enqueue(object : Callback<List<RequestExpediente>> {
            override fun onResponse(call: Call<List<RequestExpediente>>, response: Response<List<RequestExpediente>>) {
                if (response.isSuccessful) {

                   pacients = response.body()!!

                    adapter.actualizarLista(pacients)

                    Toast.makeText(this@Expediente, "Datos recibidos", Toast.LENGTH_SHORT).show()

                } else {
                    // Manejo de errores en caso de una respuesta no exitosa de la API
                    Toast.makeText(this@Expediente, "Datos no recibidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RequestExpediente>>, t: Throwable) {
                // Manejo de errores en caso de una falla en la llamada a la API
            }
        })
    }

    inner class PacienteAdapter(private var pacientes: List<RequestExpediente>) :
        RecyclerView.Adapter<PacienteViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.expedienteviewholder, parent, false)
            return PacienteViewHolder(itemView)
        }
        override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) {
            val paciente = pacients[position]
            holder.bind(paciente)
            holder.itemView.setOnClickListener {
                val gson = Gson()
                val pacienteJson = gson.toJson(paciente)

                val intent = Intent(this@Expediente, ListaNotas::class.java)
                intent.putExtra("paciente", pacienteJson)
                holder.itemView.context.startActivity(intent)

            }

        }

        override fun getItemCount(): Int {
            return pacients.size
        }

        fun actualizarLista(nuevaLista: List<RequestExpediente>) {
            pacients = nuevaLista
            notifyDataSetChanged()
        }
    }

    inner class PacienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



        var firebaseStorage: FirebaseStorage? = null

        private val cardViewEx: CardView = itemView.findViewById(R.id.cardExpediente)
        private val nombreExp: TextView = itemView.findViewById(R.id.nombreEx)
        private val ultimatext: TextView = itemView.findViewById(R.id.ultimanCita)
        private val fotoexp : CircleImageView = itemView.findViewById(R.id.fotoExpediente)
        private val telexp : TextView = itemView.findViewById(R.id.numeroExpediente)

        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"

        fun bind(paciente: RequestExpediente) {

            pacienteActual = paciente

            nombreExp.text = paciente.nombre
            //ultimatext.text = paciente.fecha
            telexp.text = paciente.telefono



            val fotop = paciente.fotoPerfil

            val imagePath = fotop ?: ""

            val storageReference = if(!imagePath.isNullOrEmpty()){
                storage.reference.child(imagePath)
            }else{
                null
            }
            if(!imagePath.isNullOrEmpty()) {
                storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(fotoexp, object : com.squareup.picasso.Callback {
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
                fotoexp.visibility = View.GONE
                fotoexp.setImageResource(R.drawable.placeholder_image)
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