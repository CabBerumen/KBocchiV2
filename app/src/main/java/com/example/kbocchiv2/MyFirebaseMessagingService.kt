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

    private fun mostrarNotificacion(title: String, body: String, senderName: String) {
        // Implementa aquí la lógica para mostrar la notificación en la barra de notificaciones.
        // Puedes utilizar el mismo código que se muestra en la función `mostrarNotificacion` anterior.
    }
}