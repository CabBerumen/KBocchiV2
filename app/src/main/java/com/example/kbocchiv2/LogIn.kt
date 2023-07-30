package com.example.kbocchiv2

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.kbocchiv2.Interfaces.ApiService
import com.example.kbocchiv2.Request.LoginRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LogIn : AppCompatActivity() {


    private val BASE_URL = "https://kbocchi.onrender.com/"
    private val RC_SIGN_IN = 1
    private lateinit var apiService: ApiService
    private lateinit var editusuario: EditText
    private lateinit var editpass: EditText
    private lateinit var botonlogin: Button
    public lateinit var terapeutas : LoginRequest
    private lateinit var restablecerContrasena: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        editusuario = findViewById(R.id.user);
        editpass = findViewById(R.id.pass);
        botonlogin = findViewById(R.id.login)
        restablecerContrasena = findViewById(R.id.restablecer)

        restablecerContrasena.setOnClickListener {
            val intent = Intent(this@LogIn, RecuperarContrasena::class.java)
            startActivity(intent)
            finish()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                val correo = editusuario.text.toString()
                val contrasena = editpass.text.toString()

                val correoValido = esCorreoValido(correo)
                val contrasenaValida = cumpleRequisitos(contrasena)

                botonlogin.isEnabled = correoValido && contrasenaValida
            }

            override fun afterTextChanged(editable: Editable) {
            }
        }

        editusuario.addTextChangedListener(textWatcher)
        editpass.addTextChangedListener(textWatcher)

        terapeutas = LoginRequest()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)
        if (token != null) {
            val inicio = Intent(this, MainActivity::class.java)
            inicio.putExtra("id", token)
            startActivity(inicio)
            finish() // Finalizar la actividad actual para que no se pueda volver atrás
        }


        botonlogin.setOnClickListener {
            val email = editusuario.text.toString().trim()
            val contrasena = editpass.text.toString().trim()

            val loggin = HttpLoggingInterceptor()
            loggin.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = okhttp3.OkHttpClient.Builder()
            httpClient.addInterceptor(loggin)
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val login = retrofit.create(ApiService::class.java)
            val call = login.login_call(email, contrasena)
            call.enqueue(object : Callback<LoginRequest> {
                override fun onResponse(call: Call<LoginRequest>, response: Response<LoginRequest>){
                    if (response.isSuccessful && response.body() != null){
                        editusuario.text.clear()
                        editpass.text.clear()

                        terapeutas = response.body()!!

                        val tokenInter = terapeutas.terapeuta.id
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@LogIn)
                        val editor = sharedPreferences.edit()
                        editor.putString("token", tokenInter.toString())

                        editor.apply()
                        val inicio = Intent(this@LogIn, MainActivity::class.java)
                        inicio.putExtra("id", tokenInter)
                        startActivity(inicio)
                        finish()

                        val sharedPreferences2 = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
                        val editor2 = sharedPreferences2.edit()
                        editor2.putString("id", terapeutas.id)
                        editor2.putString("id_usuario", terapeutas.terapeuta.idUsuario)
                        editor2.putString("email", terapeutas.email)
                        editor2.putString("nombre", terapeutas.nombre)
                        editor2.putString("telefono", terapeutas.telefono)
                        editor2.putString("domicilio", terapeutas.terapeuta.domicilio)
                        editor2.putString("nombre_del_consultorio", terapeutas.terapeuta.nombreDelConsultorio)
                        editor2.putString("numero_cedula", terapeutas.terapeuta.numeroCedula)
                        editor2.putString("pago_maximo", terapeutas.terapeuta.pagoMaximo.toString())
                        editor2.putString("pago_minimo", terapeutas.terapeuta.pagoMinimo.toString())
                        editor2.putString("rango_servicio", terapeutas.terapeuta.rangoServicio.toString())
                        editor2.putString("foto_perfil", terapeutas.fotoPerfil)
                        editor2.apply()

                        val id_Usuario = terapeutas.terapeuta.idUsuario
                        val sharedPreferences4 = PreferenceManager.getDefaultSharedPreferences(this@LogIn)
                        val editor4 = sharedPreferences4.edit()
                        editor4.putString("idusuario", id_Usuario.toString())
                        editor4.apply()
                        Log.e("id paciente ", "ID DEL TERAPEUTA: $id_Usuario", )

                        notificacion()
                        Toast.makeText(this@LogIn, "Inicio de sesion exitoso", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@LogIn, "Datos Incorrectos", Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onFailure(call: Call<LoginRequest>, t: Throwable){
                    Toast.makeText(this@LogIn, "Error en la conexion", Toast.LENGTH_SHORT).show()

                }
            })
        }

    }
    private fun esCorreoValido(correo: String): Boolean {

    return correo.contains("@") && correo.contains(".com") && correo.contains("gmail")
    }
    private fun cumpleRequisitos(contrasena: String): Boolean {
        val longitudMinima = 8
        val letraMayuscula = contrasena.matches(Regex(".*[A-Z].*"))

        return contrasena.length >= longitudMinima && letraMayuscula
    }

    private fun notificacion() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
                // Aquí llamamos a la función para enviar el token y el ID del usuario al servidor
            enviarTokenYUsuarioAServidor(token)


            Toast.makeText(this@LogIn, token, Toast.LENGTH_SHORT).show()
        })
    }

    private fun enviarTokenYUsuarioAServidor(token: String) {
        val idUsuario = terapeutas.id

        val notification = JSONObject()

        notification.put("token", token)
        notification.put("id_usuario", idUsuario)

        val requestBody = notification.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://kbocchi.onrender.com/fcmtokens")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al crear el token", Toast.LENGTH_SHORT).show()

                }
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    val notificacionCreada = JSONObject(responseData)
                    runOnUiThread {

                        Toast.makeText(applicationContext, "Token creado exitosamente", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al crear el token", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}