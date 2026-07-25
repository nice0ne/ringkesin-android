# API Integration Guide

## Overview

Ringkesin supports multiple AI providers through a unified `AiSummarizer` interface. This document covers integration details, authentication, request/response formats, and troubleshooting for each provider.

---

## Supported Providers

| Provider | Models | API Format | Auth Method |
|----------|--------|------------|-------------|
| **OpenAI** | GPT-4o, GPT-4o-mini, GPT-3.5-turbo | OpenAI-compatible | Bearer token |
| **Anthropic** | Claude Sonnet 4, Claude Opus 4 | Anthropic Messages API | x-api-key header |
| **Google** | Gemini 1.5 Pro, Gemini 1.5 Flash | Google AI Studio | API key in URL |
| **xAI** | Grok Beta | OpenAI-compatible | Bearer token |
| **Zhipu (GLM)** | GLM-5.2 | Anthropic-compatible (reverse proxy) | Bearer token |
| **Custom** | Any | OpenAI or Anthropic | User-defined |

---

## General Configuration

### Settings Storage

```kotlin
// SharedPreferences keys
"ai_provider"   → "openai" | "anthropic" | "google" | "xai" | "custom"
"api_key"       → String (required)
"model"         → String (provider-specific)
"base_url"      → String (auto-filled, editable)
"system_prompt" → String (optional)
```

### ProviderConfig Data Class

```kotlin
data class ProviderConfig(
    val provider: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String,
    val systemPrompt: String
)
```

---

## OpenAI Integration

### Supported Models

- `gpt-4o` — Latest GPT-4 Omni
- `gpt-4o-mini` — Cost-effective GPT-4
- `gpt-3.5-turbo` — Fast and affordable

### Configuration

```kotlin
provider = "openai"
baseUrl = "https://api.openai.com/v1"
model = "gpt-4o-mini"  // default
apiKey = "sk-..."      // from platform.openai.com
```

### Request Format

**Endpoint:** `POST /chat/completions`

```json
{
  "model": "gpt-4o-mini",
  "messages": [
    {
      "role": "system",
      "content": "Anda adalah asisten yang merangkum percakapan..."
    },
    {
      "role": "user",
      "content": "[transcript text]"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 1500
}
```

**Headers:**
```http
Authorization: Bearer sk-...
Content-Type: application/json
```

### Response Format

```json
{
  "id": "chatcmpl-...",
  "object": "chat.completion",
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "[summary text]"
      },
      "finish_reason": "stop"
    }
  ]
}
```

### Implementation

```kotlin
private suspend fun callOpenAI(text: String, config: ProviderConfig): Result<String> {
    val url = "${config.baseUrl}/chat/completions"
    
    val requestBody = JSONObject().apply {
        put("model", config.model)
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", config.systemPrompt.ifBlank { defaultPrompt })
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", text)
            })
        })
        put("temperature", 0.7)
        put("max_tokens", 1500)
    }
    
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer ${config.apiKey}")
        .header("Content-Type", "application/json")
        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
        .build()
    
    val response = client.newCall(request).execute()
    val json = JSONObject(response.body?.string() ?: "")
    return Result.success(
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    )
}
```

---

## Anthropic Integration

### Supported Models

- `claude-sonnet-4-20250514` — Claude Sonnet 4
- `claude-opus-4-20250514` — Claude Opus 4

### Configuration

```kotlin
provider = "anthropic"
baseUrl = "https://api.anthropic.com"
model = "claude-sonnet-4-20250514"
apiKey = "sk-ant-..."  // from console.anthropic.com
```

### Request Format

**Endpoint:** `POST /v1/messages`

```json
{
  "model": "claude-sonnet-4-20250514",
  "max_tokens": 1500,
  "system": "Anda adalah asisten yang merangkum percakapan...",
  "messages": [
    {
      "role": "user",
      "content": "[transcript text]"
    }
  ]
}
```

**Headers:**
```http
x-api-key: sk-ant-...
anthropic-version: 2023-06-01
Content-Type: application/json
```

### Response Format

```json
{
  "id": "msg_...",
  "type": "message",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "[summary text]"
    }
  ],
  "stop_reason": "end_turn"
}
```

### Implementation

```kotlin
private suspend fun callAnthropic(text: String, config: ProviderConfig): Result<String> {
    val url = "${config.baseUrl}/v1/messages"
    
    val requestBody = JSONObject().apply {
        put("model", config.model)
        put("max_tokens", 1500)
        put("system", config.systemPrompt.ifBlank { defaultPrompt })
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", text)
            })
        })
    }
    
    val request = Request.Builder()
        .url(url)
        .header("x-api-key", config.apiKey)
        .header("anthropic-version", "2023-06-01")
        .header("Content-Type", "application/json")
        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
        .build()
    
    val response = client.newCall(request).execute()
    val json = JSONObject(response.body?.string() ?: "")
    return Result.success(
        json.getJSONArray("content")
            .getJSONObject(0)
            .getString("text")
    )
}
```

