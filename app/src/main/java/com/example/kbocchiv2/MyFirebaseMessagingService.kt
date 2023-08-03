package com.example.kbocchiv2

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title ?: "Nuevo mensaje"
            val body = remoteMessage.notification?.body ?: ""
            val senderName = remoteMessage.data["nombre"] ?: ""

            mostrarNotificacion(title, body, senderName)

        }
    }
    override fun onNewToken(token: String) {

    }
    private fun mostrarNotificacion(title: String, body: String, senderName: String) {

    }
}

