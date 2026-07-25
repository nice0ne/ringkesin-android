# Application Flow

## User Journey Diagrams

### 1. Recording Flow

```
┌─────────────────────────────────────────────────────────────┐
│                     USER STARTS RECORDING                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
                   ┌────────────────┐
                   │ RecordScreen   │
                   │ User taps mic  │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ RecordViewModel│
                   │ startRecording()│
                   └────────┬───────┘
                            ↓
              ┌─────────────┴─────────────┐
              │                           │
    ┌─────────▼─────────┐      ┌─────────▼──────────┐
    │RecordingService   │      │ RecordingManager   │
    │(if background=ON) │      │ start("en-US")     │
    │startForeground()  │      │                    │
    └─────────┬─────────┘      └─────────┬──────────┘
              │                           │
              │                           ↓
              │                ┌──────────────────────┐
              │                │ SpeechRecognizer API │
              │                │ startListening()     │
              │                └──────────┬───────────┘
              │                           │
              │                           ↓
              │                ┌──────────────────────┐
              │                │ Partial Results      │
              │                │ (live text preview)  │
              │                └──────────┬───────────┘
              │                           │
              └───────────────┬───────────┘
                              ↓
                    ┌─────────────────────┐
                    │ RecordViewModel     │
                    │ update partialText  │
                    │ update timer (1/sec)│
                    └─────────┬───────────┘
                              ↓
                    ┌─────────────────────┐
                    │ UI Recomposes       │
                    │ Shows live text     │
                    └─────────────────────┘
                              ↓
                   [User stops recording]
                              ↓
                    ┌─────────────────────┐
                    │ Final Result        │
                    │ Complete transcript │
                    └─────────┬───────────┘
                              ↓
                    ┌─────────────────────┐
                    │ Auto-save (if ON)   │
                    │ SessionDao.insert() │
                    └─────────────────────┘
```

---

### 2. AI Summarization Flow

```
┌─────────────────────────────────────────────────────────────┐
│               USER REQUESTS AI SUMMARY                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
                   ┌────────────────┐
                   │ RecordScreen   │
                   │ "Summarize"    │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ RecordViewModel│
                   │summarizeWithAi()│
                   └────────┬───────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Check API Key             │
              └─────────────┬─────────────┘
                            ↓
                   ┌────────────────┐
                   │ AiSummarizer   │
                   │ summarize()    │
                   └────────┬───────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Detect Provider           │
              └─────────────┬─────────────┘
                            ↓
        ┌───────────────────┼───────────────────┐
        │                   │                   │
  ┌─────▼─────┐      ┌─────▼─────┐      ┌─────▼─────┐
  │  OpenAI   │      │ Anthropic │      │  Google   │
  │ /chat/... │      │/v1/messages│      │/generate..│
  └─────┬─────┘      └─────┬─────┘      └─────┬─────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ↓
                   ┌────────────────┐
                   │ HTTP POST      │
                   │ with transcript│
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ Provider API   │
                   │ Processing...  │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ Parse Response │
                   │ Extract summary│
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ RecordViewModel│
                   │update summary  │
                   └────────┬───────┘
                            ↓
              ┌─────────────┴─────────────┐
              │                           │
    ┌─────────▼─────────┐      ┌─────────▼──────────┐
    │ SummaryViewModel  │      │ Auto-save (if ON)  │
    │ Load from prefs   │      │ SessionDao.update()│
    │ Display result    │      │ isSummarized=true  │
    └───────────────────┘      └────────────────────┘
```

---

### 3. History & Restore Flow

