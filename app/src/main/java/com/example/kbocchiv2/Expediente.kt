package com.example.kbocchiv2

import POJO.RequestExpediente
import POJO.ResultExpediente
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbocchiv2.Interfaces.ApiService
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

class Expediente : AppCompatActivity() {

    var apellidos : TextView? = null
   // var nombre: TextView? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PacienteAdapter
    private var pacients: List<RequestExpediente> = ArrayList()

    private var pacienteActual: RequestExpediente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expediente)

        recyclerView = findViewById(R.id.list_pacientes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PacienteAdapter(pacients)
        recyclerView.adapter = adapter

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
}