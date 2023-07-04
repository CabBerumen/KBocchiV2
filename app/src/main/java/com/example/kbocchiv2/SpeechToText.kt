
package com.example.kbocchiv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechToText(private val context: Context) {
    private val speechRecognizer: SpeechRecognizer


    init {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("es", "MX"))
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora")

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle) {
                val voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (voiceResults != null && voiceResults.size > 0) {
                    val recognizedText = voiceResults[0]
                    (context as MainActivity).displayResult(recognizedText)
                    // Sección del mapa
                    if (recognizedText.contains("Ver mapa") || recognizedText.contains("ver mapa")) {
                        val intent = Intent(context, Maps::class.java)
                        intent.putExtra("recognizedText", recognizedText)
                        context.startActivity(intent)
                    }
                    // Sección del Chat
                    if (recognizedText.contains("ver conversaciones") || recognizedText.contains("ver conversacion") || recognizedText.contains("ver chats") || recognizedText.contains("ver chat")) {
                        val intent = Intent(context, mainChat::class.java)
                        intent.putExtra("recognizedText", recognizedText)
                        context.startActivity(intent)
                    }
                    // Sección de paciente
                    if (recognizedText.contains("ver pacientes") || recognizedText.contains("ver perfil de paciente")) {
                        val intent = Intent(context, Pacientes::class.java)
                        intent.putExtra("recognizedText", recognizedText)
                        context.startActivity(intent)
                    }
                    // Sección de expedientes
                    if (recognizedText.contains("ver expedientes") || recognizedText.contains("ver expediente")) {
                        val intent = Intent(context, Expediente::class.java)
                        intent.putExtra("recognizedText", recognizedText)
                        context.startActivity(intent)
                    }
                    //inicio
                    if (recognizedText.contains("ver inicio") || recognizedText.contains("inicio")|| recognizedText.contains("home") ) {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("recognizedText", recognizedText)
                        context.startActivity(intent)
                    }
                }
                // Iniciar nuevamente el reconocimiento de voz
                startListening()
            }

            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })

        speechRecognizer.startListening(intent)
    }

   /* fun destroy() {
        speechRecognizer.destroy()
    }*/
}


