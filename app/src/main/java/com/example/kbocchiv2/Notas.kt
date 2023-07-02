package com.example.kbocchiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
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

class Notas : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)
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


        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"

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
            fechacreaText.setText(creacion)
            fechamod.setText(edicion)
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
        val citaid = intent.getStringExtra("id_cita")
        val titulo = intent.getStringExtra("titulo")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)


        nota.put("diagnostico",diagnosticoText.text.toString())
        nota.put("observaciones", observacionText.text.toString())
        nota.put("tratamiento", tratamientoText.text.toString())
        nota.put("evolucion", evolucionText.text.toString())
        nota.put("id", notaid)
        nota.put("id_cita", citaid)
        nota.put("titulo", tituloText.text.toString())
        Log.e("ID NOTA ", "ID : $notaid", )

        notadta.put("id", notaid)
        notadta.put("id_terapeuta", token)
        notadta.put("nota", nota)


        val requestBody = nota.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/notas/" + notaid)
            .patch(requestBody)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread{
                    Toast.makeText(this@Notas, "Error", Toast.LENGTH_SHORT).show()
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
                    runOnUiThread {
                        Toast.makeText(this@Notas, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
    }

    
}