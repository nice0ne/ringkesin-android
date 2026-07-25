# Contributing to Ringkesin

Thank you for your interest in contributing to Ringkesin! This document provides guidelines and instructions for contributing to the project.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing](#testing)
- [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of experience level, background, or identity.

### Expected Behavior

- Be respectful and considerate
- Welcome newcomers and help them get started
- Accept constructive criticism gracefully
- Focus on what is best for the project
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Trolling, insulting, or derogatory remarks
- Personal or political attacks
- Publishing others' private information without permission

---

## Getting Started

### Prerequisites

**Required:**
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 35
- Git

**Recommended:**
- Kotlin 2.1.0+
- Gradle 8.7+
- Device or emulator running Android 10 (API 29) or higher

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/ringkesin-android.git
   cd ringkesin-android
   ```
3. **Add upstream remote:**
   ```bash
   git remote add upstream https://github.com/msam-team/ringkesin-android.git
   ```

### Build and Run

1. **Open in Android Studio:**
   - File → Open → select `ringkesin-android` directory

2. **Sync Gradle:**
   - Android Studio will prompt to sync
   - Or run: `./gradlew build`

3. **Run on device/emulator:**
   - Click "Run" button
   - Or: `./gradlew installDebug`

---

## Development Workflow

### Branching Strategy

We use **feature branches** off `main`:

```
main (stable)
  ├─ feature/add-voice-commands
  ├─ fix/timer-reset-bug
  └─ docs/update-api-guide
```

**Branch naming:**
- `feature/` — new features
- `fix/` — bug fixes
- `docs/` — documentation updates
- `refactor/` — code refactoring
- `test/` — test additions

### Step-by-Step Workflow

1. **Sync with upstream:**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Create feature branch:**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Make changes:**
   - Write code
   - Add tests
   - Update documentation

4. **Test locally:**
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```

5. **Commit changes:**
   ```bash
   git add .
   git commit -m "feat: add amazing feature"
   ```

6. **Push to your fork:**
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Open Pull Request** on GitHub

---

## Coding Standards

### Kotlin Style Guide

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

**Key points:**
- **Indentation:** 4 spaces (no tabs)
- **Line length:** Max 120 characters
- **Naming:**
  - Classes: `PascalCase`
  - Functions: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Private properties: `_camelCase` (if mutable state)

### Example

```kotlin
class RecordViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    
    fun startRecording() {
        viewModelScope.launch {
            manager.start()
            _uiState.value = _uiState.value.copy(isRecording = true)
        }
    }
    
    companion object {
        private const val TAG = "RecordViewModel"
        private const val MAX_RETRIES = 3
    }
}
```

### Compose Best Practices

**Stateless Composables:**
```kotlin
// ✅ Good: stateless, testable
@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Text(if (isRecording) "Stop" else "Record")
    }
}

// ❌ Bad: stateful, hard to test
@Composable
fun RecordButton(viewModel: RecordViewModel) {
    val state by viewModel.uiState.collectAsState()
    Button(onClick = { viewModel.toggle() }) { ... }
}
```

**Preview Functions:**
```kotlin
@Preview(showBackground = true)
@Composable
fun RecordButtonPreview() {
    RingkesinTheme {
        RecordButton(isRecording = false, onClick = {})
    }
}
```

### Architecture Guidelines

**ViewModel:**
- All UI state in single `UiState` data class
- Use `StateFlow` for reactive updates
- Coroutines for async operations
- No Android framework dependencies (except Application)

**Repository (if added):**
- Single source of truth for data
- Abstract data sources (Room, network, SharedPreferences)
- Return `Flow` for observable data, `suspend fun` for one-shot operations

**Data Layer:**
- Room entities immutable (`val` properties)
- Network models separate from domain models
- Use `Result<T>` for error handling

---

## Commit Guidelines

### Conventional Commits

We use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat:` — new feature
- `fix:` — bug fix
- `docs:` — documentation only
- `style:` — formatting, missing semicolons, etc.
- `refactor:` — code restructuring
- `test:` — adding tests
- `chore:` — build tasks, dependencies

**Examples:**
```bash
feat(record): add voice command support
fix(timer): prevent reset on configuration change
docs(api): add GLM integration guide
refactor(ViewModel): extract summarization logic to UseCase
test(RecordViewModel): add unit tests for recording flow
chore(deps): update Compose BOM to 2024.11.00
```

### Commit Best Practices

- **One logical change per commit**
- **Write descriptive messages**
- **Reference issues:** `fix(timer): resolve #42`
- **Keep commits atomic** (small, self-contained)

---

## Pull Request Process

### Before Opening PR

1. **Sync with upstream main:**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run tests:**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest  # if device/emulator available
   ```

3. **Build successfully:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Lint code:**
   ```bash
   ./gradlew lint
   ```

### PR Template

When opening a PR, include:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] UI tests added/updated
- [ ] Manual testing completed

## Screenshots (if UI change)
[Add screenshots or screen recordings]

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests pass locally
```

### Review Process

1. **Automated checks** must pass:
   - Build success
   - Tests pass
   - Lint clean

2. **Code review** by maintainer:
   - Code quality
   - Architecture fit
   - Test coverage
   - Documentation

3. **Approval** → **Merge** (squash and merge)

### After Merge

1. **Delete feature branch:**
   ```bash
   git branch -d feature/amazing-feature
   git push origin --delete feature/amazing-feature
   ```

2. **Sync main:**
   ```bash
   git checkout main
   git pull upstream main
   ```

---

## Testing

### Unit Tests

**Location:** `app/src/test/java/`

**Example:**
```kotlin
class RecordViewModelTest {
    
    private lateinit var viewModel: RecordViewModel
    private lateinit var application: Application
    
    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = RecordViewModel(application)
    }
    
    @Test
    fun `startRecording updates state correctly`() = runTest {
        viewModel.startRecording()
        
        val state = viewModel.uiState.value
        assertTrue(state.isRecording)
        assertEquals("Recording...", state.statusText)
    }
}
```

**Run tests:**
```bash
./gradlew test
./gradlew testDebugUnitTest  # specific variant
```

### Instrumentation Tests

**Location:** `app/src/androidTest/java/`

**Example:**
```kotlin
@RunWith(AndroidJUnit4::class)
class RecordScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun clickingMicButtonStartsRecording() {
        val viewModel = RecordViewModel(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            RecordScreen(viewModel)
        }
        
        composeTestRule.onNodeWithContentDescription("Microphone")
            .performClick()
        
        assertTrue(viewModel.uiState.value.isRecording)
    }
}
```

**Run tests:**
```bash
./gradlew connectedAndroidTest
```

### Test Coverage

**Aim for:**
- ViewModels: 80%+ coverage
- Critical business logic: 90%+ coverage
- UI: Smoke tests for key flows

---

## Documentation

### When to Update Docs

- **New feature** → Update `FEATURES.md` and `README.md`
- **API change** → Update `docs/API.md`
- **Architecture change** → Update `docs/ARCHITECTURE.md`
- **Bug fix** → Add entry to `CHANGELOG.md`

### Documentation Standards

**Code comments:**
- Public APIs: KDoc format
- Complex logic: inline comments
- No redundant comments

**Example:**
```kotlin
/**
 * Starts voice recording with the specified language.
 *
 * @param language Speech recognition language (e.g., "en-US", "id-ID")
 * @throws SecurityException if microphone permission not granted
 */
fun start(language: String = "en-US") {
    // Check permission before starting
    if (!hasPermission()) {
        throw SecurityException("Microphone permission required")
    }
    
    // Configure recognizer intent
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    
    recognizer?.startListening(intent)
}
```

### README Updates

When adding a feature, update the feature table in `README.md`:

```markdown
| Feature | Description |
|---------|-------------|
| 🎤 **Voice Recording** | Real-time speech recognition with live transcription |
| 🎙️ **Voice Commands** | Control app with voice (new!) |
```

---

## Issue Reporting

### Bug Reports

Use the following template:

```markdown
## Bug Description
Clear description of what went wrong

## Steps to Reproduce
1. Open Record screen
2. Tap microphone button
3. Speak for 10 seconds
4. Timer resets unexpectedly

## Expected Behavior
Timer should continue counting

## Actual Behavior
Timer resets to 0 after tab switch

## Environment
- Device: Pixel 7
- Android Version: 14
- App Version: 3.1.0

## Screenshots/Logs
[Attach if available]
```

### Feature Requests

```markdown
## Feature Description
Brief description of proposed feature

## Use Case
Who benefits and how?

## Proposed Implementation
Technical approach (if you have ideas)

## Alternatives Considered
Other approaches you've thought about
```

---

## Areas for Contribution

### Good First Issues

- 🐛 **Bug fixes** — small, well-defined issues
- 📝 **Documentation** — typos, clarifications, examples
- 🧪 **Tests** — increase coverage
- 🌐 **Localization** — add new UI languages

### Advanced Contributions

- ✨ **New features** — voice commands, cloud sync, export
- 🏗️ **Architecture** — Repository layer, UseCase abstraction
- 🔐 **Security** — EncryptedSharedPreferences, certificate pinning
- ⚡ **Performance** — optimize Compose recomposition, reduce APK size

### Wishlist

- [ ] Export sessions to JSON/text
- [ ] Import/backup database
- [ ] More speech languages (zh-CN, ko-KR, es-ES)
- [ ] Offline summarization (on-device LLM)
- [ ] Share transcript/summary
- [ ] Dark/light theme auto-switch
- [ ] Voice commands ("summarize", "save", "delete")
- [ ] Streaming AI responses
- [ ] Token usage tracking
- [ ] Cost estimation per provider

---

## Getting Help

- **Questions:** [GitHub Discussions](https://github.com/msam-team/ringkesin-android/discussions)
- **Bugs:** [GitHub Issues](https://github.com/msam-team/ringkesin-android/issues)
- **Real-time chat:** [Discord/Slack link if available]

---

## License

By contributing to Ringkesin, you agree that your contributions will be licensed under the [MIT License](LICENSE).

---

## Recognition

Contributors will be acknowledged in:
- `README.md` contributors section
- Release notes for significant contributions
- Special mentions for major features

Thank you for making Ringkesin better! 🎉

---

**Happy Contributing!**
