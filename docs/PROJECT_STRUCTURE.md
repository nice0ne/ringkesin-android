# Project Structure

## Directory Layout

```
ringkesin-android/
├── .gitignore                      # Git ignore rules
├── LICENSE                         # MIT License
├── README.md                       # Project overview & quick start
├── FEATURES.md                     # Comprehensive feature documentation
├── CHANGELOG.md                    # Version history & release notes
├── CONTRIBUTING.md                 # Development guidelines
├── build.gradle.kts                # Root Gradle configuration
├── settings.gradle.kts             # Gradle project settings
├── gradle.properties               # Gradle build properties
├── local.properties                # Local SDK paths (not in git)
│
├── docs/                           # Extended documentation
│   ├── ARCHITECTURE.md             # Technical architecture guide
│   ├── API.md                      # AI provider integration guide
│   └── FLOW.md                     # Application flow diagrams
│
├── gradle/                         # Gradle wrapper files
│   └── wrapper/
│       └── gradle-wrapper.properties
│
└── app/                            # Main application module
    ├── build.gradle.kts            # App module Gradle config
    │
    └── src/main/
        ├── AndroidManifest.xml     # App manifest & permissions
        │
        ├── java/com/msam/ringkesin/
        │   │
        │   ├── MainActivity.kt     # Single activity entry point
        │   ├── RingkesinApp.kt     # Application class
        │   │
        │   ├── data/               # Data layer
        │   │   ├── local/          # Local storage
        │   │   │   ├── AppDatabase.kt
        │   │   │   ├── dao/
        │   │   │   │   └── SessionDao.kt
        │   │   │   └── entity/
        │   │   │       └── SessionEntity.kt
        │   │   │
        │   │   ├── remote/         # Network layer
        │   │   │   └── AiSummarizer.kt
        │   │   │
        │   │   └── repository/     # Repository pattern
        │   │       └── SessionRepository.kt
        │   │
        │   ├── service/            # Background services
        │   │   ├── RecordingManager.kt
        │   │   └── RecordingService.kt
        │   │
        │   └── ui/                 # Presentation layer
        │       ├── localization/   # Multilingual strings
        │       │   └── Strings.kt
        │       │
        │       ├── navigation/     # App navigation
        │       │   ├── AppNavigation.kt
        │       │   └── Screen.kt
        │       │
        │       ├── record/         # Recording feature
        │       │   ├── RecordScreen.kt
        │       │   └── RecordViewModel.kt
        │       │
        │       ├── summary/        # Summary feature
        │       │   ├── SummaryScreen.kt
        │       │   └── SummaryViewModel.kt
        │       │
        │       ├── history/        # History feature
        │       │   ├── HistoryScreen.kt
        │       │   └── HistoryViewModel.kt
        │       │
        │       ├── settings/       # Settings feature
        │       │   ├── SettingsScreen.kt
        │       │   └── SettingsViewModel.kt
        │       │
        │       └── theme/          # UI theming
        │           ├── Color.kt
        │           └── Theme.kt
        │
        └── res/                    # Android resources
            ├── drawable/           # Vector drawables
            ├── mipmap-*/           # App icons
            ├── values/
            │   ├── strings.xml     # App name only
            │   └── themes.xml      # Material theme config
            └── xml/                # (if needed)
```

---

## File Count Summary

| Category | Count | Description |
|----------|-------|-------------|
| **Kotlin Source** | 22 files | All app logic |
| **Documentation** | 7 files | README, guides, changelog |
| **Gradle Config** | 3 files | Build scripts |
| **Android Resources** | 6 files | Manifests, strings, themes |
| **Total** | **38 files** | Excluding build outputs |

---

## Key Files

### Root Level

| File | Purpose |
|------|---------|
| `README.md` | Project overview, quick start, badges |
| `FEATURES.md` | Complete feature documentation (12KB) |
| `CHANGELOG.md` | Version history with detailed release notes |
| `CONTRIBUTING.md` | Developer contribution guidelines |
| `LICENSE` | MIT License |
| `.gitignore` | Exclude build outputs, IDE files, secrets |

### Documentation (`docs/`)

| File | Size | Purpose |
|------|------|---------|
| `ARCHITECTURE.md` | 21KB | Technical architecture, patterns, testing |
| `API.md` | 17KB | AI provider integration, auth, troubleshooting |
| `FLOW.md` | 31KB | User journey diagrams, state flow, schemas |

### App Module (`app/src/main/java/`)

#### Entry Points

