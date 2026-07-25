package com.msam.ringkesin.ui.summary

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.msam.ringkesin.data.remote.AiSummarizer
import com.msam.ringkesin.ui.localization.S
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val text: String = "",
    val transcript: String = "",
    val uiLanguage: String = "en",
    val isCopied: Boolean = false,
    val isEmpty: Boolean = true,
)

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        val savedLang = prefs.getString("ui_lang", "en") ?: "en"
        _uiState.value = _uiState.value.copy(uiLanguage = savedLang)
        loadSummary()
    }

    fun reloadUiLanguage() {
        val lang = prefs.getString("ui_lang", "en") ?: "en"
        _uiState.value = _uiState.value.copy(uiLanguage = lang)
    }

    fun loadSummary() {
        val text = prefs.getString("current_summary", "") ?: ""
        val transcript = prefs.getString("current_transcript", "") ?: ""
        _uiState.value = SummaryUiState(
            text = text,
            transcript = transcript,
            isEmpty = text.isBlank(),
        )
    }

    fun copyText() {
        val text = _uiState.value.text
        if (text.isBlank()) return

        val clipboard = getApplication<Application>()
            .getSystemService(Application.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ringkesin Summary", text)
        clipboard.setPrimaryClip(clip)

        _uiState.value = _uiState.value.copy(isCopied = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(isCopied = false)
        }
    }

    fun clearSummary() {
        prefs.edit()
            .remove("current_summary")
            .remove("current_transcript")
            .apply()
        _uiState.value = SummaryUiState()
    }

    companion object {
        fun saveToPrefs(context: Context, summary: String, transcript: String) {
            context.getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
                .edit()
                .putString("current_summary", summary)
                .putString("current_transcript", transcript)
                .apply()
        }
    }
}
