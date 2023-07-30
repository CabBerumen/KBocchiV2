package com.example.kbocchiv2

import POJO.RequestCitas
import POJO.ResultCita
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbocchiv2.Interfaces.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MostrarCitas : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PacienteAdapter
    private lateinit var calendarView: CalendarView
    private var pacient: List<RequestCitas> = ArrayList()
    var toolbar: Toolbar? = null

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_citas)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        calendarView = findViewById(R.id.calendarViewCitas)

        drawerLayout?.closeDrawer(GravityCompat.START)

        navigationView?.setNavigationItemSelectedListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

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


        //Bloquea las fechas anteriores en el Calendarario
        calendarView.minDate = System.currentTimeMillis()

        //Al seleccionar una fecha en el calendario, muestra las citas de esa fecha
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val citasFiltradas = filtrarCitasPorFecha(selectedDate)
            adapter.actualizarLista(citasFiltradas)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("citas").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MostrarCitas", "Successfully subscribed to the topic 'citas'")
                } else {
                    Log.e("MostrarCitas", "Failed to subscribe to the topic 'citas'")
                }
            }


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

                    if (resultCita?.getCitas()?.isNotEmpty() == true) {
                        // Check if the VIBRATE permission is granted
                        if (ActivityCompat.checkSelfPermission(this@MostrarCitas, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                            // If VIBRATE permission is already granted, show the notification directly
                            mostrarNotificacion("Nueva Cita", "Has recibido una nueva cita.")
                        } else {
                            // If VIBRATE permission is not granted, request it
                            val requestCode = 123 // Use any unique request code here
                            ActivityCompat.requestPermissions(this@MostrarCitas, arrayOf(Manifest.permission.VIBRATE), requestCode)
                        }
                    }



                    Log.d("MostrarCitas", "Datos recibidos: $pacient")
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

    private fun filtrarCitasPorFecha(fecha: Date): List<RequestCitas> {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatoFechaSolo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaSeleccionada = formatoFechaSolo.format(fecha)

        return pacient.filter { paciente ->
            val fechaCita = formatoFecha.parse(paciente.fecha)
            val fechaCitaSolo = formatoFechaSolo.format(fechaCita)
            fechaCitaSolo == fechaSeleccionada
        }
    }


    inner class PacienteAdapter(private var pacient: List<RequestCitas>) :
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

                val idpacient = paciente.idPaciente
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MostrarCitas)
                val editor = sharedPreferences.edit()
                editor.putInt("idpacient", idpacient)
                editor.apply()

                val idcita = paciente.id
                val sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this@MostrarCitas)
                val editor2 = sharedPreferences2.edit()
                editor2.putInt("idcitaeditar", idcita)
                editor2.apply()

                intent.putExtra("nombre", paciente.nombre)
                intent.putExtra("fecha", paciente.fecha)
                intent.putExtra("domicilio", paciente.domicilio)
                intent.putExtra("modalidad", paciente.modalidad)
                intent.putExtra("id_paciente", paciente.idPaciente)
                intent.putExtra("id", paciente.id)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return pacient.size
        }

        fun actualizarLista(nuevaLista: List<RequestCitas>) {
            pacient = nuevaLista.sortedBy { cita -> cita.fecha }
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


            val fechaActual = Calendar.getInstance().time
            val calendarChida = Calendar.getInstance()
            calendarChida.time = fechaChida
            if (calendarChida.before(Calendar.getInstance())) {
                // fechaChida es anterior a la fecha actual
                cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.cita_pasada))
            } else if (calendarChida.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                calendarChida.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                calendarChida.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                calendarChida.get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                // fechaChida tiene la misma fecha que la actual, pero una hora anterior
                cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.cita_pasada))
            } else {
                // fechaChida es futura
                cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.cita_futura))
            }


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

    private fun mostrarNotificacion(title: String, body: String) {
        // The mostrarNotificacion function from the MyFirebaseMessagingService
        // Ensure the code for mostrarNotificacion function from MyFirebaseMessagingService
        // is accessible from this Activity
        // ... (your existing mostrarNotificacion code, if any) ...
    }

    // ... (rest of your activity code) ...

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) { // Use the same request code you used while requesting the permission
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The VIBRATE permission is granted. You can proceed with showing the notification.
                mostrarNotificacion("Nueva Cita", "Has recibido una nueva cita.")
            } else {
                // The VIBRATE permission is not granted. Handle the scenario accordingly.
                // In this example, I'll just log a message.
                Log.d("MostrarCitas", "VIBRATE permission not granted.")
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

}