| File | Lines | Purpose |
|------|-------|---------|
| `MainActivity.kt` | ~50 | Single activity, theme management |
| `RingkesinApp.kt` | ~30 | Application class, singleton init |

#### Data Layer (`data/`)

| File | Lines | Purpose |
|------|-------|---------|
| `AppDatabase.kt` | ~25 | Room database singleton |
| `SessionDao.kt` | ~40 | DAO interface for session CRUD |
| `SessionEntity.kt` | ~20 | Session table entity |
| `AiSummarizer.kt` | ~210 | Multi-provider AI client |
| `SessionRepository.kt` | ~50 | Repository abstraction |

#### Service Layer (`service/`)

| File | Lines | Purpose |
|------|-------|---------|
| `RecordingManager.kt` | ~150 | SpeechRecognizer wrapper |
| `RecordingService.kt` | ~120 | Foreground service for background recording |

#### UI Layer (`ui/`)

**Localization:**

| File | Lines | Purpose |
|------|-------|---------|
| `Strings.kt` | ~90 | Bilingual UI strings (EN/ID) |

**Navigation:**

| File | Lines | Purpose |
|------|-------|---------|
| `AppNavigation.kt` | ~90 | Bottom nav + NavHost |
| `Screen.kt` | ~25 | Sealed class for routes |

**Features:**

| Feature | Screen | ViewModel | Total Lines |
|---------|--------|-----------|-------------|
| **Record** | ~600 | ~380 | ~980 |
| **Summary** | ~250 | ~100 | ~350 |
| **History** | ~280 | ~120 | ~400 |
| **Settings** | ~420 | ~180 | ~600 |

**Theme:**

| File | Lines | Purpose |
|------|-------|---------|
| `Color.kt` | ~20 | Material3 color scheme |
| `Theme.kt` | ~80 | Theme composable + logic |

---

## Code Statistics

### By Layer

| Layer | Files | Approx. Lines | Purpose |
|-------|-------|---------------|---------|
| **UI** | 14 | ~2,900 | Compose screens + ViewModels |
| **Data** | 5 | ~350 | Database, network, repository |
| **Service** | 2 | ~270 | Background services |
| **Total** | **21** | **~3,520** | Kotlin source code |

### Language Distribution

```
Kotlin:        ~3,520 lines (95%)
XML:           ~150 lines (4%)
Markdown:      ~90KB text (docs)
```

---

## Architecture Breakdown

### MVVM Pattern

```
UI Layer (14 files)
  ├── Screens (Compose) → 4 files × ~250 lines avg
  ├── ViewModels → 4 files × ~150 lines avg
  ├── Navigation → 2 files
  ├── Theme → 2 files
  └── Localization → 1 file

Data Layer (5 files)
  ├── Room (Database) → 3 files
  ├── Network (AI API) → 1 file
  └── Repository → 1 file

Service Layer (2 files)
  ├── RecordingManager → SpeechRecognizer wrapper
  └── RecordingService → Foreground service
```

---

## Dependencies Overview

### Core Libraries

```kotlin
// Kotlin
kotlin = "2.1.0"
coroutines = "1.9.0"

// Android
compileSdk = 35
minSdk = 29
targetSdk = 35

// Compose
compose-bom = "2024.10.01"
compose-compiler = "1.5.15"
material3 = "1.3.1"
activity-compose = "1.9.3"
navigation-compose = "2.8.4"

// Architecture
lifecycle = "2.8.7"
viewmodel-compose = "2.8.7"

// Data
room = "2.6.1"

// Network
okhttp = "4.12.0"
gson = "2.11.0"
```

### Dependency Graph

```
app/
├─ Kotlin stdlib
├─ Coroutines (core, android)
│
├─ AndroidX
│   ├─ Core KTX
│   ├─ Lifecycle (runtime, viewmodel)
│   └─ Activity Compose
│
├─ Compose
│   ├─ UI
│   ├─ Material3
│   ├─ Navigation
│   └─ Preview tooling
│
├─ Room
│   ├─ Runtime
│   ├─ KTX
│   └─ Compiler (kapt)
│
└─ Network
    ├─ OkHttp
    └─ Gson
```

---

## Build Outputs

### APK Structure (after build)

```
app/build/outputs/apk/debug/
└── app-debug.apk                   # ~5-8 MB

Contents:
  ├── classes.dex                   # Compiled Kotlin/Java
  ├── resources.arsc                # App resources
  ├── AndroidManifest.xml           # Manifest
  ├── res/                          # Drawables, values
  └── META-INF/                     # Signatures
```

### Excluded from Git

