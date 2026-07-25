package com.msam.ringkesin.ui.record

import android.Manifest
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.msam.ringkesin.RingkesinApp
import com.msam.ringkesin.data.local.entity.SessionEntity
import com.msam.ringkesin.data.remote.AiSummarizer
import com.msam.ringkesin.service.RecordingManager
import com.msam.ringkesin.service.RecordingService
import com.msam.ringkesin.service.RecordingStatus
import com.msam.ringkesin.ui.localization.S
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecordUiState(
    val isIdle: Boolean = true,
    val isRecording: Boolean = false,
    val isPermissionGranted: Boolean? = null,
    val transcript: String = "",
    val partialText: String = "",
    val timerSeconds: Int = 0,
    val selectedLanguage: String = "en-US",
    val uiLanguage: String = "en",
    val statusText: String = "Ketuk untuk mulai merekam",
    val errorMessage: String? = null,
    val isSummarizing: Boolean = false,
    val summaryResult: String? = null,
    val saveConfirmation: String? = null,
)

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RingkesinApp
    private val manager: RecordingManager = app.recordingManager
    private val summarizer = AiSummarizer(application)
    private val database = app.database
    private val sessionDao = database.sessionDao()

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var lastSessionId: Long? = null

    private fun currentUiLang(): String =
        getApplication<Application>()
            .getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
            .getString("ui_lang", "en") ?: "en"

    fun reloadUiLanguage() {
        val lang = currentUiLang()
        _uiState.value = _uiState.value.copy(uiLanguage = lang).let {
            // Refresh statusText juga sesuai bahasa baru
            val cur = _uiState.value
            when {
                cur.isRecording -> it.copy(statusText = S.listening(lang))
                cur.isIdle && cur.transcript.isNotBlank() -> it.copy(statusText = S.recordingComplete(lang))
                cur.isIdle -> it.copy(statusText = S.tapToRecord(lang))
                else -> it
            }
        }
    }

    init {
        // Baca setting bahasa UI (tampilan), bukan bahasa speech
        val prefs = getApplication<Application>()
            .getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
        val uiLang = prefs.getString("ui_lang", "en") ?: "en"
        _uiState.value = _uiState.value.copy(uiLanguage = uiLang)
        if (!isSpeechRecognizerAvailable()) {
            _uiState.value = _uiState.value.copy(
                statusText = "SpeechRecognizer tidak didukung di perangkat ini",
            )
        }

        // Observe manager status
        viewModelScope.launch {
            manager.status.collect { status ->
                val lang = currentUiLang()
                when (status) {
                    is RecordingStatus.Idle -> {
                        val lastTranscript = _uiState.value.transcript
                        val lastTimer = _uiState.value.timerSeconds
                        _uiState.value = _uiState.value.copy(
                            isIdle = true,
                            isRecording = false,
                            statusText = if (lastTranscript.isNotBlank())
                                S.recordingComplete(lang)
                            else S.tapToRecord(lang),
                            errorMessage = null,
                            partialText = "",
                            timerSeconds = lastTimer,
                        )
                        // Auto-save if enabled and has transcript
                        if (lastTranscript.isNotBlank() && isAutoSaveEnabled()) {
                            saveTranscriptInternal(lastTranscript, lastTimer)
                        }
                    }
                    is RecordingStatus.RequestingPermission -> {
                        _uiState.value = _uiState.value.copy(
                            isPermissionGranted = false,
                            statusText = S.permissionRequired(lang),
                        )
                    }
                    is RecordingStatus.Starting -> {
                        _uiState.value = _uiState.value.copy(
                            isIdle = false,
                            isRecording = false,
                            statusText = S.preparing(lang),
                        )
                    }
                    is RecordingStatus.Recording -> {
                        // elapsedSeconds dari absolute time di manager — kebal restart/reset
                        _uiState.value = _uiState.value.copy(
                            isIdle = false,
                            isRecording = true,
                            transcript = status.finalTranscript,
                            partialText = status.partialTranscript,
                            timerSeconds = status.elapsedSeconds,
                            statusText = S.listening(lang),
                            errorMessage = null,
                        )
                    }
                    is RecordingStatus.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isIdle = true,
                            isRecording = false,
                            statusText = status.message,
                            errorMessage = status.message,
                        )
                    }
                    is RecordingStatus.Unsupported -> {
                        _uiState.value = _uiState.value.copy(
                            statusText = "SpeechRecognizer tidak didukung di perangkat ini",
                        )
                    }
                }
            }
        }

        // Check if a session was restored from History
        checkRestoredData()
    }

    fun checkRestoredData() {
        val prefs = getApplication<Application>()
            .getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
        val restored = prefs.getString("restored_transcript", "") ?: ""
        if (restored.isNotBlank()) {
            val summary = prefs.getString("current_summary", "") ?: ""
            prefs.edit().remove("restored_transcript").apply()
            _uiState.value = _uiState.value.copy(
                transcript = restored,
                summaryResult = summary.ifBlank { null },
                timerSeconds = 0,
                statusText = "Dimuat dari riwayat",
                saveConfirmation = "✅ Dimuat dari riwayat",
            )
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(saveConfirmation = null)
            }
        }
    }

    fun checkPermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = ContextCompat.checkSelfPermission(
            getApplication(), permission
        ) == PackageManager.PERMISSION_GRANTED
        _uiState.value = _uiState.value.copy(isPermissionGranted = granted)
        return granted
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isPermissionGranted = granted)
        if (granted) {
            startRecordingInternal()
        } else {
            _uiState.value = _uiState.value.copy(
                statusText = "Izin mikrofon ditolak"
            )
        }
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) {
            stopRecording()
        } else {
            if (!checkPermission()) {
                _uiState.value = _uiState.value.copy(
                    statusText = "Membutuhkan izin mikrofon"
                )
                return
            }
            startRecordingInternal()
        }
    }

    private fun startRecordingInternal() {
        // Hanya mulai foreground service jika background record aktif
        if (isBackgroundRecordEnabled()) {
            val showNotif = getApplication<Application>()
                .getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
                .getBoolean("notification_on", true)
            RecordingService.start(getApplication(), showNotif)
        }
        manager.start(_uiState.value.selectedLanguage)
        // Clear previous summary when starting new recording
        _uiState.value = _uiState.value.copy(
            summaryResult = null,
            saveConfirmation = null,
        )
    }

    private fun stopRecording() {
        manager.stop()
        RecordingService.stop(getApplication())
    }

    /**
     * Called every 1s from composable LaunchedEffect.
     * Membaca elapsedSeconds dari absolute time di manager — kebal reset.
     */
    fun tick() {
        _uiState.value = _uiState.value.copy(timerSeconds = manager.elapsedSeconds())
    }

    private fun isSpeechRecognizerAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(getApplication())
    }

    private fun isAutoSaveEnabled(): Boolean {
        val prefs = getApplication<Application>()
            .getSharedPreferences("ringkesin_settings", Application.MODE_PRIVATE)
        return prefs.getBoolean("auto_save", true)
    }

    private fun isBackgroundRecordEnabled(): Boolean {
        val prefs = getApplication<Application>()
            .getSharedPreferences("ringkesin_settings", Application.MODE_PRIVATE)
        return prefs.getBoolean("background_record", true)
    }

    // ── Save to Room DB ──
    fun saveTranscript() {
        val text = _uiState.value.transcript
        if (text.isBlank()) return
        saveTranscriptInternal(text, _uiState.value.timerSeconds)
    }

    private fun saveTranscriptInternal(text: String, durationSec: Int) {
        viewModelScope.launch {
            try {
                val session = SessionEntity(
                    transcript = text,
                    summary = "",
                    durationSeconds = durationSec,
                    language = _uiState.value.selectedLanguage,
                    createdAt = System.currentTimeMillis(),
                )
                lastSessionId = sessionDao.insert(session)
                _uiState.value = _uiState.value.copy(
                    saveConfirmation = "✅ Tersimpan"
                )
                // Clear confirmation after 2s
                delay(2000)
                _uiState.value = _uiState.value.copy(saveConfirmation = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveConfirmation = "❌ Gagal menyimpan: ${e.message}"
                )
            }
        }
    }

    // ── Copy to Clipboard ──
    fun copyToClipboard() {
        val text = _uiState.value.transcript
        if (text.isBlank()) return

        val clipboard = getApplication<Application>()
            .getSystemService(Application.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ringkesin Transcript", text)
        clipboard.setPrimaryClip(clip)

        _uiState.value = _uiState.value.copy(
            saveConfirmation = "📋 Disalin"
        )
        viewModelScope.launch {
            delay(2000)
            _uiState.value = _uiState.value.copy(saveConfirmation = null)
        }
    }

    // ── Summarize with AI ──
    fun summarizeText() {
        val text = _uiState.value.transcript
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(
            isSummarizing = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            val config = summarizer.loadConfig()

            // Validasi API key
            if (config.apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSummarizing = false,
                    statusText = S.apiKeyMissingInSettings(currentUiLang()),
                    errorMessage = "API Key dibutuhkan",
                )
                return@launch
            }

            val result = summarizer.summarize(text, config)

            result.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(
                    isSummarizing = false,
                    summaryResult = summary,
                    statusText = S.summaryComplete(currentUiLang()),
                )
                // Save to prefs for Summary tab
                com.msam.ringkesin.ui.summary.SummaryViewModel.saveToPrefs(
                    getApplication(), summary, text
                )
                // Auto-save summary to Room
                try {
                    val session = SessionEntity(
                        transcript = text,
                        summary = summary,
                        durationSeconds = _uiState.value.timerSeconds,
                        language = _uiState.value.selectedLanguage,
                        createdAt = System.currentTimeMillis(),
                        isSummarized = true,
                    )
                    sessionDao.insert(session)
                    _uiState.value = _uiState.value.copy(
                        saveConfirmation = S.summarySaved(currentUiLang())
                    )
                } catch (_: Exception) {}
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSummarizing = false,
                    errorMessage = error.message ?: S.failedToSummarize(currentUiLang()),
                    statusText = "${S.failedToSummarize(currentUiLang())}: ${error.message}",
                )
            }
        }
    }

    // ── Language ──
    fun setLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = lang)
    }

    fun updateTranscript(text: String) {
        _uiState.value = _uiState.value.copy(transcript = text, partialText = "")
    }

    fun clearTranscript() {
        _uiState.value = _uiState.value.copy(
            transcript = "",
            partialText = "",
            timerSeconds = 0,
            statusText = "Ketuk untuk mulai merekam",
            errorMessage = null,
            summaryResult = null,
            saveConfirmation = null,
        )
    }

    fun clearConfirmation() {
        _uiState.value = _uiState.value.copy(saveConfirmation = null)
    }

    fun clearSummary() {
        _uiState.value = _uiState.value.copy(summaryResult = null)
    }

    override fun onCleared() {
        super.onCleared()
        manager.destroy()
    }
}
