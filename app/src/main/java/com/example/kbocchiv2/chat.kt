package com.example.kbocchiv2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.kbocchiv2.databinding.ActivityChatBinding
import io.socket.client.IO
import io.socket.client.Socket

import java.net.URISyntaxException
import javax.net.ssl.SSLSessionBindingEvent


class chat : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try{
            socket = IO.socket(SOCKET_URL)
            socket.connect()
            initMain()

        }catch (e: URISyntaxException){
            e.printStackTrace()

        }
    }

    private fun initMain() {
        socket.on(CHAT_KEYS.NEW_MESSAGE) { args ->
            Log.d("OnNewMessageDebug", "${args[0]}")
        }
        binding.sendHello.setOnClickListener{
        socket.emit(CHAT_KEYS.NEW_MESSAGE, "hola")
        }
    }
    private object CHAT_KEYS{
    const val NEW_MESSAGE = "new_message"
}

    companion object {
       private const val SOCKET_URL = ""
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::socket.isInitialized){
            socket.disconnect()
            socket.off(CHAT_KEYS.NEW_MESSAGE)
        }
    }

}