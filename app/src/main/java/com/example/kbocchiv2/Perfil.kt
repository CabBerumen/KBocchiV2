package com.example.kbocchiv2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Perfil : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null


    var emailText: TextView? = null
    var nombreText: TextView? = null
    var telText: TextView? = null
    var domiText: TextView? = null
    var consultText: TextView? = null
    var cedulatText: TextView? = null
    var pagomaxText: TextView? = null
    var pagominText: TextView? = null
    var rangoText: TextView? = null
    private lateinit var imgperfil2: CircleImageView

    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        emailText = findViewById(R.id.id_email)
        nombreText = findViewById(R.id.id_nombre)
        telText = findViewById(R.id.id_tel)
        domiText = findViewById(R.id.id_domicilio)
        consultText = findViewById(R.id.id_consultorio)
        cedulatText = findViewById(R.id.id_cedula)
        pagomaxText = findViewById(R.id.id_pagomax)
        pagominText = findViewById(R.id.id_pagomin)
        rangoText = findViewById(R.id.id_rango)
        imgperfil2 = findViewById(R.id.profile_image)

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

        val sharedPreferences = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val nombre = sharedPreferences.getString("nombre", "")
        val telefono = sharedPreferences.getString("telefono", "")
        val domicilio = sharedPreferences.getString("domicilio", "")
        val consultorio = sharedPreferences.getString("nombre_del_consultorio", "")
        val cedula = sharedPreferences.getString("numero_cedula", "")
        val pagomax = sharedPreferences.getString("pago_maximo", "")
        val pagomin = sharedPreferences.getString("pago_minimo", "")
        val rango = sharedPreferences.getString("rango_servicio", "")
        val fototerapeuta = sharedPreferences.getString("foto_perfil", "")


        val image = fototerapeuta ?: ""

        val storageReference = if(!image.isNullOrEmpty()){
            storage.reference.child(image)
        }else{
            null
        }

        emailText?.setText(email)
        nombreText?.setText(nombre)
        telText?.setText(telefono)
        consultText?.setText(consultorio)
        domiText?.setText(domicilio)
        cedulatText?.setText(cedula)
        pagomaxText?.setText(pagomax)
        pagominText?.setText(pagomin)
        rangoText?.setText(rango)

        if(!image.isNullOrEmpty()) {
            storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri)
                    .into(imgperfil2, object : Callback {
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
            imgperfil2.visibility = View.GONE
            imgperfil2.setImageResource(R.drawable.placeholder_image)
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

