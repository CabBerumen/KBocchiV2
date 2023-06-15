package com.example.kbocchiv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.kbocchiv2.Interfaces.ApiService
import com.example.kbocchiv2.Request.LoginRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    private lateinit var mSignInButtonGoogle: SignInButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        editusuario = findViewById(R.id.user);
        editpass = findViewById(R.id.pass);
        botonlogin = findViewById(R.id.login)
        mSignInButtonGoogle = findViewById(R.id.btngoogle)

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mSignInButtonGoogle.setOnClickListener {
            signIn()
        }

        botonlogin.setOnClickListener {
            val email = editusuario.text.toString().trim()
            val contrasena = editpass.text.toString().trim()

            val loggin = HttpLoggingInterceptor()
            loggin.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = OkHttpClient.Builder()
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
                        val tokenInter = response.body()!!.token

                        val inicio = Intent(this@LogIn, MainActivity::class.java)
                        inicio.putExtra("token", tokenInter)
                        startActivity(inicio)
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

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    irHome()
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LogIn, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            irHome()
        }
    }
    private fun irHome() {
        val intent = Intent(this@LogIn, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}