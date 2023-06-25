package com.example.kbocchiv2

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var boton: Button? = null
    var archivo: SharedPreferences? = null
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var btnspeech: ImageButton? = null
    private var speechToText: SpeechToText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        drawerLayout?.closeDrawer(GravityCompat.START)
        mAuth = FirebaseAuth.getInstance()
        btnspeech = findViewById(R.id.btnSpeech)
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

        btnspeech?.setOnClickListener(View.OnClickListener {
            startSpeechToText()
        })

        speechToText = SpeechToText(this)


    }

    private fun startSpeechToText() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.RECORD_AUDIO), MainActivity.REQUEST_PERMISSION)
        } else {
            speechToText!!.startListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.REQUEST_PERMISSION && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            speechToText!!.startListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechToText!!.destroy()
    }

    fun displayResult(result: String?) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
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
            R.id.nav_item2 -> {
                val intent = Intent(this, mainChat::class.java)
                startActivity(intent)


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

    companion object {
        private const val REQUEST_PERMISSION = 1
    }
}

