# Architecture Documentation

## Overview

Ringkesin follows **Clean Architecture** principles with **MVVM pattern** in the presentation layer. The app is structured in layers that promote separation of concerns, testability, and maintainability.

---

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ RecordScreen │  │ SummaryScreen│  │ HistoryScreen│ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│  └─────────────────── Jetpack Compose ────────────────┘│
└─────────────────────────────────────────────────────────┘
                          ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                  ViewModel Layer                        │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────┐│
│  │ RecordViewModel│  │SummaryViewModel│  │HistoryVM  ││
│  └────────────────┘  └────────────────┘  └───────────┘│
│  └─────────────── StateFlow / LiveData ───────────────┘│
└─────────────────────────────────────────────────────────┘
                          ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                   Domain Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Use Cases   │  │   Models     │  │  Repositories│ │
│  │  (optional)  │  │ SessionEntity│  │  (implicit)  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                          ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                           │
│  ┌───────────────┐  ┌─────────────┐  ┌──────────────┐ │
│  │ Room Database │  │SharedPrefs  │  │ AiSummarizer │ │
│  │  SessionDao   │  │  Settings   │  │   (Network)  │ │
│  └───────────────┘  └─────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                          ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                  Service Layer                          │
│  ┌─────────────────┐          ┌───────────────────────┐│
│  │RecordingManager │          │  RecordingService     ││
│  │ (SpeechRecogn.) │          │  (Foreground Service) ││
│  └─────────────────┘          └───────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

---

## Layer Details

### 1. UI Layer (Presentation)

**Technology:** Jetpack Compose + Material3

**Responsibility:** Display UI and handle user interactions

**Components:**
```kotlin
// Screens (stateless composables)
@Composable fun RecordScreen(viewModel: RecordViewModel)
@Composable fun SummaryScreen(viewModel: SummaryViewModel)
@Composable fun HistoryScreen(viewModel: HistoryViewModel)
@Composable fun SettingsScreen(viewModel: SettingsViewModel)

// Navigation
@Composable fun AppNavigation(onThemeChanged: (RingkesinTheme) -> Unit)
sealed class Screen(val route: String, val title: String, val icon: ImageVector)
```

**Key Patterns:**
- **Stateless Composables** — UI state comes from ViewModel
- **Single Source of Truth** — ViewModel holds `UiState`
- **Unidirectional Data Flow** — UI emits events, ViewModel updates state
- **Compose Lifecycle** — `DisposableEffect` for ON_RESUME hooks

**Example:**
```kotlin
@Composable
fun RecordScreen(viewModel: RecordViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    
    // UI observes state
    Text(state.statusText)
    
    // UI emits events
    Button(onClick = { viewModel.startRecording() })
}
```

---

### 2. ViewModel Layer

**Technology:** AndroidX ViewModel + Kotlin Coroutines + StateFlow

**Responsibility:** Business logic, state management, lifecycle awareness

**Components:**
```kotlin
class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    
    fun startRecording() { /* ... */ }
    fun stopRecording() { /* ... */ }
    fun summarizeWithAi() { /* ... */ }
}

data class RecordUiState(
    val isRecording: Boolean = false,
    val transcript: String = "",
    val statusText: String = "",
    val timerSeconds: Int = 0,
    // ...
)
```

**Key Patterns:**
- **StateFlow** — reactive state updates
- **Coroutines** — async operations (network, database)
- **AndroidViewModel** — access to Application context
- **Single Activity** — ViewModel survives configuration changes

**State Management:**
```kotlin
// Immutable state updates
_uiState.value = _uiState.value.copy(
    isRecording = true,
    statusText = "Recording..."
)

// Collect in Composable
val state by viewModel.uiState.collectAsState()
```

---

### 3. Domain Layer

**Responsibility:** Business rules and entities (minimal in this app)

**Components:**
```kotlin
// Entity (shared with data layer)
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val transcript: String,
    val summary: String,
    val language: String,
    val duration: Int,
    val isSummarized: Boolean = false
)

// Provider config (settings model)
data class ProviderConfig(
    val provider: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String,
    val systemPrompt: String
)
```

**Note:** This app has a thin domain layer — ViewModels access data sources directly. For larger apps, consider introducing Repository and UseCase layers.

---

### 4. Data Layer

**Technology:** Room + SharedPreferences + OkHttp

**Responsibility:** Data persistence and network access

#### Room Database

```kotlin
@Database(entities = [SessionEntity::class], version = 1)
abstract class RingkesinDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE transcript LIKE '%' || :query || '%'")
    fun searchSessions(query: String): Flow<List<SessionEntity>>
    
    @Insert
    suspend fun insert(session: SessionEntity): Long
    
    @Delete
    suspend fun delete(session: SessionEntity)
}
```

#### SharedPreferences

```kotlin
// Settings storage (key-value pairs)
"ai_provider"       → String
"api_key"           → String
"model"             → String
"ui_lang"           → String ("en" | "id")
"background_record" → Boolean
"auto_save"         → Boolean
```

#### Network Layer

