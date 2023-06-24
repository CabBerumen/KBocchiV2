package com.example.kbocchiv2

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException

class mainChat : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var socket: Socket
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var textViewChat: TextView
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var drawerLayout: DrawerLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)

        setupSocket()
        socket.connect()

        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        textViewChat = findViewById(R.id.textViewChat)

        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                editTextMessage.setText("")
            }
        }

        socket.on("message", Emitter.Listener { args ->
            runOnUiThread {
                val message = args[0] as String
                displayMessage(message)
            }
        })


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
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_close,
            R.string.navigation_drawer_close
        )

        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()




    }

    private fun setupSocket() {
        try {
            socket = IO.socket("https://kbocchi.onrender.com/")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun sendMessage(message: String) {
        socket.emit("message", message)

    }

    private fun displayMessage(message: String) {
        val currentChat = textViewChat.text.toString()
        val updatedChat = "$currentChat\n$message"
        textViewChat.text = updatedChat

    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item0 -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_logout -> {
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

}




