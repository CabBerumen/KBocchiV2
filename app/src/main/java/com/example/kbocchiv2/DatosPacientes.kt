package com.example.kbocchiv2

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Callback
import de.hdodenhof.circleimageview.CircleImageView


class DatosPacientes : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var nombrep : TextView? = null
    var telp: TextView? = null
    var correo: TextView? = null
    private lateinit var imgpaciente: CircleImageView

    var firebaseStorage: FirebaseStorage? = null

    var toolbar: Toolbar? = null

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_pacientes)
        nombrep = findViewById(R.id.txtnombre)
        telp = findViewById(R.id.txttelefono)
        imgpaciente = findViewById(R.id.fotopaciente)
        correo = findViewById(R.id.txtemail)
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

        val toogle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_close, R.string.navigation_drawer_open)
        drawerLayout?.addDrawerListener(toogle)
        toogle.syncState()

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item3 -> {
                val intent = Intent(this, Maps::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item0 -> {
                //Ir a la actividad principal
                val intent = Intent(this, MainActivity::class.java)
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
            R.id.nav_pacientes -> {
                val intent = Intent(this, Pacientes::class.java)
                startActivity(intent)
            }
            R.id.nav_perfil -> {
                val intent = Intent(this, Perfil::class.java)
                startActivity(intent)
            }
            R.id.nav_citas -> {
                val intent = Intent(this, AgendarCita::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}