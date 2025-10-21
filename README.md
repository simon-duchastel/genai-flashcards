# GenAI Flashcards

> A Kotlin Multiplatform flashcard app that generates AI-powered study cards using **Circuit** (Slack's Compose architecture) and **Koog** (JetBrains' AI framework).

## 🚀 Features

- ✨ **AI-Powered Generation**: Create flashcards on any topic using Koog AI agents
- 💾 **Local Storage**: All flashcards saved in browser localStorage
- 📱 **Swipe Interface**: Intuitive card flipping and swiping through study sets
- 🎨 **Material 3 Design**: Modern, beautiful UI with Compose Multiplatform
- 🏗️ **Circuit Architecture**: Production-grade state management from Slack
- 🌐 **Multiplatform Ready**: Currently targeting JS/Browser, architecture supports Android/iOS

## 📦 Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Kotlin Multiplatform** | Shared business logic |
| **Compose Multiplatform** | Cross-platform UI framework |
| **Circuit** (Slack) | Presenter-based architecture |
| **Koog** (JetBrains) | AI agent framework |
| **Ktor** | HTTP client for API calls |
| **kotlinx.serialization** | Data serialization |
| **localStorage** | Browser-based persistence |

## 🏗️ Project Structure

```
genai-flashcards/
├── composeApp/src/
│   ├── commonMain/kotlin/
│   │   ├── domain/
│   │   │   ├── model/          # Flashcard, FlashcardSet data models
│   │   │   └── repository/     # FlashcardRepository
│   │   ├── data/
│   │   │   ├── storage/        # FlashcardStorage (expect/actual)
│   │   │   └── ai/             # FlashcardGenerator (Koog agent)
│   │   ├── presentation/
│   │   │   ├── home/           # HomeScreen (list all sets)
│   │   │   ├── create/         # CreateScreen (generate new cards)
│   │   │   └── study/          # StudyScreen (swipe through cards)
│   │   └── App.kt              # Circuit setup & navigation
│   ├── jsMain/kotlin/
│   │   ├── data/storage/       # localStorage implementation
│   │   └── main.kt             # JS entry point
│   └── jsMain/resources/
│       └── index.html          # App HTML shell
└── Planning.md                 # Living doc with architecture decisions
```

## 🎯 User Flows

### 1. Create Flashcards
1. Enter a topic (e.g., "Kotlin Coroutines")
2. Select number of cards (5-50)
3. Click "Generate"
4. AI creates flashcards via Koog
5. Preview and edit cards
6. Save to localStorage

### 2. Study Mode
1. Select a flashcard set from home
2. Cards are shuffled randomly
3. Tap card to flip (front ↔ back)
4. Swipe left/right to navigate
5. Track progress (e.g., "5 of 20")
6. Completion screen with restart option

## 🔧 Build Instructions

### Prerequisites
- JDK 17 or higher
- No Gradle installation needed (uses wrapper)

### Build & Run

```bash
# Build JS bundle
./gradlew :composeApp:jsBrowserProductionWebpack

# Dev server with hot reload
./gradlew :composeApp:jsBrowserDevelopmentRun

# Run tests
./gradlew :composeApp:jsTest
```

### Output
Built files will be in:
```
composeApp/build/dist/js/productionExecutable/
```

## ⚠️ Current Status

**Phase**: Development
**Target**: JS/Browser
**Status**: Core architecture complete, build optimization needed

### ✅ Completed
- [x] Project structure & Gradle setup
- [x] Domain models (Flashcard, FlashcardSet)
- [x] localStorage implementation for JS
- [x] Circuit screens & presenters for all flows
- [x] Material 3 UI for Home, Create, Study screens
- [x] Swipe gesture handling
- [x] Repository pattern
- [x] Koog AI framework integration (placeholder)

### 🚧 Known Issues

1. **Build Error**: Kotlin compiler issue with `@Serializable` on Screen classes
   - **Cause**: Kotlin 2.1.0 + Circuit 0.24.0 compatibility
   - **Solution**: Downgrade Kotlin to 2.0.x OR upgrade Circuit when available

2. **AI Generation**: Currently returns mock data
   - **Solution**: Implement actual Koog agent with OpenAI API key

### 📋 Next Steps

1. **Fix Build** (Priority: High)
   ```kotlin
   // Option 1: Update gradle/libs.versions.toml
   kotlin = "2.0.21"  // Downgrade from 2.1.0

   // Option 2: Remove @Serializable from Screen classes
   // Use custom navigation serialization
   ```

2. **Implement Real AI Generation**
   ```kotlin
   // In FlashcardGenerator.kt
   private val agent = koogAgent {
       model = OpenAI(apiKey = apiKey, model = "gpt-4-turbo")
       systemPrompt = "You are an expert flashcard creator..."
   }
   ```

3. **Add API Key Input**
   - Settings screen
   - Store in localStorage
   - Allow user to provide their own OpenAI key

4. **Deploy**
   - Build production bundle
   - Host on Netlify/Vercel/GitHub Pages
   - Configure for static SPA

## 📖 Documentation

See [Planning.md](Planning.md) for:
- Architecture decisions
- Development phases
- Technical rationale
- Future roadmap

## 🤝 Contributing

This is a 0-1 startup-style project! Contributions welcome:

1. Fork the repo
2. Create a feature branch
3. Make your changes
4. Submit a PR

## 📄 License

See [LICENSE](LICENSE) for details.

## 🙌 Acknowledgments

- **Circuit** by Slack for the excellent Compose architecture
- **Koog** by JetBrains for making AI agents in Kotlin awesome
- **Compose Multiplatform** by JetBrains

---

**Built with ❤️ using Kotlin Multiplatform**

*Questions? Check Planning.md or open an issue!*
