package com.example.kbocchiv2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var boton: Button? = null
    var archivo: SharedPreferences? = null
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var btnspeech: ImageButton? = null
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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item3 -> {
                val intent = Intent(this, Maps::class.java)
                startActivity(intent)
                finish()
            }
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
