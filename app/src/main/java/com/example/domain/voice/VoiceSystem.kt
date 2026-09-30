package com.example.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

interface SpeechRecognizer {
    val isListening: StateFlow<Boolean>
    val recognizedText: StateFlow<String>
    val speechError: StateFlow<String?>
    fun startListening()
    fun stopListening()
    fun cancel()
}

interface SpeechSynthesizer {
    val isSpeaking: StateFlow<Boolean>
    fun speak(text: String)
    fun stop()
    fun release()
}

class HarmesSpeechRecognizer(private val context: Context) : SpeechRecognizer {
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedText = MutableStateFlow("")
    override val recognizedText: StateFlow<String> = _recognizedText

    private val _speechError = MutableStateFlow<String?>(null)
    override val speechError: StateFlow<String?> = _speechError

    private var recognizer: AndroidSpeechRecognizer? = null

    init {
        if (AndroidSpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = AndroidSpeechRecognizer.createSpeechRecognizer(context)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _speechError.value = null
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }
                override fun onError(error: Int) {
                    _isListening.value = false
                    _speechError.value = "Voice recognition code $error"
                }
                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _recognizedText.value = matches[0]
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _recognizedText.value = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    override fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        _recognizedText.value = ""
        _speechError.value = null
        try {
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            _speechError.value = e.message
            _isListening.value = false
        }
    }

    override fun stopListening() {
        recognizer?.stopListening()
        _isListening.value = false
    }

    override fun cancel() {
        recognizer?.cancel()
        _isListening.value = false
    }
}

class HarmesSpeechSynthesizer(private val context: Context) : SpeechSynthesizer {
    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(0.95f) // Calm, analytical Harmes pitch
                tts?.setSpeechRate(1.0f)
                isInitialized = true
            }
        }
    }

    override fun speak(text: String) {
        if (!isInitialized) return
        val cleanText = text.replace(Regex("[#*_`\\[\\]]"), "")
        _isSpeaking.value = true
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "harmes_utterance")
    }

    override fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}
