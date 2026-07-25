# Ringkesin — Features & Usage Guide

**Version:** 3.1.0  
**Package:** `com.msam.ringkesin`  
**Platform:** Android (Kotlin + Jetpack Compose)

---

## Overview

Ringkesin is a speech-to-text Android app with AI-powered summarization. Record voice, get instant transcripts, and generate smart summaries using multiple AI providers.

---

## Core Features

### 1. 🎤 Voice Recording & Transcription

**Real-time speech recognition** powered by Android SpeechRecognizer API.

#### Features:
- **Multi-language support**
  - English (🇺🇸 EN)
  - Indonesian (🇮🇩 ID)
  - Japanese (🇯🇵 JP)
- **Live transcription** — see text appear as you speak
- **Partial results** — real-time preview while speaking
- **Timer** — absolute time tracking (survives errors/tab switches)
- **Background recording** — optional (disable in Settings)
- **Recording notification** — optional (disable in Settings)

#### Usage:
1. Open **Record** tab
2. Select speech language (default: 🇺🇸 EN)
3. Tap microphone button to start
4. Speak naturally
5. Tap "Stop" when done

#### Controls:
| Button | Function |
|--------|----------|
| 🎤 Microphone | Start/Stop recording |
| 📋 Copy | Copy transcript to clipboard |
| 💾 Save | Manually save session to history |
| 🗑️ Delete | Clear current transcript |

---

### 2. 🤖 AI Summarization

**Multi-provider AI integration** — summarize transcripts with GPT, Claude, Gemini, or custom models.

#### Supported Providers:
- **OpenAI** (GPT-4o, GPT-4o-mini, GPT-3.5-turbo)
- **Anthropic** (Claude Sonnet 4, Claude Opus 4)
- **Google** (Gemini 1.5 Pro, Gemini 1.5 Flash)
- **xAI** (Grok Beta)
- **Zhipu AI (GLM)** — via reverse proxy at `api.z.ai`
- **Custom providers** — any OpenAI-compatible or Anthropic-compatible API

#### Features:
- **One-tap summarization** — "Summarize with AI" button
- **Custom system prompts** — define your summarization style
- **Auto-save** — optional (disable in Settings)
- **Bilingual support** — summaries follow transcript language
- **Streaming support** — real-time summary generation (where supported)

#### Usage:
1. Record a session (or load from History)
2. Tap **"Summarize with AI"**
3. Wait for processing (status: "Summarizing...")
4. View summary in **Summary** tab

#### API Configuration (Settings):
```
Provider: OpenAI / Anthropic / Google / xAI / Custom
API Key: sk-...
Model: gpt-4o-mini (auto-suggested per provider)
Base URL: https://api.openai.com/v1 (auto-filled)
System Prompt: (optional custom instructions)
```

---

### 3. 📋 History & Session Management

**Persistent storage** — all sessions saved to local SQLite database.

#### Features:
- **Session list** — chronological view with timestamps
- **Status badges** — "Transcript" vs "AI Summary"
- **Search** — full-text search across all transcripts
- **Restore** — load any session back to Record/Summary tabs
- **Delete** — remove individual sessions

#### Session Metadata:
```kotlin
data class SessionEntity(
    val id: Long,
    val timestamp: Long,
    val transcript: String,
    val summary: String,
    val language: String,        // e.g., "en-US"
    val duration: Int,            // seconds
    val isSummarized: Boolean     // has AI summary
)
```

#### Usage:
1. Open **History** tab
2. Search (optional): type in search bar
3. Tap session card → see details
4. **Load** button → restore to Record/Summary tabs
5. **Delete** button → remove from history

---

### 4. 🌐 Multilingual UI

**Fully localized interface** — switch between Indonesian and English.

#### Features:
- **UI Language** — all labels, buttons, messages
- **Speech Language** — recognition language (independent)
- **Live refresh** — changes apply instantly (no restart)
- **Persistent** — saved in SharedPreferences

#### Supported UI Languages:
| Language | Code | Default |
|----------|------|---------|
| English | `en` | ✅ Yes |
| Indonesian | `id` | No |

#### Localized Elements:
- Tab navigation (Record, Summary, History, Settings)
- Status messages (Listening, Recording complete, Summarizing...)
- Button labels (Copy, Save, Delete, Load)
- Settings sections (AI Provider, Recording, Data, About)
- Placeholders (Transcript will appear here, Search transcripts...)
- Notifications (Voice Recording)
- Error messages (API Key not filled in Settings)

