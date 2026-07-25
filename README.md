# Ringkesin

<div align="center">

![Version](https://img.shields.io/badge/version-3.1.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-purple.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

**AI-Powered Voice Recording & Summarization**

Real-time speech-to-text transcription with intelligent AI summaries using GPT, Claude, Gemini, and more.

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Documentation](#-documentation) • [Contributing](#-contributing)

</div>

---

## 📖 Overview

Ringkesin is a native Android app that transforms voice into actionable text. Record conversations, lectures, meetings, or notes — get instant transcripts and AI-powered summaries in seconds.

### Key Highlights

- 🎤 **Real-time Transcription** — see text appear as you speak
- 🤖 **Multi-Provider AI** — OpenAI, Anthropic, Google, xAI, custom endpoints
- 🌐 **Multilingual** — UI in English/Indonesian, speech in 3+ languages
- 💾 **Local-First** — all data stored on your device
- 🚀 **Modern Stack** — Kotlin, Jetpack Compose, Material3

---

## ✨ Features

### Core Capabilities

| Feature | Description |
|---------|-------------|
| 🎤 **Voice Recording** | Real-time speech recognition with live transcription |
| 🤖 **AI Summarization** | One-tap summaries using GPT-4, Claude, Gemini, or custom models |
| 📋 **Session History** | Searchable database of all recordings with metadata |
| 🌐 **Multilingual UI** | Full localization (English/Indonesian) with live refresh |
| ⚙️ **Flexible Settings** | Configure AI provider, theme, language, recording behavior |

### Advanced Features

- **Background Recording** — continue recording with screen off
- **Auto-Save** — automatically save sessions when done
- **Silent Notifications** — optional foreground service without interruption
- **Restore Sessions** — load any previous transcript back to active state
- **Custom Prompts** — define your own summarization instructions
- **Absolute Timer** — resilient time tracking that survives errors

---

## 🚀 Installation

### Prerequisites

- **Android Device:** Android 10 (API 29) or higher
- **Permissions:** Microphone access (required)

### Download

1. **Clone the repository:**
   ```bash
   git clone https://github.com/msam-team/ringkesin-android.git
   cd ringkesin-android
   ```

2. **Open in Android Studio:**
   - Android Studio Hedgehog (2023.1.1) or newer
   - Gradle 8.7+
   - Kotlin 2.1.0+

3. **Build the APK:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### First Launch Setup

1. Grant microphone permission when prompted
2. Open **Settings** tab
3. Configure AI provider:
   - Select provider (OpenAI, Anthropic, etc.)
   - Enter API key
   - Choose model (auto-suggested)
4. Start recording!

---

## 🎯 Usage

### Quick Start

1. **Record Voice**
   - Open **Record** tab
   - Select speech language (🇺🇸 EN / 🇮🇩 ID / 🇯🇵 JP)
   - Tap microphone button
   - Speak naturally
   - Tap "Stop" when done

2. **Generate Summary**
   - Tap **"Summarize with AI"**
   - Wait for processing
   - View result in **Summary** tab

3. **Review History**
   - Open **History** tab
   - Search or browse sessions
   - Tap **Load** to restore any session

### Supported AI Providers

| Provider | Models | API Base |
|----------|--------|----------|
| **OpenAI** | GPT-4o, GPT-4o-mini, GPT-3.5-turbo | `api.openai.com/v1` |
| **Anthropic** | Claude Sonnet 4, Claude Opus 4 | `api.anthropic.com` |
| **Google** | Gemini 1.5 Pro, Gemini 1.5 Flash | `generativelanguage.googleapis.com` |
| **xAI** | Grok Beta | `api.x.ai/v1` |
| **Zhipu (GLM)** | GLM-5.2 | `api.z.ai/api/anthropic` |
| **Custom** | Any OpenAI/Anthropic-compatible | User-defined |

### Speech Languages

- 🇺🇸 **English** (en-US) — default
- 🇮🇩 **Indonesian** (id-ID)
- 🇯🇵 **Japanese** (ja-JP)

---

## 📚 Documentation

Comprehensive guides available in the `docs/` directory:

- **[FEATURES.md](FEATURES.md)** — Full feature list with technical details
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** — App structure and design decisions
- **[API.md](docs/API.md)** — AI provider integration guide
- **[CONTRIBUTING.md](CONTRIBUTING.md)** — Development workflow and guidelines

---

## 🏗️ Architecture

### Technology Stack

```
┌─────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)            │
│  • RecordScreen, SummaryScreen          │
│  • HistoryScreen, SettingsScreen        │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  ViewModel Layer (MVVM)                 │
│  • RecordViewModel, SummaryViewModel    │
│  • HistoryViewModel, SettingsViewModel  │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  Data Layer                             │
│  • Room Database (SessionEntity)        │
│  • SharedPreferences (Settings)         │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  Service Layer                          │
│  • RecordingManager (SpeechRecognizer)  │
│  • RecordingService (Foreground)        │
│  • AiSummarizer (Multi-provider API)    │
└─────────────────────────────────────────┘
```

### Key Dependencies

```gradle
// Core
kotlin = "2.1.0"
compose-bom = "2024.10.01"
material3 = "1.3.1"

// Data
room = "2.6.1"
coroutines = "1.9.0"

// Network
okhttp = "4.12.0"
gson = "2.11.0"
```

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `./gradlew test`
5. Build: `./gradlew assembleDebug`
6. Commit: `git commit -m 'Add amazing feature'`
7. Push: `git push origin feature/amazing-feature`
8. Open a Pull Request

### Code Style

- **Kotlin Coding Conventions** — follow official guidelines
- **Compose Best Practices** — stateless composables, preview functions
- **MVVM Pattern** — UI logic in ViewModel, data logic in Repository

---

## 🐛 Known Issues

- [ ] GLM provider requires specific base URL format
- [ ] Notification channel language not dynamically updated
- [ ] Export/import history not yet implemented

See [Issues](https://github.com/msam-team/ringkesin-android/issues) for full list.

---

## 📝 Changelog

### v3.1.0 (2026-07-25)

**🌐 Multilingual UI**
- Full English/Indonesian localization
- Live language refresh (no restart needed)
- English as default language

**🎤 Recording Improvements**
- Absolute time timer (survives errors/tab switches)
- Background recording control
- Silent notification option

**📋 History Enhancements**
- Status badges (Transcript vs AI Summary)
- Search improvements
- Session restore mechanism

**🤖 AI Provider Updates**
- Zhipu AI (GLM) support via reverse proxy
- Custom system prompts
- Auto-save summaries

### v3.0.0 (earlier)

- Multi-provider AI support (OpenAI, Anthropic, Google, xAI)
- Room database for history
- Jetpack Compose UI
- Material3 theme

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

**MSAM-Team**

- **Version:** 3.1.0
- **Platform:** Android
- **Package:** `com.msam.ringkesin`

---

## 🙏 Acknowledgments

- [Android Speech Recognition](https://developer.android.com/reference/android/speech/SpeechRecognizer) — for real-time transcription
- [OpenAI](https://openai.com/) — GPT models
- [Anthropic](https://anthropic.com/) — Claude models
- [Google AI](https://ai.google/) — Gemini models
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — modern UI toolkit

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/msam-team/ringkesin-android/issues)
- **Discussions:** [GitHub Discussions](https://github.com/msam-team/ringkesin-android/discussions)

---

<div align="center">

**Made with ❤️ by MSAM-Team**

[⬆ Back to top](#ringkesin)

</div>