```kotlin
class AiSummarizer(private val context: Context) {
    suspend fun summarize(
        text: String,
        config: ProviderConfig? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        when (config.provider) {
            "openai" -> callOpenAI(text, config)
            "anthropic" -> callAnthropic(text, config)
            "google" -> callGoogle(text, config)
            // ...
        }
    }
}
```

**HTTP Client:**
```kotlin
private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build()
```

---

### 5. Service Layer

**Technology:** Android Foreground Service + SpeechRecognizer API

#### RecordingManager

**Responsibility:** Wrap Android SpeechRecognizer API

```kotlin
class RecordingManager(private val appContext: Context) {
    private val _status = MutableStateFlow<RecordingStatus>(RecordingStatus.Idle)
    val status: StateFlow<RecordingStatus> = _status.asStateFlow()
    
    fun start(language: String = "en-US") {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer?.startListening(intent)
    }
    
    fun stop() {
        recognizer?.stopListening()
    }
}

sealed class RecordingStatus {
    object Idle : RecordingStatus()
    object Recording : RecordingStatus()
    data class PartialResult(val text: String) : RecordingStatus()
    data class FinalResult(val text: String) : RecordingStatus()
    data class Error(val message: String) : RecordingStatus()
}
```

#### RecordingService

**Responsibility:** Keep recording active in background

```kotlin
class RecordingService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Recording...")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
}
```

**Lifecycle:**
```
ViewModel.startRecording()
    ↓
RecordingService.start(context)  // foreground service
    ↓
RecordingManager.start(language) // SpeechRecognizer
    ↓
status flow emits updates
    ↓
ViewModel observes status
    ↓
UI recomposes
```

---

## Data Flow Patterns

### 1. Recording Flow

```
User taps mic button
    ↓
RecordScreen → RecordViewModel.startRecording()
    ↓
RecordingService.start() [if background enabled]
    ↓
RecordingManager.start("en-US")
    ↓
SpeechRecognizer starts listening
    ↓
Partial results → RecordingStatus.PartialResult
    ↓
ViewModel updates _uiState.partialText
    ↓
UI recomposes with live text
    ↓
Final result → RecordingStatus.FinalResult
    ↓
ViewModel updates _uiState.transcript
    ↓
Auto-save [if enabled] → SessionDao.insert()
```

### 2. Summarization Flow

```
User taps "Summarize with AI"
    ↓
RecordScreen → RecordViewModel.summarizeWithAi()
    ↓
AiSummarizer.summarize(transcript, config)
    ↓
HTTP POST to provider API (OpenAI/Anthropic/etc.)
    ↓
Result<String> returned
    ↓
ViewModel updates _uiState.summaryResult
    ↓
Auto-save [if enabled] → SessionDao.insert(isSummarized=true)
    ↓
SummaryViewModel loads from SharedPreferences
    ↓
SummaryScreen displays result
```

### 3. History Restore Flow

```
User taps "Load" in History
    ↓
HistoryScreen → HistoryViewModel.restoreSession(session)
    ↓
SharedPreferences.edit {
    putString("last_transcript", session.transcript)
    putString("last_summary", session.summary)
}
    ↓
User switches to Record tab
    ↓
RecordScreen composable enters (DisposableEffect ON_RESUME)
    ↓
RecordViewModel.checkRestoredData()
    ↓
Read from SharedPreferences
    ↓
_uiState.value = _uiState.value.copy(
    transcript = restoredTranscript,
    summaryResult = restoredSummary
)
    ↓
Clear SharedPreferences (consume once)
    ↓
UI shows restored data
```

---

## State Management Strategy

### StateFlow vs LiveData

**Choice:** StateFlow (Kotlin Coroutines)

**Rationale:**
- Native Kotlin (no Android dependency)
- Better for Compose (no lifecycle awareness needed)
- Built-in operators (map, filter, combine)
- Immutable state updates

**Pattern:**
```kotlin
// ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Composable
val state by viewModel.uiState.collectAsState()
```

### Single State vs Multiple Streams

**Choice:** Single `UiState` data class per screen

**Rationale:**
- Single source of truth
- Easier to reason about
- Atomic updates (no race conditions)
- Simpler testing

**Example:**
```kotlin
data class RecordUiState(
    val isRecording: Boolean = false,
    val transcript: String = "",
    val partialText: String = "",
    val timerSeconds: Int = 0,
    val statusText: String = "",
    val errorMessage: String? = null,
    val summaryResult: String = "",
    val isSummarizing: Boolean = false,
    // ... all screen state in one place
)
```

---

## Dependency Injection

**Choice:** Manual DI (no framework)

**Rationale:**
- Small app with few dependencies
- ViewModels created via factory
- Singleton managers injected via Application

**Pattern:**
```kotlin
class RingkesinApp : Application() {
    lateinit var recordingManager: RecordingManager
    lateinit var database: RingkesinDatabase
    
    override fun onCreate() {
        super.onCreate()
        recordingManager = RecordingManager(this)
        database = Room.databaseBuilder(
            applicationContext,
            RingkesinDatabase::class.java,
            "ringkesin.db"
        ).build()
    }
}

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val manager = (application as RingkesinApp).recordingManager
    private val sessionDao = (application as RingkesinApp).database.sessionDao()
}
```