#### Usage:
1. Open **Settings** tab
2. Find **"UI Language"** under **Tampilan** section
3. Tap dropdown → select 🇺🇸 English or 🇮🇩 Bahasa Indonesia
4. All text updates immediately

---

### 5. ⚙️ Settings & Configuration

**Comprehensive customization** — control every aspect of the app.

#### Settings Categories:

##### 🤖 AI Provider
```
Provider: [OpenAI | Anthropic | Google | xAI | Custom]
API Key: (required for summarization)
Model: (auto-suggested per provider)
Base URL: (auto-filled, editable for custom endpoints)
```

##### 🎨 Theme
```
Theme: [AI Native | Dark | Light]
UI Language: [🇺🇸 English | 🇮🇩 Bahasa Indonesia]
```

##### 🎤 Recording
```
☑ Background Record — allow recording while screen off
☑ Notification — show notification during recording
☑ Auto Save — save sessions automatically when done
```

##### 📝 System Prompt
```
Custom instructions for AI summarization
(Optional, uses default if empty)
```

##### 💾 Data
```
Export all data → JSON file
Delete all data → clear history
```

##### ℹ️ About
```
Version: 3.1.0 · Android
Created by: MSAM-Team
```

---

## Advanced Features

### Auto-Save Logic

**Smart session persistence** — saves automatically when enabled.

**Triggers:**
1. Recording stops → transcript saved to history
2. AI summary generated → session updated with summary + `isSummarized = true`

**Disable:** Settings → Recording → uncheck "Auto Save"

---

### Restore from History

**Resume previous sessions** — load transcript + summary back to active state.

**Mechanism:**
```kotlin
// On History "Load" tap:
SharedPreferences.edit {
    putString("last_transcript", session.transcript)
    putString("last_summary", session.summary)
}

// Record/Summary tabs check on ON_RESUME:
fun checkRestoredData() {
    val transcript = prefs.getString("last_transcript", "")
    val summary = prefs.getString("last_summary", "")
    if (transcript.isNotBlank()) {
        _uiState.value = _uiState.value.copy(
            transcript = transcript,
            summaryResult = summary
        )
        prefs.edit { clear() } // consume once
    }
}
```

---

### Background Recording

**Continue recording with screen off** — requires notification for Android foreground service.

**Settings:**
- **Background Record ON** + **Notification ON** → normal notification
- **Background Record ON** + **Notification OFF** → silent notification (Android requirement)
- **Background Record OFF** → skip foreground service entirely (recording stops when screen off)

**Implementation:**
```kotlin
fun startRecordingInternal() {
    if (isBackgroundRecordEnabled()) {
        val showNotif = isNotificationOn()
        RecordingService.start(getApplication(), showNotif)
    }
    manager.start(_uiState.value.selectedLanguage)
}
```

---

### Timer Resilience

**Absolute time tracking** — timer never resets.

**Problem (before):**
```kotlin
// ❌ Stale data — status.elapsedSeconds not updated during tick
statusText.elapsedSeconds // frozen value
```

**Solution (after):**
```kotlin
// ✅ Absolute time calculation
val startedAt = System.currentTimeMillis()
fun elapsedSeconds() = ((now() - startedAt) / 1000).toInt()

// Composable ticks every second
LaunchedEffect(Unit) {
    while (true) {
        delay(1000)
        viewModel.tick() // read fresh elapsed time
    }
}
```

---

### Language Refresh Mechanism

**UI language updates without restart** — all tabs + nav recompose.

**Architecture:**
```
SettingsScreen → user selects language
    ↓
SettingsViewModel.setUiLanguage(lang)
    ↓
SharedPreferences.edit { putString("ui_lang", lang) }
    ↓
Every tab ON_RESUME → reloadUiLanguage()
    ↓
_uiState.value = _uiState.value.copy(uiLanguage = readPrefs())
    ↓
All S.xxx(state.uiLanguage) recompose ✅
```

**Files involved:**
- `Strings.kt` — `object S` with all bilingual text
- `RecordViewModel.kt` — `reloadUiLanguage()` on ON_RESUME
- `SummaryViewModel.kt` — `reloadUiLanguage()` on ON_RESUME
- `HistoryScreen.kt` — baca langsung dari prefs tiap recompose
- `AppNavigation.kt` — `screen.displayName(uiLang)` for tab labels

---

## Technical Details

### Architecture

**MVVM + Jetpack Compose**

