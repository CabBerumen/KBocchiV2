package com.example.kbocchiv2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RecuperarContrasena : AppCompatActivity() {
    var toolbar: Toolbar? = null
    private lateinit var botoncontrasena : Button
    private lateinit var correo : EditText
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_contrasena)
        setSupportActionBar(toolbar)
        toolbar = findViewById(R.id.toolbar)
        correo = findViewById(R.id.Editcorreo)
        botoncontrasena = findViewById(R.id.btnrecuperar)

        botoncontrasena.setOnClickListener {
            recuperarContrasena()
        }

        correo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val correo = charSequence.toString()

                if (esCorreoValido(correo)) { botoncontrasena.isEnabled= true }
                else {
                    botoncontrasena.isEnabled = false
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

    }
    private fun esCorreoValido(correo: String): Boolean {
        return correo.contains("@") && correo.contains(".com") && correo.contains("gmail")
    }

    private fun recuperarContrasena() {
        val correoData = JSONObject()

        correoData.put("email", correo.text.toString())


        val requestBody = correoData.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/usuarios/solicitarReestablecerContrasena")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al mandar el correo", Toast.LENGTH_SHORT).show()

                }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Se ha mandado el correo", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error en la api", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}