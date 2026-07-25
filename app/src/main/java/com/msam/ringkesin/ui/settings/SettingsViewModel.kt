package com.msam.ringkesin.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.msam.ringkesin.data.remote.AiSummarizer
import com.msam.ringkesin.ui.theme.RingkesinTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProviderInfo(
    val id: String,
    val label: String,
    val defaultModel: String,
    val defaultBaseUrl: String,
)

val PROVIDERS = listOf(
    ProviderInfo("openai", "OpenAI", "gpt-4o-mini", "https://api.openai.com/v1"),
    ProviderInfo("anthropic", "Anthropic", "claude-3-haiku-20240307", "https://api.anthropic.com/v1"),
    ProviderInfo("deepseek", "DeepSeek", "deepseek-chat", "https://api.deepseek.com/v1"),
    ProviderInfo("glm", "GLM", "glm-5.2", "https://api.z.ai/api/anthropic"),
    ProviderInfo("openrouter", "OpenRouter", "openrouter/auto", "https://openrouter.ai/api/v1"),
    ProviderInfo("ollama", "Ollama", "llama3.2", "http://localhost:11434/v1"),
    ProviderInfo("custom", "Custom", "", ""),
)

data class SettingsUiState(
    val selectedTheme: RingkesinTheme = RingkesinTheme.AI_NATIVE,
    val aiProvider: String = "openai",
    val apiKey: String = "",
    val model: String = "gpt-4o-mini",
    val baseUrl: String = "https://api.openai.com/v1",
    val systemPrompt: String = "",
    val uiLanguage: String = "en",
    val backgroundRecord: Boolean = true,
    val autoSave: Boolean = true,
    val notificationOn: Boolean = true,
    val showApiKey: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: String? = null,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val savedProvider = prefs.getString("ai_provider", "openai") ?: "openai"
        _uiState.value = SettingsUiState(
            selectedTheme = RingkesinTheme.valueOf(
                prefs.getString("theme", RingkesinTheme.AI_NATIVE.name) ?: RingkesinTheme.AI_NATIVE.name
            ),
            aiProvider = savedProvider,
            apiKey = prefs.getString("api_key", "") ?: "",
            model = prefs.getString("model", getDefaultModel(savedProvider)) ?: getDefaultModel(savedProvider),
            baseUrl = prefs.getString("base_url", getDefaultBaseUrl(savedProvider))
                ?: getDefaultBaseUrl(savedProvider),
            systemPrompt = prefs.getString("system_prompt", "") ?: "",
            uiLanguage = prefs.getString("ui_lang", "en") ?: "en",
            backgroundRecord = prefs.getBoolean("background_record", true),
            autoSave = prefs.getBoolean("auto_save", true),
            notificationOn = prefs.getBoolean("notification_on", true),
        )
    }

    private fun saveSetting(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
            }
            apply()
        }
    }

    private fun getDefaultModel(providerId: String): String =
        PROVIDERS.find { it.id == providerId }?.defaultModel ?: ""

    private fun getDefaultBaseUrl(providerId: String): String =
        PROVIDERS.find { it.id == providerId }?.defaultBaseUrl ?: ""

    fun setTheme(theme: RingkesinTheme) {
        _uiState.value = _uiState.value.copy(selectedTheme = theme)
        saveSetting("theme", theme.name)
    }

    fun setAiProvider(provider: String) {
        val defaultModel = getDefaultModel(provider)
        val defaultBaseUrl = getDefaultBaseUrl(provider)
        _uiState.value = _uiState.value.copy(
            aiProvider = provider,
            model = defaultModel,
            baseUrl = defaultBaseUrl,
        )
        saveSetting("ai_provider", provider)
        saveSetting("model", defaultModel)
        saveSetting("base_url", defaultBaseUrl)
    }

    fun setApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
        saveSetting("api_key", key)
    }

    fun setModel(model: String) {
        _uiState.value = _uiState.value.copy(model = model)
        saveSetting("model", model)
    }

    fun setBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(baseUrl = url)
        saveSetting("base_url", url)
    }

    fun setSystemPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(systemPrompt = prompt)
        saveSetting("system_prompt", prompt)
    }

    fun setUiLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(uiLanguage = lang)
        saveSetting("ui_lang", lang)
    }

    fun toggleBackgroundRecord() {
        val v = !_uiState.value.backgroundRecord
        _uiState.value = _uiState.value.copy(backgroundRecord = v)
        saveSetting("background_record", v)
    }

    fun toggleAutoSave() {
        val v = !_uiState.value.autoSave
        _uiState.value = _uiState.value.copy(autoSave = v)
        saveSetting("auto_save", v)
    }

    fun toggleNotification() {
        val v = !_uiState.value.notificationOn
        _uiState.value = _uiState.value.copy(notificationOn = v)
        saveSetting("notification_on", v)
    }

    fun toggleShowApiKey() {
        _uiState.value = _uiState.value.copy(showApiKey = !_uiState.value.showApiKey)
    }

    fun testApi() {
        val s = _uiState.value
        if (s.apiKey.isBlank()) {
            _uiState.value = s.copy(testResult = "❌ API Key kosong")
            return
        }
        _uiState.value = s.copy(isTesting = true, testResult = null)
        viewModelScope.launch {
            val summarizer = AiSummarizer(getApplication())
            val config = summarizer.loadConfig()
            val result = summarizer.summarize(
                "Halo, ini adalah tes koneksi API. Mohon balas dengan 'OK' jika terhubung.",
                config,
            )
            result.onSuccess { text ->
                _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    testResult = "✅ Berhasil: ${text.take(80)}${if (text.length > 80) "…" else ""}",
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    testResult = "❌ Gagal: ${error.message?.take(120) ?: "Unknown error"}",
                )
            }
        }
    }

    fun clearTestResult() {
        _uiState.value = _uiState.value.copy(testResult = null)
    }
}
