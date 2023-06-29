package com.example.kbocchiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Notas : AppCompatActivity() {
    private lateinit var autorText : TextView
    private lateinit var diagnosticoText : TextView
    private lateinit var observacionText : TextView
    private lateinit var tratamientoText : TextView
    private lateinit var evolucionText : TextView
    private lateinit var fechacreaText : TextView
    private lateinit var fechamod: TextView
    private lateinit var fotoPerfilNotas : CircleImageView
    private lateinit var tituloText : TextView

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
    }
}