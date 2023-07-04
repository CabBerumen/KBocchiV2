package com.example.kbocchiv2

import java.util.ArrayList
import POJO.NotasBitacora
import POJO.RequestBitacora
import POJO.RequestExpediente
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.integrity.e
import com.google.android.play.core.integrity.i
import com.google.android.play.integrity.internal.j
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class ListaNotas : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerListaNotas : RecyclerView
    private lateinit var adapter : ListaNotasAdapter
    private var listanotas : ArrayList<NotasBitacora> = ArrayList()

    private lateinit var paciente: RequestExpediente

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_notas)
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


        recyclerListaNotas = findViewById(R.id.listanotasRecycler)

        val pacienteJson = intent.getStringExtra("paciente")
        val gson = Gson()
        val tipoPaciente = object : TypeToken<RequestExpediente>() {}.type
         paciente = gson.fromJson<RequestExpediente>(pacienteJson, tipoPaciente)

        val spanCount = 2
        val layoutManager = GridLayoutManager(this, spanCount)
        recyclerListaNotas.layoutManager = layoutManager
        adapter = ListaNotasAdapter(listanotas)
        recyclerListaNotas.adapter = adapter



        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val pacientsJson = sharedPreferences.getString("listanotes", null)

        if (pacientsJson != null) {
            val listanotes = Gson().fromJson<List<NotasBitacora>>(pacientsJson, object : TypeToken<List<NotasBitacora>>() {}.type)
            adapter.actualizarLista(listanotes)
        }

        obtenerDatosDeAPI()

    }

    private fun obtenerDatosDeAPI() {

        val logginInterceptor = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger{
            override fun log(message: String){
                Log.d("Api Request", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logginInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://kbocchi.onrender.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)

        val call = apiService.obtenerListaNotas(token, paciente.id.toString())

        call.enqueue(object : Callback<List<RequestBitacora>> {
            override fun onResponse(call: Call<List<RequestBitacora>>, response: Response<List<RequestBitacora>>) {
                if (response.isSuccessful) {
                    val resultListaNotas = response.body()
                    Log.d("API Response", "Response: $resultListaNotas")

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    //resultListaNotas?.forEach(e -> e.forEach(j -> listanotas.add(j)))
                      //listanotas = resultListaNotas!!.get(0).notas
                    for (i in 0 until resultListaNotas!!.size) {
                        for (j in 0 until resultListaNotas!!.get(i).notas.size) {
                            val nota = resultListaNotas!!.get(i).notas.get(j)
                            val fechaCreacionString = nota.fechaCreacion
                            val fechaCreacionDate = dateFormat.parse(fechaCreacionString)
                            nota.fechaCreacionDate = fechaCreacionDate
                            listanotas.add(nota)
                        }
                    }
                    listanotas.sortBy { it.fechaCreacionDate }
                    adapter.actualizarLista(listanotas)
                    adapter.actualizarLista(listanotas)

                    Toast.makeText(this@ListaNotas, "Datos recibidos", Toast.LENGTH_SHORT).show()

                } else {
                    Log.e("API Response", "Error en la respuesta de la API: ${response.code()}")
                    // Manejo de errores en caso de una respuesta no exitosa de la API
                    Toast.makeText(this@ListaNotas, "Datos no recibidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RequestBitacora>>, t: Throwable) {
                Toast.makeText(this@ListaNotas, "Error en la llamada a la API: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ListaNotas", "Error en la llamada a la API: ${t.message}", t)
            }
        })
    }

    inner class ListaNotasAdapter(private var nuevaLista: List<NotasBitacora>) :
        RecyclerView.Adapter<ListaNotasViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaNotasViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.notas_holderview, parent, false)
            return ListaNotasViewHolder(itemView)
        }
        override fun onBindViewHolder(holder: ListaNotasViewHolder, position: Int) {
            val listan = nuevaLista[position]
            holder.bind(listan)
            holder.itemView.setOnClickListener {
                val intent = Intent(this@ListaNotas, Notas::class.java)

                val id_Cita = listan.idCita
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@ListaNotas)
                val editor = sharedPreferences.edit()
                editor.putInt("idcitaNota", id_Cita)
                editor.apply()

                intent.putExtra("nombre", listan.cita?.terapeutaDatos?.usuario?.nombre)
                intent.putExtra("id", listan.id.toString())
                intent.putExtra("diagnostico", listan.diagnostico)
                intent.putExtra("observaciones", listan.observaciones)
                intent.putExtra("tratamiento", listan.tratamiento)
                intent.putExtra("evolucion", listan.evolucion)
                intent.putExtra("fecha_creacion", listan.fechaCreacion)
                intent.putExtra("fecha_edicion", listan.fechaEdicion)
                intent.putExtra("titulo", listan.titulo)
                intent.putExtra("foto_perfil", listan.cita?.terapeutaDatos?.usuario?.fotoPerfil)
                intent.putExtra("id_cita", listan.idCita)
                startActivity(intent)

            }

        }
        override fun getItemCount(): Int {
            return listanotas.size
        }

        fun actualizarLista(nuevaLista: List<NotasBitacora>) {
            this.nuevaLista = nuevaLista
            Log.d("Adapter", "List updated: $nuevaLista")
            notifyDataSetChanged()

        }
    }

    inner class ListaNotasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var firebaseStorage: FirebaseStorage? = null

        private val cardViewLista: CardView = itemView.findViewById(R.id.cardListaNotas)
        private val textNombreNota: TextView = itemView.findViewById(R.id.textListaNotas)
        private val textfechaNota: TextView = itemView.findViewById(R.id.fechaLista)
        private val fotolista: CircleImageView = itemView.findViewById(R.id.fotoListaNotas)


        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"

        fun bind(list: NotasBitacora) {

            val fechaStr : String = list.fechaCreacion
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fechaChida = formatoFecha.parse(fechaStr)
            val formatoSalida = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
            val formatoHoraSalida = SimpleDateFormat("h:mm a", Locale("es", "ES"))
            val fechaFormateada = formatoSalida.format(fechaChida)
            val horaFormateada = formatoHoraSalida.format(fechaChida)

            textNombreNota.text = list.titulo
            textfechaNota.text = fechaFormateada

            val fotodelista = list.cita?.terapeutaDatos?.usuario?.fotoPerfil

            Log.d("ViewHolder", "Binding data: $list")

            val imagePath = fotodelista ?: ""

            val storageReference = if(!imagePath.isNullOrEmpty()){
                storage.reference.child(imagePath)
            }else{
                null
            }
            if(!imagePath.isNullOrEmpty()) {
                storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(fotolista, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
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
                fotolista.visibility = View.GONE
                fotolista.setImageResource(R.drawable.placeholder_image)
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


