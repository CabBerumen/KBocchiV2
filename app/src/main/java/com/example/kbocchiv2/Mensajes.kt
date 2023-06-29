package com.example.kbocchiv2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbocchiv2.databinding.ActivityMensajesBinding
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException


class Mensajes : AppCompatActivity() {

    private lateinit var socket: Socket
    private val messagesList: ArrayList<String> = ArrayList()
    private lateinit var adapter: MessageAdapter
    private lateinit var dButton: Button
    private lateinit var inputTexto: EditText
   // var messagesRecycler: RecyclerView? = null
    private lateinit var messagesRecycler:RecyclerView

     private lateinit var binding: ActivityMensajesBinding
     private lateinit var conexion: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

}