```
┌─────────────────────────────────────────────────────────────┐
│                USER BROWSES HISTORY                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
                   ┌────────────────┐
                   │ HistoryScreen  │
                   │ Show sessions  │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ HistoryViewModel│
                   │ Load from DB   │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ SessionDao     │
                   │getAllSessions()│
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ Flow<List<...>>│
                   │ Reactive query │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ UI displays    │
                   │ Session cards  │
                   │ + badges       │
                   └────────────────┘
                            ↓
                   [User taps "Load"]
                            ↓
                   ┌────────────────┐
                   │ HistoryViewModel│
                   │restoreSession()│
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │SharedPreferences│
                   │ Store temp data│
                   │ "last_..."     │
                   └────────┬───────┘
                            ↓
              [User switches to Record tab]
                            ↓
                   ┌────────────────┐
                   │ RecordScreen   │
                   │ ON_RESUME      │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ RecordViewModel│
                   │checkRestoredData│
                   └────────┬───────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Read from SharedPrefs     │
              │ Update UI state           │
              │ Clear prefs (consume once)│
              └─────────────┬─────────────┘
                            ↓
                   ┌────────────────┐
                   │ UI displays    │
                   │ Restored data  │
                   └────────────────┘
```

---

### 4. Settings & Language Change Flow

```
┌─────────────────────────────────────────────────────────────┐
│              USER CHANGES UI LANGUAGE                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
                   ┌────────────────┐
                   │ SettingsScreen │
                   │ Select EN/ID   │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ SettingsViewModel│
                   │ setUiLanguage()│
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │SharedPreferences│
                   │ Save "ui_lang" │
                   └────────┬───────┘
                            ↓
              [User switches to any tab]
                            ↓
        ┌───────────────────┼───────────────────┐
        │                   │                   │
  ┌─────▼─────┐      ┌─────▼─────┐      ┌─────▼─────┐
  │  Record   │      │  Summary  │      │  History  │
  │ ON_RESUME │      │ ON_RESUME │      │Recompose  │
  └─────┬─────┘      └─────┬─────┘      └─────┬─────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ↓
                   ┌────────────────┐
                   │ViewModel       │
                   │reloadUiLanguage│
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ Read prefs     │
                   │ Update state   │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ All composables│
                   │ S.xxx(lang)    │
                   │ Recompose ✅   │
                   └────────────────┘
```

---

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Record   │  │ Summary  │  │ History  │  │ Settings │   │
│  │ Screen   │  │ Screen   │  │ Screen   │  │ Screen   │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
        │ observe     │ observe     │ observe     │ observe
        │ state       │ state       │ state       │ state
        ↓             ↓             ↓             ↓
┌─────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Record   │  │ Summary  │  │ History  │  │ Settings │   │
│  │ViewModel │  │ViewModel │  │ViewModel │  │ViewModel │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
        ↓             ↓             ↓             ↓
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                            │
│  ┌───────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │ Recording │  │   Ai     │  │ Session  │  │  Shared  │  │
│  │  Manager  │  │Summarizer│  │   Dao    │  │   Prefs  │  │
│  └─────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  │
└────────┼─────────────┼─────────────┼─────────────┼─────────┘
         │             │             │             │
         ↓             ↓             ↓             ↓
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL SYSTEMS                         │
│  ┌───────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Speech   │  │ OpenAI   │  │  Room    │  │   File   │  │
│  │Recognizer │  │ Anthropic│  │ Database │  │  System  │  │
│  │   API     │  │  etc.    │  │          │  │          │  │
│  └───────────┘  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## State Management Flow

```
                    ┌─────────────────┐
                    │   User Action   │
                    │  (tap, speak)   │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │   UI Layer      │
                    │ onClick/onEvent │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │  ViewModel      │
                    │  Event Handler  │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │  Business Logic │
                    │  (coroutine)    │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │   Data Layer    │
                    │  (suspend fun)  │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │   Result<T>     │
                    │  success/failure│
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │  ViewModel      │
                    │  Update State   │
                    │ _state.value =  │
                    │   state.copy()  │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │  StateFlow      │
                    │    emits        │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │  Composable     │
                    │  Recomposes     │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │   UI Updates    │
                    │  User sees new  │
                    │     state       │
                    └─────────────────┘
```

---

## Database Schema

```
┌────────────────────────────────────────────────────────┐
│                   SessionEntity                        │
├────────────────────────────────────────────────────────┤
│ id: Long (PK, autoincrement)                           │
│ timestamp: Long (unix millis)                          │
│ transcript: String (full text)                         │
│ summary: String (AI-generated, empty if not summarized)│
│ language: String (e.g., "en-US", "id-ID")              │
│ duration: Int (seconds)                                │
│ isSummarized: Boolean (has AI summary)                 │
└────────────────────────────────────────────────────────┘

Indexes:
- PRIMARY KEY: id
- INDEX: timestamp DESC (for sorted list)

Queries:
- getAllSessions(): ORDER BY timestamp DESC
- searchSessions(query): WHERE transcript LIKE '%query%'
- insert(session): returns inserted id
- delete(session): removes by id
```

