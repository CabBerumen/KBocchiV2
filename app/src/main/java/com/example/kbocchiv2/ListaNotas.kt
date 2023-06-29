package com.example.kbocchiv2

import POJO.NotasBitacora
import POJO.RequestBitacora
import POJO.RequestExpediente
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListaNotas : AppCompatActivity() {

    private lateinit var recyclerListaNotas : RecyclerView
    private lateinit var adapter : ListaNotasAdapter
    private var listanotas : List<NotasBitacora> = ArrayList()

    private lateinit var paciente: RequestExpediente


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_notas)
        recyclerListaNotas = findViewById(R.id.listanotasRecycler)

        val pacienteJson = intent.getStringExtra("paciente")
        val gson = Gson()
        val tipoPaciente = object : TypeToken<RequestExpediente>() {}.type
         paciente = gson.fromJson<RequestExpediente>(pacienteJson, tipoPaciente)

        recyclerListaNotas.layoutManager = LinearLayoutManager(this)
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

        call.enqueue(object : Callback<List<NotasBitacora>> {
            override fun onResponse(call: Call<List<NotasBitacora>>, response: Response<List<NotasBitacora>>) {
                if (response.isSuccessful) {

                    val resultListaNotas = response.body()
                    Log.d("API Response", "Response: $resultListaNotas")

                    if (resultListaNotas != null) {
                        listanotas = resultListaNotas
                        adapter.actualizarLista(listanotas)

                    }



                    Toast.makeText(this@ListaNotas, "Datos recibidos", Toast.LENGTH_SHORT).show()

                } else {
                    Log.e("API Response", "Error en la respuesta de la API: ${response.code()}")
                    // Manejo de errores en caso de una respuesta no exitosa de la API
                    Toast.makeText(this@ListaNotas, "Datos no recibidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NotasBitacora>>, t: Throwable) {
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
                intent.putExtra("nombre", listan.cita?.terapeutaDatos?.usuario?.nombre)
                intent.putExtra("diagnostico", listan.diagnostico)
                intent.putExtra("observaciones", listan.observaciones)
                intent.putExtra("tratamiento", listan.tratamiento)
                intent.putExtra("evolucion", listan.evolucion)
                intent.putExtra("fecha_creacion", listan.fechaCreacion)
                intent.putExtra("fecha_edicion", listan.fechaEdicion)
                intent.putExtra("titulo", listan.titulo)
                intent.putExtra("foto_perfil", listan.cita?.terapeutaDatos?.usuario?.fotoPerfil)
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

            textNombreNota.text = list.titulo
            textfechaNota.text = list.fechaCreacion

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

}