---

## Google Gemini Integration

### Supported Models

- `gemini-1.5-pro-latest` — Most capable
- `gemini-1.5-flash-latest` — Fastest

### Configuration

```kotlin
provider = "google"
baseUrl = "https://generativelanguage.googleapis.com"
model = "gemini-1.5-flash-latest"
apiKey = "AIza..."  // from aistudio.google.com
```

### Request Format

**Endpoint:** `POST /v1beta/models/{model}:generateContent?key={apiKey}`

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "System: [system prompt]\n\nUser: [transcript]"
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 1500
  }
}
```

**Headers:**
```http
Content-Type: application/json
```

**Note:** API key in URL, not header

### Response Format

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "[summary text]"
          }
        ]
      },
      "finishReason": "STOP"
    }
  ]
}
```

### Implementation

```kotlin
private suspend fun callGoogle(text: String, config: ProviderConfig): Result<String> {
    val url = "${config.baseUrl}/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
    
    val prompt = buildString {
        if (config.systemPrompt.isNotBlank()) {
            append("System: ${config.systemPrompt}\n\n")
        }
        append("User: $text")
    }
    
    val requestBody = JSONObject().apply {
        put("contents", JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            })
        })
        put("generationConfig", JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 1500)
        })
    }
    
    val request = Request.Builder()
        .url(url)
        .header("Content-Type", "application/json")
        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
        .build()
    
    val response = client.newCall(request).execute()
    val json = JSONObject(response.body?.string() ?: "")
    return Result.success(
        json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    )
}
```

---

## xAI (Grok) Integration

### Supported Models

- `grok-beta` — Latest Grok model

### Configuration

```kotlin
provider = "xai"
baseUrl = "https://api.x.ai/v1"
model = "grok-beta"
apiKey = "xai-..."  // from x.ai
```

### Request Format

**Same as OpenAI** (OpenAI-compatible endpoint)

```json
{
  "model": "grok-beta",
  "messages": [
    { "role": "system", "content": "..." },
    { "role": "user", "content": "..." }
  ],
  "temperature": 0.7,
  "max_tokens": 1500
}
```

**Headers:**
```http
Authorization: Bearer xai-...
Content-Type: application/json
```

### Implementation

```kotlin
// Uses callOpenAI() with xAI base URL
```

---

## Zhipu AI (GLM) Integration

### Supported Models

- `glm-5.2` — Latest GLM model

### Configuration

```kotlin
provider = "custom"  // or detect "z.ai" in baseUrl
baseUrl = "https://api.z.ai/api/anthropic"  // ⚠️ Special routing
model = "glm-5.2"
apiKey = "..."  // from zhipuai.cn
```

### ⚠️ Important: Reverse Proxy Format

**GLM global API** uses Anthropic-compatible format via reverse proxy at `api.z.ai`:

- **Base URL:** `https://api.z.ai/api/anthropic` (not `/api/openai`)
- **Endpoint:** `POST /v1/messages`
- **Auth:** `Authorization: Bearer` (not `x-api-key`)
- **Request:** Anthropic Messages API format

### Request Format

```json
{
  "model": "glm-5.2",
  "max_tokens": 1500,
  "system": "...",
  "messages": [
    { "role": "user", "content": "..." }
  ]
}
```

**Headers:**
```http
Authorization: Bearer [token]
Content-Type: application/json
```

### Implementation

```kotlin
// Detect z.ai reverse proxy
if (config.baseUrl.contains("z.ai")) {
    return callAnthropic(text, config.copy(
        baseUrl = "https://api.z.ai/api/anthropic"
    ))
}
```

---

## Custom Provider Integration

### OpenAI-Compatible Endpoints

Any API supporting OpenAI chat completions format:

```kotlin
provider = "custom"
baseUrl = "https://your-api.com/v1"
model = "your-model"
apiKey = "your-key"
```

**Examples:**
- **LocalAI:** `http://localhost:8080/v1`
- **LM Studio:** `http://localhost:1234/v1`
- **Ollama (with adapter):** Custom wrapper

### Anthropic-Compatible Endpoints

Any API supporting Anthropic Messages API:

```kotlin
provider = "custom"
baseUrl = "https://your-api.com"
model = "your-model"
apiKey = "your-key"
```

**Detection Logic:**
```kotlin
// If baseUrl contains "anthropic" or "claude", use Anthropic format
if (config.baseUrl.contains("anthropic", ignoreCase = true) ||
    config.model.contains("claude", ignoreCase = true)) {
    callAnthropic(text, config)
} else {
    callOpenAI(text, config)  // default to OpenAI format
}
```

---

## Error Handling

### Common HTTP Errors

| Status Code | Meaning | Action |
|-------------|---------|--------|
| 401 | Invalid API key | Check key in Settings |
| 403 | Permission denied | Verify account access |
| 429 | Rate limit exceeded | Wait and retry |
| 500 | Server error | Retry after delay |
| 503 | Service unavailable | Provider down, try later |