---

## Network Request Flow (AI Summarization)

```
┌─────────────────────────────────────────────────────────────┐
│                    AiSummarizer.summarize()                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Load config from prefs    │
              │ (provider, key, model)    │
              └─────────────┬─────────────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Validate API key          │
              │ (return error if blank)   │
              └─────────────┬─────────────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Detect provider           │
              │ (openai/anthropic/google) │
              └─────────────┬─────────────┘
                            ↓
        ┌───────────────────┼───────────────────┐
        │                   │                   │
  ┌─────▼─────┐      ┌─────▼─────┐      ┌─────▼─────┐
  │  OpenAI   │      │ Anthropic │      │  Google   │
  │  format   │      │  format   │      │  format   │
  └─────┬─────┘      └─────┬─────┘      └─────┬─────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ↓
                   ┌────────────────┐
                   │ Build JSON     │
                   │ request body   │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ Create Request │
                   │ + headers      │
                   │ + auth token   │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ OkHttp execute │
                   │ (blocking IO)  │
                   └────────┬───────┘
                            ↓
              ┌─────────────┴─────────────┐
              │ Check HTTP status         │
              └─────────────┬─────────────┘
                            ↓
                   ┌────────────────┐
                   │ Parse JSON     │
                   │ Extract text   │
                   └────────┬───────┘
                            ↓
                   ┌────────────────┐
                   │ Return Result  │
                   │ success(text)  │
                   │ or failure(e)  │
                   └────────────────┘
```

---

## Lifecycle Hooks & Data Flow

```
╔════════════════════════════════════════════════════════════╗
║                    APP LIFECYCLE                           ║
╚════════════════════════════════════════════════════════════╝

onCreate (Application)
    ↓
Initialize singletons:
    - RecordingManager
    - Room Database
    ↓
onCreate (MainActivity)
    ↓
setContent { AppNavigation() }
    ↓
╔════════════════════════════════════════════════════════════╗
║                  SCREEN LIFECYCLE                          ║
╚════════════════════════════════════════════════════════════╝

Composable enters composition
    ↓
DisposableEffect(Unit) {
    onResume { reloadUiLanguage() }
    onPause { /* cleanup if needed */ }
}
    ↓
Collect StateFlow:
    val state by viewModel.uiState.collectAsState()
    ↓
Render UI with state
    ↓
User interaction → emit event to ViewModel
    ↓
ViewModel updates state
    ↓
StateFlow emits new value
    ↓
Composable recomposes ✅
    ↓
[Repeat until screen exits]
    ↓
onDispose { /* cleanup resources */ }
```

---

## Error Handling Flow

```
User Action
    ↓
ViewModel Event Handler
    ↓
try {
    Data Layer Operation
} catch (e: Exception) {
    when (e) {
        HttpException → "API error: ${e.code}"
        IOException → "Network error"
        SecurityException → "Permission denied"
        else → "Unknown error: ${e.message}"
    }
        ↓
    _uiState.value = state.copy(
        errorMessage = errorText,
        isLoading = false
    )
}
    ↓
UI displays error
    ↓
User sees Toast/Snackbar/Dialog
```

---

## Performance Optimization Points

### 1. Compose Recomposition
- Use `remember` for expensive computations
- Stable state classes (immutable data)
- Avoid lambdas in composable parameters

### 2. Database Queries
- Room Flow auto-caches results
- Only emit when data changes
- Use pagination for large lists

### 3. Network Calls
- Timeout configuration (30s connect, 60s read)
- Connection pooling (OkHttp default)
- Cancel coroutines on ViewModel clear

### 4. Memory Management
- No memory leaks (ViewModelScope auto-cancels)
- Bitmap recycling if images added
- Clear large text fields when done

---

**End of Flow Documentation**
