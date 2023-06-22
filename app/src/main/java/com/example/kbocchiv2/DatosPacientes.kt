package com.example.kbocchiv2

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import javax.sql.DataSource
import com.bumptech.glide.request.target.Target
import com.squareup.picasso.Callback


class DatosPacientes : AppCompatActivity() {

    var nombrep : TextView? = null
    var telp: TextView? = null
    var correo: TextView? = null
    private lateinit var imgpaciente: ImageView

    var firebaseStorage: FirebaseStorage? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_pacientes)
        nombrep = findViewById(R.id.txtnombre)
        telp = findViewById(R.id.txttelefono)
        imgpaciente = findViewById(R.id.fotopaciente)
        correo = findViewById(R.id.txtemail)


        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"


        val intent = intent
        if (intent != null) {
            val nombre = intent.getStringExtra("nombre")
            val email = intent.getStringExtra("email")
            val telefono = intent.getStringExtra("telefono")
            val fotopaciente = intent.getStringExtra(("fotoPerfil"))

            val imagePath = fotopaciente ?: ""

            val storageReference = if(!imagePath.isNullOrEmpty()){
                storage.reference.child(imagePath)
            }else{
                null
            }

            nombrep?.setText(nombre)
            correo?.setText(email)
            telp?.setText(telefono)

            if(!imagePath.isNullOrEmpty()) {
                storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.placeholder_image)
                        .into(imgpaciente, object : Callback {
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
                imgpaciente.visibility = View.GONE
                imgpaciente.setImageResource(R.drawable.placeholder_image)
            }

            Log.d("DatosPacientes", "Nombre: $nombre")
            Log.d("DatosPacientes", "Email: $email")
            Log.d("DatosPacientes", "Teléfono: $telefono")
            Log.d("DatosPacientes", "Foto de perfil: $fotopaciente")


        }

    }
}