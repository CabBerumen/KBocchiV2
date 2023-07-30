package com.example.kbocchiv2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbocchiv2.databinding.ActivityMensajesBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException


class Mensajes : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var socket: Socket
    private val messagesList: ArrayList<String> = ArrayList()
    private lateinit var adapter: MessageAdapter
    private lateinit var dButton: Button
    private lateinit var inputTexto: EditText
   // var messagesRecycler: RecyclerView? = null
    private lateinit var messagesRecycler:RecyclerView

     private lateinit var binding: ActivityMensajesBinding
     private lateinit var conexion: TextView

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val toogle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_close,
            R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toogle)
        toogle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val navHeaderView = navigationView.getHeaderView(0)
        val imageView = navHeaderView.findViewById<CircleImageView>(R.id.navheaderFoto)
        val usertext = navHeaderView.findViewById<TextView>(R.id.user_name)
        val emailtext = navHeaderView.findViewById<TextView>(R.id.user_email)

        val sharedPreferences = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
        val fototerapeuta = sharedPreferences.getString("foto_perfil", "")
        val email = sharedPreferences.getString("email", "")
        val nombre = sharedPreferences.getString("nombre", "")

        val storage = Firebase.storage
        val storeImageUrl = "gs://kbocchi-1254b.appspot.com/"

        usertext.text = nombre
        emailtext.text = email
        val fotoNav = fototerapeuta

        val imagePath = fotoNav ?: ""

        val storageReference = if(!imagePath.isNullOrEmpty()){
            storage.reference.child(imagePath)
        }else{
            null
        }
        if(!imagePath.isNullOrEmpty()) {
            storageReference?.downloadUrl?.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(imageView, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                        }
                        override fun onError(e: Exception?) {
                        }
                    })
            }?.addOnFailureListener { exception ->
            }
        } else {
            imageView.visibility = View.GONE
            imageView.setImageResource(R.drawable.perfil)
        }


        dButton = binding.sendButton
        messagesRecycler = binding.messagesRecyclerView
        messagesRecycler.layoutManager = LinearLayoutManager(this)
        inputTexto =binding.inputText
        conexion = binding.conexion


        //muestra informacion del paciente
        // Muestra el nombre del paciente
        val intent = intent
        if (intent != null) {
            val nombre = intent.getStringExtra("nombre")
            conexion?.text = nombre
            Log.d("DatosPacientes", "Chat de: $nombre")
        }

        // Inicializar el adaptador y asignarlo al RecyclerView
         adapter = MessageAdapter(messagesList)
        messagesRecycler.adapter = adapter

        try {
            // Configurar la conexión del socket
            val options = IO.Options()
            options.forceNew = true
            socket = IO.socket("https://kbocchi.onrender.com/", options)
        } catch (e: URISyntaxException) {
            Log.e("SocketIO", "Error: $e")
        }

        // Conectar al servidor de Socket.IO
        socket.connect()

        // Manejar eventos de conexión y desconexión
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)

        // Manejar evento de recibir mensaje
        socket.on("mensajes:recibido", onNewMessage)

        // Enviar mensaje cuando se presiona el botón

        dButton.setOnClickListener {

            val message = inputTexto.text.toString().trim()

            if (message.isNotEmpty()) {
                sendMessage(message)
                inputTexto.text.clear()
            }
        }

        //creacion del sharePreference para la id del terapeuta

    }
    private val onConnect = Emitter.Listener {

        val datos = JSONObject()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("idusuario", null)
        val sharedPreferences2 = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
        val nombre = sharedPreferences2.getString("nombre", "")
        val idusuario = sharedPreferences.getString("id_usuario","")
        val id =sharedPreferences2.getString("id", "")


        datos.put("id",id)
        datos.put("id_usuario", token)
        datos.put("nombre", nombre)

        socket.emit("send_data", datos)
        Log.e("id paciente ", "ID DEL TERAPEUTA: $token", )
        Log.e("id paciente ", "NOMBRE TERAPEUTA: $nombre", )
        Log.e("id paciente ", "id : $id", )

        runOnUiThread {

            Log.d("SocketIO", "Conectado al servidor")
        }
    }

    private val onDisconnect = Emitter.Listener {

        runOnUiThread {
            Log.d("SocketIO", "Desconectado del servidor")

        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            val senderName = data.getString("nombre")
            val messageContent = data.getString("contenido")
            val formattedMessage = "$senderName: $messageContent"
            addMessage(formattedMessage)

        }
    }

    private fun sendMessage(message: String, ) {

      //  val data = JSONObject().apply {
        val data =  JSONObject()
            val intent = intent
            if (intent != null) {
                val idPacienteUsuario = intent.getStringExtra("id_usuario")
               data.put("to", idPacienteUsuario) //  ID del destinatario
               data.put("contenido", message)
                Log.e("id paciente ", "ID PACIENTE: $idPacienteUsuario", )
                Log.e("contenido ", "mensaje: $message", )
                socket.emit("mensajes:enviar", data)
        }

        addMessage("Yo: $message")
    }

   // @SuppressLint("NotifyDataSetChanged")
    private fun addMessage(message: String) {
        messagesList.add(message)
        adapter.notifyDataSetChanged()
        messagesRecycler.scrollToPosition(messagesList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.off("mensajes:recibido", onNewMessage)
        Log.d("destructor", "Desconectado del servidor")
    }

    //RecyclerView
    class MessageAdapter(private val messages: ArrayList<String>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val messageText: TextView = itemView.findViewById(R.id.nombreMensaje)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.mensajes_holder, parent, false)
            return MessageViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.messageText.text = message
        }

        override fun getItemCount(): Int {
            return messages.size
        }

    }

    private fun mostrarNotificacion(title: String, message: String, senderName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mi_canal_de_notificaciones"
        val notificationId = 0 // Usa un ID único para cada notificación

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nombre del canal",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, Mensajes::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item0 -> {
                //Ir a la actividad principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item1 -> {
                //Ir a la agenda
                val intent = Intent(this, MostrarCitas::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_citas -> {
                //Ir a agendar citas
                val intent = Intent(this, AgendarCita::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item2 -> {
                //Ir al chat
                val intent = Intent(this, mainChat::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item3 -> {
                //Ir al maps
                val intent = Intent(this, Maps::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_item4 -> {
                //Ir al expediente
                val intent = Intent(this, Expediente::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_pacientes -> {
                //Ir a ver pacientes
                val intent = Intent(this, Pacientes::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_perfil -> {
                //Ir a ver perfil
                val intent = Intent(this, Perfil::class.java)
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