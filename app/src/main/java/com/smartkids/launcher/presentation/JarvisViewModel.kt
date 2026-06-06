package com.smartkids.launcher.presentation

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartkids.launcher.service.JarvisManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isFromJarvis: Boolean
)

@HiltViewModel
class JarvisViewModel @Inject constructor(
    application: Application,
    private val jarvisManager: JarvisManager
) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        // Jarvis greets the child when the screen first opens
        addJarvisMessage("Hi! I am Jarvis, your smart helper 😊 You can type or tap the microphone to talk to me!")
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isThinking.value) return

        addUserMessage(text)
        _isThinking.value = true

        viewModelScope.launch {
            jarvisManager.sendMessage(text)
                .catch { _ ->
                    addJarvisMessage("Sorry, something went wrong. Please try again.")
                    _isThinking.value = false
                }
                .collect { response ->
                    addJarvisMessage(response)
                    _isThinking.value = false
                }
        }
    }

    fun startListening() {
        val context = getApplication<Application>()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            addJarvisMessage("Voice input is not available on this device. Please type instead.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }
            override fun onResults(results: Bundle?) {
                val words = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = words?.firstOrNull()
                _isListening.value = false
                if (!spoken.isNullOrBlank()) sendMessage(spoken)
            }
            override fun onError(error: Int) {
                _isListening.value = false
                addJarvisMessage("I could not hear you clearly. Please try again!")
            }
            override fun onEndOfSpeech()                            { _isListening.value = false }
            override fun onBeginningOfSpeech()                      {}
            override fun onRmsChanged(rmsdB: Float)                 {}
            override fun onBufferReceived(buffer: ByteArray?)       {}
            override fun onPartialResults(partialResults: Bundle?)  {}
            override fun onEvent(eventType: Int, params: Bundle?)   {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    private fun addUserMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(text, isFromJarvis = false)
    }

    private fun addJarvisMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(text, isFromJarvis = true)
    }

    override fun onCleared() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onCleared()
    }
}