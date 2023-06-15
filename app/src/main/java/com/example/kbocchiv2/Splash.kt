package com.example.kbocchiv2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {
    var handler: Handler? = null
    var runnable: Runnable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        handler = Handler()
        runnable = Runnable {
            val logInActivity = Intent(this@Splash, LogIn::class.java)
            startActivity(logInActivity)
            finish()
        }
        handler!!.postDelayed(runnable!!, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler!!.removeCallbacks(runnable!!)
    }
}