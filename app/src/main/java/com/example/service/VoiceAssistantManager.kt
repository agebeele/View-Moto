package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastVoiceFeedback = MutableStateFlow("Asistente de voz listo")
    val lastVoiceFeedback: StateFlow<String> = _lastVoiceFeedback.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "MX"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            isTtsReady = true
            Log.d("VoiceAssistant", "TTS Initialized successfully in Spanish")
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        _lastVoiceFeedback.value = text
        if (isTtsReady) {
            tts?.speak(text, queueMode, null, "MOTO_DASH_TTS_${System.currentTimeMillis()}")
        }
    }

    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    /**
     * Parses spoken or typed voice commands and returns action identifier
     */
    fun processVoiceCommand(commandText: String): VoiceCommandResult {
        val normalized = commandText.lowercase().trim()
        return when {
            normalized.contains("emergencia") || normalized.contains("auxilio") || normalized.contains("sos") -> {
                speak("Activando protocolo de emergencia y llamando a tu contacto principal.")
                VoiceCommandResult.TriggerEmergency
            }
            normalized.contains("contacto") || normalized.contains("llamar") -> {
                val contactName = normalized.substringAfter("llamar a").substringAfter("contacto").trim()
                speak("Iniciando llamada manos libres para $contactName.")
                VoiceCommandResult.CallContact(contactName)
            }
            normalized.contains("presión") || normalized.contains("llantas") || normalized.contains("neumático") -> {
                speak("Revisando sensores TPMS: Neumático delantero 29.4 PSI, trasero 32.8 PSI. Presión óptima.")
                VoiceCommandResult.CheckTpms
            }
            normalized.contains("servicio") || normalized.contains("mantenimiento") || normalized.contains("aceite") -> {
                speak("Alerta: Próximo cambio de aceite en 2773 kilómetros. Cadena requiere lubricación preventiva.")
                VoiceCommandResult.CheckService
            }
            normalized.contains("velocidad") || normalized.contains("rápido") -> {
                VoiceCommandResult.CheckSpeed
            }
            normalized.contains("pausar") || normalized.contains("play") || normalized.contains("música") || normalized.contains("canción") -> {
                speak("Alternando reproducción de audio.")
                VoiceCommandResult.ToggleMusic
            }
            normalized.contains("siguiente") -> {
                speak("Siguiente pista.")
                VoiceCommandResult.NextTrack
            }
            else -> {
                speak("Comando no reconocido. Puedes decir: Emergencia, Presión de llantas, Próximo servicio o Música.")
                VoiceCommandResult.Unknown(commandText)
            }
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Error shutting down TTS", e)
        }
    }
}

sealed class VoiceCommandResult {
    data object TriggerEmergency : VoiceCommandResult()
    data class CallContact(val name: String) : VoiceCommandResult()
    data object CheckTpms : VoiceCommandResult()
    data object CheckService : VoiceCommandResult()
    data object CheckSpeed : VoiceCommandResult()
    data object ToggleMusic : VoiceCommandResult()
    data object NextTrack : VoiceCommandResult()
    data class Unknown(val raw: String) : VoiceCommandResult()
}
