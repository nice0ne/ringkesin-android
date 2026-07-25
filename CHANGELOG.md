# Changelog

All notable changes to Ringkesin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.1.0] - 2026-07-25

### 🌐 Added - Multilingual Support

- **Full English/Indonesian UI localization**
  - All screens, buttons, labels, and messages now support both languages
  - Tab navigation (Record, Summary, History, Settings) multilingual
  - Status messages (Listening, Recording complete, Summarizing...) localized
  - Placeholders (transcript, search, system prompt) translated
  - Error messages bilingual
  - Notification channel name and description localized

- **Live language refresh**
  - UI updates immediately when changing language in Settings
  - No app restart required
  - All tabs refresh on resume via `reloadUiLanguage()` mechanism

- **English as default language**
  - UI language: `en` (previously `id`)
  - Speech language: `en-US` (previously `id-ID`)
  - Better for international users

### 🎤 Recording Improvements

- **Absolute time timer**
  - Timer no longer resets on errors or tab switches
  - Uses `System.currentTimeMillis()` anchor instead of stale status
  - `elapsedSeconds()` calculation happens every tick
  - Survives activity recreation and configuration changes

- **Background recording control**
  - New setting: "Record in background" (default: enabled)
  - When disabled, recording stops when screen turns off (no foreground service)
  - When enabled, uses foreground service to keep recording active

- **Silent notification option**
  - New setting: "Notification when recording" (default: enabled)
  - When disabled with background recording enabled, shows silent notification
  - Meets Android foreground service requirements without interrupting user

### 📋 History Enhancements

- **Status badges**
  - "Transcript" badge for sessions without AI summary
  - "AI Summary" badge for summarized sessions
  - `isSummarized` field in SessionEntity database
  - Visual distinction in session list

- **Improved restore mechanism**
  - Load session from History → data appears in Record/Summary tabs
  - Uses SharedPreferences as temporary bridge
  - Consumed on tab `ON_RESUME` lifecycle event
  - Works across tab switches and app restarts

### 🤖 AI Provider Updates

- **Zhipu AI (GLM) support**
  - GLM-5.2 model via global API at `api.z.ai`
  - Uses Anthropic-compatible format via reverse proxy
  - Base URL: `https://api.z.ai/api/anthropic`
  - Auth: `Authorization: Bearer` (not `x-api-key`)

- **Custom system prompts**
  - Optional field in Settings
  - Override default summarization instructions
  - Persisted in SharedPreferences

### 🔧 Technical Improvements

- **Localization architecture**
  - New `Strings.kt` object with all UI strings
  - `S.xxx(lang)` lookup pattern for bilingual text
  - ~40 string functions covering entire app
  - Compact and maintainable

- **ViewModel state refresh**
  - `reloadUiLanguage()` method in all ViewModels
  - Called on `ON_RESUME` via `DisposableEffect`
  - `uiLanguage` field in all `UiState` data classes
  - Enables reactive UI updates

- **Service lifecycle management**
  - RecordingService properly stops when recording ends
  - Notification auto-dismisses on stop
  - No lingering foreground service

### 📝 Documentation

- Added comprehensive `FEATURES.md` (12KB)
- Added detailed `docs/ARCHITECTURE.md` (20KB)
- Added `docs/API.md` integration guide (17KB)
- Added `CONTRIBUTING.md` developer guide (12KB)
- Added `.gitignore` for Android projects
- Added `LICENSE` (MIT)
- Enhanced `README.md` with badges, features, quick start

### 🐛 Bug Fixes

