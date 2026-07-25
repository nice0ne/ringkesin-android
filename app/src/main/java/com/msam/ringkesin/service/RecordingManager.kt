package com.msam.ringkesin.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class RecordingStatus {
    data object Idle : RecordingStatus()
    data object RequestingPermission : RecordingStatus()
    data object Starting : RecordingStatus()
    data class Recording(
        val partialTranscript: String = "",
        val finalTranscript: String = "",
        val elapsedSeconds: Int = 0,
    ) : RecordingStatus()
    data class Error(val message: String) : RecordingStatus()
    data object Unsupported : RecordingStatus()
}

class RecordingManager(private val appContext: Context) {

    private var recognizer: SpeechRecognizer? = null
    private var currentLanguage: String = "en-US"

    private val _status = MutableStateFlow<RecordingStatus>(RecordingStatus.Idle)
    val status: StateFlow<RecordingStatus> = _status.asStateFlow()

    private var accumulatedTranscript = ""
    private var callbackHasRecorded = false
    private var consecutiveErrors = 0
    private val maxConsecutiveErrors = 5
    private var startedAt: Long = 0L

    fun elapsedSeconds(): Int =
        if (startedAt > 0) ((System.currentTimeMillis() - startedAt) / 1000).toInt() else 0

    fun start(language: String = "en-US") {
        if (_status.value is RecordingStatus.Recording) return

        currentLanguage = language
        accumulatedTranscript = ""
        callbackHasRecorded = false
        startedAt = System.currentTimeMillis()
        _status.value = RecordingStatus.Starting

        try {
            initRecognizer()
            recognizer?.startListening(buildIntent())
        } catch (e: Exception) {
            _status.value = RecordingStatus.Error(e.message ?: "Gagal memulai")
        }
    }

    /**
     * Restart the recognizer for continuous listening.
     * Preserves accumulatedTranscript and elapsedSeconds.
     */
    private fun restartListening() {
        try {
            recognizer?.destroy()
            recognizer = null
            initRecognizer()
            recognizer?.startListening(buildIntent())
        } catch (e: Exception) {
            // If restart fails, emit error but keep accumulated text
            val current = _status.value
            if (current is RecordingStatus.Recording && accumulatedTranscript.isNotBlank()) {
                _status.value = RecordingStatus.Recording(
                    finalTranscript = accumulatedTranscript,
                    elapsedSeconds = elapsedSeconds(),
                )
            }
        }
    }

    private fun initRecognizer() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
        if (recognizer == null) {
            _status.value = RecordingStatus.Unsupported
            return
        }

        recognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                callbackHasRecorded = true
                consecutiveErrors = 0 // reset error counter on success
                val current = _status.value
                _status.value = RecordingStatus.Recording(
                    partialTranscript = "",
                    finalTranscript = accumulatedTranscript,
                    elapsedSeconds = elapsedSeconds(),
                )
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // Save current final transcript before onResults fires
                val current = _status.value
                if (current is RecordingStatus.Recording) {
                    accumulatedTranscript = current.finalTranscript
                }
            }

            override fun onError(error: Int) {
                val retryableErrors = setOf(
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                    SpeechRecognizer.ERROR_SERVER,
                    SpeechRecognizer.ERROR_AUDIO,
                )

                when {
                    error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        _status.value = RecordingStatus.RequestingPermission
                        return
                    }
                    error in retryableErrors -> {
                        consecutiveErrors++
                        if (consecutiveErrors >= maxConsecutiveErrors) {
                            _status.value = RecordingStatus.Error("Terlalu banyak error, hentikan")
                            return
                        }
                        val delay = when (error) {
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> 1500L
                            SpeechRecognizer.ERROR_SERVER -> 2000L
                            else -> 500L
                        }
                        android.os.Handler(appContext.mainLooper).postDelayed({
                            if (_status.value is RecordingStatus.Recording ||
                                _status.value is RecordingStatus.Starting) {
                                restartListening()
                            }
                        }, delay)
                    }
                    else -> {
                        // LANGUAGE_NOT_SUPPORTED, RECOGNIZER_BUSY, CLIENT, TOO_MANY_REQUESTS
                        consecutiveErrors++
                        if (consecutiveErrors >= maxConsecutiveErrors) {
                            _status.value = RecordingStatus.Error("Terlalu banyak error, hentikan")
                            return
                        }
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Bahasa tidak didukung"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Sibuk, coba lagi"
                            SpeechRecognizer.ERROR_CLIENT -> "Error client"
                            else -> "Error ($error)"
                        }
                        // Show brief status then retry — elapsed dari absolute time
                        _status.value = RecordingStatus.Error(msg)
                        android.os.Handler(appContext.mainLooper).postDelayed({
                            if (_status.value is RecordingStatus.Error) {
                                // If no one stopped us, restart — elapsed dari absolute time, kebal reset
                                _status.value = RecordingStatus.Recording(
                                    partialTranscript = "",
                                    finalTranscript = accumulatedTranscript,
                                    elapsedSeconds = elapsedSeconds(),
                                )
                                restartListening()
                            }
                        }, 1500)
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                consecutiveErrors = 0 // reset on successful result
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""

                if (text.isNotBlank()) {
                    accumulatedTranscript = if (accumulatedTranscript.isBlank()) text
                    else "$accumulatedTranscript $text"
                }

                val current = _status.value
                _status.value = RecordingStatus.Recording(
                    partialTranscript = "",
                    finalTranscript = accumulatedTranscript,
                    elapsedSeconds = elapsedSeconds(),
                )

                // ⚡ RESTART for continuous listening
                restartListening()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                val current = _status.value
                if (current is RecordingStatus.Recording) {
                    _status.value = current.copy(partialTranscript = text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
        }
    }

    fun stop() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null

        accumulatedTranscript = ""
        callbackHasRecorded = false
        startedAt = 0L
        _status.value = RecordingStatus.Idle
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        accumulatedTranscript = ""
        callbackHasRecorded = false
        startedAt = 0L
        _status.value = RecordingStatus.Idle
    }

    val isRecording: Boolean
        get() = _status.value is RecordingStatus.Recording
}