### Implementation

```kotlin
suspend fun summarize(text: String, config: ProviderConfig?): Result<String> {
    return try {
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            return Result.failure(HttpException(
                code = response.code,
                message = when (response.code) {
                    401 -> "Invalid API key"
                    429 -> "Rate limit exceeded"
                    500 -> "Provider server error"
                    else -> "HTTP ${response.code}: ${response.message}"
                }
            ))
        }
        
        // Parse response...
        
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Network Errors

```kotlin
try {
    // API call
} catch (e: SocketTimeoutException) {
    Result.failure(Exception("Request timeout - check network"))
} catch (e: UnknownHostException) {
    Result.failure(Exception("Cannot reach server - check internet"))
} catch (e: SSLException) {
    Result.failure(Exception("SSL error - check HTTPS certificate"))
} catch (e: IOException) {
    Result.failure(Exception("Network error: ${e.message}"))
}
```

---

## Testing API Integration

### Manual Testing via cURL

#### OpenAI
```bash
curl https://api.openai.com/v1/chat/completions \
  -H "Authorization: Bearer sk-..." \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4o-mini",
    "messages": [{"role": "user", "content": "Summarize: Hello world"}]
  }'
```

#### Anthropic
```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: sk-ant-..." \
  -H "anthropic-version: 2023-06-01" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "claude-sonnet-4-20250514",
    "max_tokens": 1024,
    "messages": [{"role": "user", "content": "Summarize: Hello world"}]
  }'
```

#### Google
```bash
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIza..." \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{"parts": [{"text": "Summarize: Hello world"}]}]
  }'
```

### Unit Testing

```kotlin
@Test
fun `OpenAI summarization returns valid response`() = runTest {
    val summarizer = AiSummarizer(context)
    val config = ProviderConfig(
        provider = "openai",
        apiKey = "test-key",
        model = "gpt-4o-mini",
        baseUrl = "https://api.openai.com/v1",
        systemPrompt = ""
    )
    
    val result = summarizer.summarize("Test transcript", config)
    
    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull())
}
```

---

## Rate Limits & Quotas

### OpenAI

| Tier | RPM | TPM |
|------|-----|-----|
| Free | 3 | 40,000 |
| Tier 1 | 500 | 200,000 |
| Tier 2 | 5,000 | 2,000,000 |

**Implementation:** Exponential backoff on 429

### Anthropic

- **Rate limits:** Tier-based (check console)
- **Context window:** Claude Sonnet 4 = 200K tokens

### Google

- **Free tier:** 15 RPM
- **Paid tier:** Higher limits with billing

### Recommended Retry Logic

```kotlin
suspend fun <T> retryWithBackoff(
    maxAttempts: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxAttempts - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (e is HttpException && e.code == 429) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            } else {
                throw e
            }
        }
    }
    return block() // last attempt
}
```

---

## Security Best Practices

### API Key Storage

**Current:** SharedPreferences (plaintext)

**Recommended:** EncryptedSharedPreferences

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val securePrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### HTTPS Enforcement

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        if (request.url.scheme != "https") {
            throw SecurityException("HTTP not allowed - use HTTPS")
        }
        chain.proceed(request)
    }
    .build()
```

### Certificate Pinning (Optional)

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.openai.com", "sha256/...")
    .add("api.anthropic.com", "sha256/...")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

## Troubleshooting

### Issue: "Invalid API key"

**Cause:** Wrong key or expired

**Fix:**
1. Verify key in provider dashboard
2. Regenerate if needed
3. Update in Settings → AI Provider → API Key

### Issue: "Connection timeout"

**Cause:** Network slow or provider down

**Fix:**
1. Check internet connection
2. Increase timeout: `client.readTimeout(60, TimeUnit.SECONDS)`
3. Try different provider

### Issue: GLM returns 404

**Cause:** Wrong base URL

**Fix:** Use `https://api.z.ai/api/anthropic` (not `/api/openai`)

### Issue: Response is truncated

**Cause:** `max_tokens` too low

**Fix:** Increase to 2000+ in request

### Issue: Summary in wrong language

**Cause:** Default system prompt in Indonesian

**Fix:** Set custom English prompt in Settings

---

## Future Enhancements

- [ ] Streaming responses (SSE)
- [ ] Token usage tracking
- [ ] Cost estimation per provider
- [ ] Offline summarization (on-device LLM)
- [ ] Custom temperature/top_p controls
- [ ] Multi-turn conversation history
- [ ] Function calling support

---

## API Documentation Links

- **OpenAI:** https://platform.openai.com/docs/api-reference
- **Anthropic:** https://docs.anthropic.com/en/api/messages
- **Google:** https://ai.google.dev/gemini-api/docs
- **xAI:** https://docs.x.ai/api
- **Zhipu:** https://zhipuai.cn/devapi (Chinese)

---

**End of API Guide**
