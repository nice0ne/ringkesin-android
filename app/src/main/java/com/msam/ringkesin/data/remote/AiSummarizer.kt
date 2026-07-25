package com.msam.ringkesin.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiSummarizer(private val appContext: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val prefs = appContext.getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)

    data class ProviderConfig(
        val providerId: String,
        val apiKey: String,
        val model: String,
        val baseUrl: String,
        val systemPrompt: String,
    )

    fun loadConfig(): ProviderConfig {
        return ProviderConfig(
            providerId = prefs.getString("ai_provider", "openai") ?: "openai",
            apiKey = prefs.getString("api_key", "") ?: "",
            model = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini",
            baseUrl = prefs.getString("base_url", "https://api.openai.com/v1")
                ?: "https://api.openai.com/v1",
            systemPrompt = prefs.getString("system_prompt", "") ?: "",
        )
    }

    suspend fun summarize(
        transcript: String,
        config: ProviderConfig? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val cfg = config ?: loadConfig()

        if (cfg.apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException(
                "API Key not filled in Settings"
            ))
        }

        return@withContext try {
            val prompt = cfg.systemPrompt.ifBlank {
                "Anda adalah asisten yang merangkum percakapan. " +
                "Ringkaslah teks berikut secara jelas dan terstruktur dalam bahasa yang sama dengan teks. " +
                "Gunakan poin-poin jika memungkinkan."
            }

            val result = when (cfg.providerId) {
                "anthropic", "glm" -> callAnthropic(cfg, prompt, transcript)
                else -> callOpenAICompatible(cfg, prompt, transcript)
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callOpenAICompatible(
        cfg: ProviderConfig,
        systemPrompt: String,
        transcript: String
    ): String {
        val url = "${cfg.baseUrl.trimEnd('/')}/chat/completions"

        val body = OpenAIRequest(
            model = cfg.model,
            messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = "Teks:\n\n$transcript")
            ),
            temperature = 0.3,
            maxTokens = 2048,
        )

        val jsonBody = gson.toJson(body)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${cfg.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful) {
            throw Exception(
                "API error ${response.code}: ${responseBody?.take(200) ?: "Unknown"}"
            )
        }

        val chatResponse = gson.fromJson(responseBody, OpenAIResponse::class.java)
        return chatResponse.choices?.firstOrNull()?.message?.content
            ?: throw Exception("Response tidak valid dari API")
    }

    private fun callAnthropic(
        cfg: ProviderConfig,
        systemPrompt: String,
        transcript: String
    ): String {
        // z.ai uses /v1/messages + Authorization: Bearer;
        // real Anthropic uses /messages + x-api-key.
        val isZai = cfg.baseUrl.contains("z.ai", ignoreCase = true)
        val base = cfg.baseUrl.replace(Regex("/v\\d+/?\$"), "").trimEnd('/')
        val url = "$base/v1/messages"

        val body = AnthropicRequest(
            model = cfg.model,
            system = systemPrompt,
            messages = listOf(
                AnthropicMessage(role = "user", content = "Teks:\n\n$transcript")
            ),
            maxTokens = 2048,
            temperature = 0.3,
        )

        val jsonBody = gson.toJson(body)
        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("anthropic-version", "2023-06-01")

        if (isZai) {
            requestBuilder.addHeader("Authorization", "Bearer ${cfg.apiKey}")
        } else {
            requestBuilder.addHeader("x-api-key", cfg.apiKey)
        }

        val request = requestBuilder.post(jsonBody.toRequestBody(jsonMediaType)).build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful) {
            throw Exception(
                "API error ${response.code}: ${responseBody?.take(200) ?: "Unknown"}"
            )
        }

        val anthroResponse = gson.fromJson(responseBody, AnthropicResponse::class.java)
        val text = anthroResponse.content?.firstOrNull()?.text?.trim()
        if (text.isNullOrBlank()) {
            throw Exception(
                "Response tidak valid dari API: ${responseBody?.take(200) ?: "empty"}"
            )
        }
        return text
    }

    // ── OpenAI-compatible request/response models ──
    data class OpenAIRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double,
        @SerializedName("max_tokens") val maxTokens: Int,
    )

    data class Message(
        val role: String,
        val content: String,
    )

    data class OpenAIResponse(
        val choices: List<Choice>?,
    )

    data class Choice(
        val message: Message,
    )

    // ── Anthropic request/response models ──
    data class AnthropicRequest(
        val model: String,
        val system: String,
        val messages: List<AnthropicMessage>,
        @SerializedName("max_tokens") val maxTokens: Int,
        val temperature: Double,
    )

    data class AnthropicMessage(
        val role: String,
        val content: String,
    )

    data class AnthropicResponse(
        val content: List<AnthropicContent>?,
    )

    data class AnthropicContent(
        val text: String?,
    )
}
