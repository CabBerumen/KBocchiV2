package com.example.kbocchiv2
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

class Mensajes : AppCompatActivity() {

    var nombree : TextView? = null
    private lateinit var socket: Socket
    private lateinit var editTextMessage: EditText
    private lateinit var textViewMessages: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)
        nombree = findViewById(R.id.nameChat)
//muestra el nombre del paciente
        val intent = intent

        if (intent != null) {
            val nombre = intent.getStringExtra("nombre")

            nombree?.setText(nombre)
            Log.d("DatosPacientes", "Nombre: $nombre")
        }
//codigo para la conexion con el socket


        //seccion del boton
        editTextMessage = findViewById(R.id.editTextMessage)
        val buttonSend = findViewById<Button>(R.id.buttonSend)

        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString()
            if (message.isNotEmpty()) {
                val jsonObject = JSONObject()
                val id_usuario = intent.getStringExtra("id_usuario")
                jsonObject.put("to",id_usuario ) // Reemplaza "destinatario" con el ID del destinatario del mensaje
                jsonObject.put("contenido", message)
                socket.emit("mensajes:enviar", jsonObject)

                editTextMessage.text.clear()
            }
        }

        val options = IO.Options()
        options.forceNew = true
        options.reconnection = true

        try {
            socket = IO.socket("https://kbocchi.onrender.com/")
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.on("connected", onConnected)
        socket.on("usuario:conectado", onUsuarioConectado)
        socket.on("usuario:desconectado", onUsuarioDesconectado)
        socket.on("usuario:lista", onUsuarioLista)
        socket.on("mensajes:recibido", onMensajeRecibido)


//recibir mensajes
        socket.on(Socket.EVENT_CONNECT) {
            runOnUiThread {
                // Conexión exitosa
            }
        }.on(Socket.EVENT_DISCONNECT) {
            runOnUiThread {
                // Desconexión
            }
        }.on("mensajes:recibido", onMensajeRecibido)

        socket.connect()

    }

    private val onMensajeRecibido = Emitter.Listener { args ->
        val mensaje = args[0] as JSONObject
        val nombre = mensaje.getString("nombre")
        val contenido = mensaje.getString("contenido")
        val fecha = mensaje.getString("fecha")

        runOnUiThread {
            val formattedMessage = "$nombre ($fecha): $contenido\n"
            textViewMessages.append(formattedMessage)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()

        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.off("connected", onConnected)
        socket.off("usuario:conectado", onUsuarioConectado)
        socket.off("usuario:desconectado", onUsuarioDesconectado)
        socket.off("usuario:lista", onUsuarioLista)
        socket.off("mensajes:recibido", onMensajeRecibido)
    }

    private val onConnect = Emitter.Listener {
        // La conexión con el servidor se estableció correctamente
        // Puedes enviar tus datos aquí, por ejemplo:
        val datos = JSONObject()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString("token", null)
        val sharedPreferences2 = getSharedPreferences("DatosPerfil", Context.MODE_PRIVATE)
        val nombre = sharedPreferences2.getString("nombre", "")
        val id_usuario = sharedPreferences.getString("id", "")
        datos.put("id", id_usuario )
        datos.put("nombre", nombre)
        socket.emit("send_data", datos)
    }
    private val onDisconnect = Emitter.Listener {
        // El socket se desconectó del servidor
    }

    private val onConnected = Emitter.Listener { args ->
        // El servidor confirmó la conexión del usuario
        val message = args[0] as String
        runOnUiThread {
            // Realiza las acciones necesarias después de la conexión exitosa
        }
    }
    private val onUsuarioConectado = Emitter.Listener { args ->
        // Un usuario se conectó al servidor
        val usuario = args[0] as JSONObject
        runOnUiThread {
            // Realiza las acciones necesarias cuando un usuario se conecta
        }
    }

    private val onUsuarioDesconectado = Emitter.Listener { args ->
        // Un usuario se desconectó del servidor
        val usuario = args[0] as JSONObject
        runOnUiThread {
            // Realiza las acciones necesarias cuando un usuario se desconecta
        }
    }

    private val onUsuarioLista = Emitter.Listener { args ->
        // Se recibió la lista de usuarios conectados
        val usuarios = args[0] as JSONArray
        runOnUiThread {
            // Actualiza la lista de usuarios conectados en tu interfaz
        }
    }

   /* override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.off("connected", onConnected)
        socket.off("usuario:conectado", onUsuarioConectado)
        socket.off("usuario:desconectado", onUsuarioDesconectado)
        socket.off("usuario:lista", onUsuarioLista)
        socket.off("mensajes:recibido", onMensajeRecibido)
    }*/

}