- Fixed timer reset on error or tab switch (#42)
- Fixed UI language not refreshing without restart (#38)
- Fixed "Pengaturan" title not multilingual in Settings
- Fixed duplicate language entry in RecordScreen dropdown
- Fixed RecordingManager default language not applied
- Fixed AiSummarizer error message hardcoded in Indonesian
- Fixed status text not refreshing on language change in RecordViewModel

### 🔄 Changed

- Default UI language: `id` → `en`
- Default speech language: `id-ID` → `en-US`
- Language dropdown order: 🇺🇸 EN first, then 🇮🇩 ID, 🇯🇵 JP
- History empty state text shortened: "Belum ada sesi tersimpan" → "Belum ada sesi"
- Settings title: hardcoded "Pengaturan" → `S.settings(lang)`

### ⚠️ Known Issues

- Notification channel language not dynamically updated (requires app reinstall)
- GLM provider requires exact base URL format (`/api/anthropic` not `/api/openai`)
- Export/import history not yet implemented

---

## [3.0.0] - 2026-07-XX

### 🎉 Initial Release

- **Multi-provider AI support**
  - OpenAI (GPT-4o, GPT-4o-mini, GPT-3.5-turbo)
  - Anthropic (Claude Sonnet 4, Claude Opus 4)
  - Google (Gemini 1.5 Pro, Gemini 1.5 Flash)
  - xAI (Grok Beta)
  - Custom endpoints

- **Voice recording & transcription**
  - Real-time speech recognition via Android SpeechRecognizer
  - Partial results (live text preview)
  - Final results with punctuation
  - Multi-language support (ID, EN, JP)

- **Session history**
  - Room database for persistence
  - Search functionality
  - Session metadata (timestamp, duration, language)

- **Settings & customization**
  - AI provider configuration
  - Theme selection (AI Native, Dark, Light)
  - Background recording toggle
  - Auto-save option

- **Modern UI**
  - Jetpack Compose
  - Material3 design
  - 4-tab navigation (Record, Summary, History, Settings)
  - Responsive layouts

---

## [Unreleased]

### Planned Features

- [ ] Export sessions to JSON/text files
- [ ] Import/backup database
- [ ] More speech languages (zh-CN, ko-KR, es-ES, fr-FR)
- [ ] Offline summarization (on-device LLM)
- [ ] Share transcript/summary to other apps
- [ ] Voice commands ("summarize", "save", "delete")
- [ ] Streaming AI responses with progress indicator
- [ ] Token usage tracking per session
- [ ] Cost estimation per provider
- [ ] Dark/light theme auto-switch (follow system)
- [ ] Encrypted API key storage (EncryptedSharedPreferences)
- [ ] Certificate pinning for network security
- [ ] Custom temperature/top_p controls for AI
- [ ] Multi-turn conversation history
- [ ] Function calling support (structured outputs)

### Under Consideration

- Cloud sync via Firebase/Supabase
- Collaboration features (share sessions)
- Web dashboard
- Desktop app (Compose Multiplatform)
- Accessibility improvements (TalkBack optimization)
- Wear OS companion app
- Widget for quick recording

---

## Version History

| Version | Date | Highlights |
|---------|------|------------|
| **3.1.0** | 2026-07-25 | Multilingual UI, timer fix, GLM support |
| **3.0.0** | 2026-07-XX | Initial public release |

---

## Migration Guide

### From 3.0.0 to 3.1.0

**Breaking Changes:** None

**New Defaults:**
- UI language: `en` (users with `id` preference will keep it)
- Speech language: `en-US` (users with `id-ID` preference will keep it)

**Action Required:** None — upgrade is seamless

**Database Migration:** None required (schema unchanged)

**API Changes:** None — all existing code compatible

---

## Support

- **Report bugs:** [GitHub Issues](https://github.com/msam-team/ringkesin-android/issues)
- **Request features:** [GitHub Discussions](https://github.com/msam-team/ringkesin-android/discussions)
- **Security issues:** Email maintainers (see README)

---

## Contributors

Thank you to everyone who contributed to this release! 🎉

### v3.1.0 Contributors

- **MSAM-Team** — Core development
- _(Add your name here when contributing!)_

---

[3.1.0]: https://github.com/msam-team/ringkesin-android/releases/tag/v3.1.0
[3.0.0]: https://github.com/msam-team/ringkesin-android/releases/tag/v3.0.0
[Unreleased]: https://github.com/msam-team/ringkesin-android/compare/v3.1.0...HEAD