```
UI Layer (Compose)
    ├── RecordScreen.kt
    ├── SummaryScreen.kt
    ├── HistoryScreen.kt
    └── SettingsScreen.kt
    
ViewModel Layer
    ├── RecordViewModel.kt
    ├── SummaryViewModel.kt
    ├── HistoryViewModel.kt
    └── SettingsViewModel.kt
    
Data Layer
    ├── SessionEntity.kt (Room)
    ├── SessionDao.kt
    └── RingkesinDatabase.kt
    
Service Layer
    ├── RecordingManager.kt (SpeechRecognizer)
    ├── RecordingService.kt (Foreground service)
    └── AiSummarizer.kt (Multi-provider API client)
```

---

### Dependencies

**Core:**
- Kotlin 2.1.0
- Compose BOM 2024.10.01
- Material3
- Navigation Compose

**Data:**
- Room 2.6.1
- Coroutines 1.9.0
- SharedPreferences

**Network:**
- OkHttp 4.12.0
- Gson 2.11.0

**Android:**
- SDK 35 (target)
- SDK 29 (min)

---

### Storage

**SharedPreferences** (`ringkesin_settings`):
```kotlin
"ai_provider"       → "openai" | "anthropic" | "google" | "xai" | "custom"
"api_key"           → String
"model"             → String
"base_url"          → String
"theme"             → "AI_NATIVE" | "DARK" | "LIGHT"
"ui_lang"           → "en" | "id"  (default: "en")
"background_record" → Boolean (default: true)
"notification_on"   → Boolean (default: true)
"auto_save"         → Boolean (default: true)
"system_prompt"     → String (optional)
"last_transcript"   → String (temp, for restore)
"last_summary"      → String (temp, for restore)
```

**Room Database** (`ringkesin.db`):
```sql
CREATE TABLE sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    transcript TEXT NOT NULL,
    summary TEXT NOT NULL,
    language TEXT NOT NULL,
    duration INTEGER NOT NULL,
    isSummarized INTEGER NOT NULL DEFAULT 0
);
```

---

## Troubleshooting

### Common Issues

#### 1. "API Key not filled in Settings"
**Cause:** AI summarization requires valid API key  
**Fix:** Settings → AI Provider → enter API Key

#### 2. Recording not working
**Cause:** Microphone permission denied  
**Fix:** Android Settings → Apps → Ringkesin → Permissions → Microphone → Allow

#### 3. Notification not showing
**Cause:** Notification setting disabled  
**Fix:** Settings → Recording → enable "Notification"

#### 4. Timer resets unexpectedly
**Fix:** Already fixed in v3.1.0 — timer uses absolute time

#### 5. Language UI not refreshing
**Fix:** Already fixed in v3.1.0 — auto-refresh on tab switch

#### 6. GLM provider not working
**Check:** Base URL must be `https://api.z.ai/api/anthropic` (not `/api/openai`)

---

## Keyboard Shortcuts

N/A — mobile app with touch interface only

---

## Privacy & Security

- **Local-first** — all data stored on device
- **No cloud sync** — transcripts never leave your phone (except API calls)
- **API keys** — stored in SharedPreferences (not encrypted)
- **Permissions required:**
  - 🎤 Microphone — for voice recording
  - 🔔 Notifications — for background recording (optional)

---

## Future Roadmap

**Planned Features:**
- [ ] Export sessions to text/JSON
- [ ] Import/backup database
- [ ] More speech languages (zh-CN, ko-KR, etc.)
- [ ] Voice commands ("summarize", "save", "delete")
- [ ] Offline summarization (on-device LLM)
- [ ] Share transcript/summary
- [ ] Dark/light theme auto-switch

---

## Credits

**Developed by:** MSAM-Team  
**Version:** 3.1.0  
**License:** [Not specified]  
**Contact:** [Not specified]

---

## Changelog

### v3.1.0 (2026-07-25)
- ✅ Multilingual UI (English + Indonesian)
- ✅ Default language changed to English
- ✅ Timer resilience (absolute time)
- ✅ Live language refresh (no restart)
- ✅ History status badges
- ✅ Background recording control
- ✅ Silent notification option
- ✅ GLM/Zhipu AI support
- ✅ All placeholders multilingual
- ✅ Tab navigation multilingual

### v3.0.0 (earlier)
- Multi-provider AI support (OpenAI, Anthropic, Google, xAI)
- Room database for session history
- Jetpack Compose UI
- Material3 theme
- Auto-save sessions
- Search history
- Custom system prompts

---

**End of Documentation**