```
# Build outputs
build/
*.apk
*.aab

# IDE files
.idea/
*.iml
local.properties

# Gradle cache
.gradle/
```

---

## Testing Structure (to be added)

### Planned Test Organization

```
app/src/
├── test/                           # Unit tests
│   └── java/com/msam/ringkesin/
│       ├── RecordViewModelTest.kt
│       ├── SummaryViewModelTest.kt
│       ├── SessionDaoTest.kt
│       └── AiSummarizerTest.kt
│
└── androidTest/                    # Instrumentation tests
    └── java/com/msam/ringkesin/
        ├── RecordScreenTest.kt
        ├── NavigationTest.kt
        └── DatabaseTest.kt
```

---

## Module Organization

### Current: Single Module

```
ringkesin-android/
└── app/                            # All code in one module
```

### Future: Multi-Module (optional)

```
ringkesin-android/
├── app/                            # Main app module
├── core/                           # Shared utilities
│   ├── data/                       # Data layer
│   ├── domain/                     # Business logic
│   └── ui/                         # Shared UI components
├── feature/
│   ├── record/                     # Record feature module
│   ├── summary/                    # Summary feature module
│   ├── history/                    # History feature module
│   └── settings/                   # Settings feature module
└── buildSrc/                       # Build configuration
```

**Benefits:**
- Faster incremental builds
- Better separation of concerns
- Parallel compilation
- Easier testing

**Current Status:** Not needed for current app size (~3.5K lines)

---

## Asset Organization

### Icons & Images

```
app/src/main/res/
├── mipmap-mdpi/                    # App icon 48×48
├── mipmap-hdpi/                    # App icon 72×72
├── mipmap-xhdpi/                   # App icon 96×96
├── mipmap-xxhdpi/                  # App icon 144×144
└── mipmap-xxxhdpi/                 # App icon 192×192
```

**Current:** Compose icons only (Material Icons)  
**Future:** Custom vector drawables if needed

---

## Configuration Files

### Gradle

| File | Purpose |
|------|---------|
| `settings.gradle.kts` | Project settings, plugin repos |
| `build.gradle.kts` (root) | Global plugins, versions |
| `app/build.gradle.kts` | App dependencies, build config |
| `gradle.properties` | JVM args, Android flags |

### Android

| File | Purpose |
|------|---------|
| `AndroidManifest.xml` | Permissions, activities, services |
| `strings.xml` | App name only (UI strings in Strings.kt) |
| `themes.xml` | Base Material3 theme reference |

---

## Documentation Size

| File | Size | Words | Purpose |
|------|------|-------|---------|
| `README.md` | 9KB | ~1,200 | Overview |
| `FEATURES.md` | 13KB | ~1,700 | Features |
| `CHANGELOG.md` | 8KB | ~1,100 | History |
| `CONTRIBUTING.md` | 13KB | ~1,800 | Guidelines |
| `docs/ARCHITECTURE.md` | 21KB | ~2,800 | Technical |
| `docs/API.md` | 17KB | ~2,300 | Integration |
| `docs/FLOW.md` | 31KB | ~1,200 | Diagrams |
| **Total** | **112KB** | **~12,100 words** | Complete docs |

---

## Maintenance

### File Ownership

| Area | Maintainer | Files |
|------|------------|-------|
| **UI/UX** | Frontend dev | `ui/` screens, theme |
| **Backend Logic** | Backend dev | ViewModels, data layer |
| **Services** | Systems dev | RecordingManager, Service |
| **Documentation** | Tech writer | All `.md` files |

### Update Frequency

| File | Update When |
|------|-------------|
| `README.md` | Major feature added |
| `FEATURES.md` | Any feature change |
| `CHANGELOG.md` | Every release |
| `ARCHITECTURE.md` | Architecture change |
| `API.md` | New provider / API change |

---

## Future Additions

### Planned Files

- [ ] `SECURITY.md` — Security policy & disclosure
- [ ] `CODE_OF_CONDUCT.md` — Community guidelines
- [ ] `SUPPORT.md` — Getting help
- [ ] `.github/ISSUE_TEMPLATE/` — Issue templates
- [ ] `.github/PULL_REQUEST_TEMPLATE.md` — PR template
- [ ] `.github/workflows/` — CI/CD (GitHub Actions)
- [ ] `app/schemas/` — Room database schemas (migrations)
- [ ] `fastlane/` — Automated deployment

---

**Last Updated:** 2026-07-25  
**Project Version:** 3.1.0  
**Total Files:** 38 (excluding build outputs)
