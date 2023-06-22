package com.example.kbocchiv2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException

class mainChat : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var textViewChat: TextView

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
    }

    private fun setupSocket() {
        try {
            socket = IO.socket("http://<IP_DEL_SERVIDOR>:3000")
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
}