**Future:** Consider Hilt/Koin if app grows.

---

## Threading Model

### Coroutine Dispatchers

```kotlin
// UI updates
viewModelScope.launch { // Main dispatcher
    _uiState.value = newState
}

// Network calls
suspend fun summarize(): Result<String> = withContext(Dispatchers.IO) {
    // HTTP request
}

// Database operations
viewModelScope.launch {
    withContext(Dispatchers.IO) {
        sessionDao.insert(session)
    }
}
```

### StateFlow Collection

```kotlin
// ViewModel
manager.status.collect { status ->
    when (status) {
        is RecordingStatus.Recording -> { /* ... */ }
        is RecordingStatus.FinalResult -> { /* ... */ }
    }
}
```

---

## Error Handling

### Network Errors

```kotlin
suspend fun summarize(): Result<String> {
    return try {
        val response = httpClient.execute(request)
        Result.success(response.body)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ViewModel
viewModelScope.launch {
    result.onSuccess { summary ->
        _uiState.value = _uiState.value.copy(summaryResult = summary)
    }.onFailure { error ->
        _uiState.value = _uiState.value.copy(errorMessage = error.message)
    }
}
```

### Recording Errors

```kotlin
override fun onError(error: Int) {
    _status.value = RecordingStatus.Error(
        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            else -> "Recognition error: $error"
        }
    )
}
```

---

## Testing Strategy

### ViewModel Tests

```kotlin
@Test
fun `startRecording updates state correctly`() = runTest {
    val viewModel = RecordViewModel(application)
    
    viewModel.startRecording()
    
    val state = viewModel.uiState.value
    assertTrue(state.isRecording)
    assertEquals("Recording...", state.statusText)
}
```

### Compose UI Tests

```kotlin
@Test
fun `clicking mic button starts recording`() {
    composeTestRule.setContent {
        RecordScreen(viewModel)
    }
    
    composeTestRule.onNodeWithContentDescription("Microphone")
        .performClick()
    
    // Assert state change
}
```

### Room Tests

```kotlin
@Test
fun `insert and query session`() = runTest {
    val session = SessionEntity(
        timestamp = System.currentTimeMillis(),
        transcript = "Test",
        summary = "",
        language = "en-US",
        duration = 10
    )
    
    sessionDao.insert(session)
    val sessions = sessionDao.getAllSessions().first()
    
    assertEquals(1, sessions.size)
    assertEquals("Test", sessions[0].transcript)
}
```

---

## Performance Considerations

### Compose Recomposition

**Optimization:** Stable state classes + remember

```kotlin
// ❌ Bad: causes recomposition on every frame
val timer = viewModel.getTimer()

// ✅ Good: stable, recomposes only when state changes
val state by viewModel.uiState.collectAsState()
```

### Database Queries

**Optimization:** Flow + Room caching

```kotlin
// Room automatically caches and emits only on changes
@Query("SELECT * FROM sessions ORDER BY timestamp DESC")
fun getAllSessions(): Flow<List<SessionEntity>>
```

### Network Calls

**Optimization:** Timeout + connection pooling

```kotlin
private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build()
```

---

## Security Considerations

### API Key Storage

**Current:** SharedPreferences (not encrypted)

**Risk:** Root access or ADB can read keys

**Mitigation:** EncryptedSharedPreferences (future improvement)

```kotlin
// Future implementation
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### Network Security

**Implementation:** HTTPS only, certificate pinning optional

```kotlin
// Current: trust system CAs
// Future: pin certificates for critical providers
```

---

## Future Architecture Improvements

### 1. Repository Layer

Introduce abstraction between ViewModel and data sources:

```kotlin
class SessionRepository(
    private val sessionDao: SessionDao,
    private val remoteApi: RemoteApi? = null
) {
    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()
    
    suspend fun syncWithCloud() {
        // Future: cloud backup
    }
}
```

### 2. Use Cases

Extract complex business logic:

```kotlin
class SummarizeTranscriptUseCase(
    private val summarizer: AiSummarizer,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(transcript: String): Result<String> {
        val result = summarizer.summarize(transcript)
        if (result.isSuccess) {
            sessionRepository.markAsSummarized(transcript)
        }
        return result
    }
}
```

### 3. Dependency Injection

Adopt Hilt for cleaner DI:

```kotlin
@HiltAndroidApp
class RingkesinApp : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideRecordingManager(app: Application): RecordingManager {
        return RecordingManager(app)
    }
}
```

---

## Conclusion

Ringkesin's architecture prioritizes:

1. **Simplicity** — no over-engineering for a small app
2. **Maintainability** — clear separation of concerns
3. **Testability** — pure functions and dependency injection
4. **Scalability** — easy to add Repository/UseCase layers later

The current structure balances pragmatism with best practices, suitable for a single-developer or small-team project.
