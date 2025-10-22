# iOS App Implementation Plan for GenAI Flashcards

## 🎯 Goal
Add an iOS app target to this Kotlin Multiplatform project with Compose Multiplatform UI, matching the functionality of the existing wasmJs target.

## 📋 Current Project Structure
- **composeApp/**: Main UI module with wasmJs target
  - Entry point: `wasmJsMain/kotlin/main.kt`
  - Uses Circuit for navigation
  - Has platform-specific storage implementations
- **shared/**: Business logic module (already has wasmJs + jvm targets)
- **server/**: Backend server module

## 🧠 Architecture Understanding

### Current wasmJs Entry Point
The wasmJs app:
1. Creates platform storage (`getFlashcardStorage()`, `getConfigRepository()`)
2. Creates a `FlashcardRepository` and `KoogFlashcardGenerator`
3. Builds a Circuit with presenters and UI factories for all screens
4. Calls `ComposeViewport` with the `App()` composable

### iOS Target Strategy
For iOS, I'll follow Kotlin Multiplatform best practices:
1. Add iOS targets to `composeApp/build.gradle.kts` (iosX64, iosArm64, iosSimulatorArm64)
2. Configure framework export for iOS
3. Create `iosMain/kotlin/main.kt` with UIViewController factory
4. Create platform-specific storage implementations for iOS
5. Create an iosApp Xcode project that wraps the shared framework

## 📝 Implementation Steps

### Step 1: Configure iOS Targets in composeApp/build.gradle.kts ✅
Add the three iOS targets:
- `iosX64()` - Intel Mac simulator
- `iosArm64()` - Physical iOS devices
- `iosSimulatorArm64()` - Apple Silicon simulator

Configure framework binary with:
- baseName = "ComposeApp"
- Export to XCFramework
- Set isStatic = true for compatibility

### Step 2: Add iOS to shared module ✅
Update `shared/build.gradle.kts` to include iOS targets (may already be there).

### Step 3: Create iosMain Source Set
Path: `composeApp/src/iosMain/kotlin/`
- `main.kt` - Entry point that creates UIViewController
- `data/storage/FlashcardStorage.ios.kt` - iOS-specific storage using NSUserDefaults
- `data/storage/ConfigRepository.ios.kt` - iOS-specific config storage

### Step 4: Implement iOS Entry Point
In `iosMain/kotlin/main.kt`:
```kotlin
@Composable
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        // Same setup as wasmJs main()
        val storage = getFlashcardStorage()
        val repository = FlashcardRepository(storage)
        // ... Circuit setup
        App(configRepository, circuit)
    }
}
```

### Step 5: Create iosApp Xcode Project
Standard structure:
```
iosApp/
├── iosApp/
│   ├── ContentView.swift (calls MainViewController())
│   ├── iosApp.swift (App entry point)
│   └── Info.plist
├── Configuration/
│   └── Config.xcconfig
└── iosApp.xcodeproj
```

### Step 6: Platform-Specific Implementations
Create expect/actual declarations for:
- Storage (NSUserDefaults on iOS)
- Config Repository (NSUserDefaults on iOS)

### Step 7: Update gradle.properties & Dependencies
May need to add iOS-specific Ktor client if not already present.

### Step 8: Test Build
Run: `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`

## 🔑 Key Differences: wasmJs vs iOS

| Aspect | wasmJs | iOS |
|--------|---------|-----|
| Entry point | `fun main()` with `ComposeViewport` | `fun MainViewController()` returns `UIViewController` |
| Storage | Browser localStorage | NSUserDefaults |
| Ktor client | ktor-client-js | ktor-client-darwin |
| Deployment | Browser bundle | Xcode project → App Store |

## 📚 References
- Kotlin Multiplatform 2025 Roadmap (Stable iOS support)
- Compose Multiplatform best practices
- JetBrains official examples (compose-multiplatform repo)

## 🚀 Progress Tracking

### Completed ✅
- [x] Research KMP iOS setup best practices
- [x] Understand project structure
- [x] Create planning document
- [x] Configure iOS targets in composeApp/build.gradle.kts (iosX64, iosArm64, iosSimulatorArm64)
- [x] Configure iOS targets in shared/build.gradle.kts
- [x] Add Ktor Darwin client dependency
- [x] Create iosMain source set with main.kt entry point
- [x] Implement iOS-specific FlashcardStorage using NSUserDefaults
- [x] Implement iOS-specific ConfigRepository using NSUserDefaults
- [x] Create basic iosApp structure (Swift files)
- [x] Fix compilation errors (ExperimentalComposeUiApi)
- [x] Verify Kotlin code compiles for iOS

### Next Steps 🎯
1. **Setup Xcode** (Required on macOS)
   - Install Xcode from App Store (if not already installed)
   - Run: `sudo xcode-select --install`
   - Run: `sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer`

2. **Create Xcode Project** (Manual - easier than generating .pbxproj)
   - Open Xcode
   - File → New → Project
   - Select "iOS" → "App"
   - Product Name: "iosApp"
   - Organization Identifier: "com.simonduchastel"
   - Interface: SwiftUI
   - Save in project root as "iosApp"

3. **Configure Xcode Project**
   - Add generated framework to project:
     - Build Phases → Link Binary with Libraries
     - Add `ComposeApp.framework` from `composeApp/build/bin/iosSimulatorArm64/debugFramework/`
   - Add Run Script Phase (before Compile Sources):
     ```bash
     cd "$SRCROOT/.."
     ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
     ```

4. **Test Build**
   - Run: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
   - Open iosApp.xcodeproj in Xcode
   - Build and run on simulator

## 💭 Notes & Learnings

### 2025-10-21 Initial Research
- Compose Multiplatform for iOS is now stable (as of 2025)
- Standard approach uses three iOS targets for different architectures
- iOS app wraps the Kotlin framework as a UIViewController
- Platform-specific code uses expect/actual pattern
- Framework should be configured with XCFramework for device + simulator support

### 2025-10-21 Implementation
- ✅ Successfully configured iOS targets in both composeApp and shared modules
- ✅ Used `baseName = "ComposeApp"` and `isStatic = true` for framework
- ✅ Created iosMain/kotlin/main.kt with `MainViewController()` function using `ComposeUIViewController`
- ✅ Implemented iOS storage using NSUserDefaults (expect/actual pattern)
- ✅ Fixed ExperimentalComposeUiApi compilation errors in StudyUi.kt
- ✅ Kotlin compilation for iOS succeeds!
- ⚠️ Xcode setup required to complete framework linking (expected)
- 📝 Swift ContentView calls `MainViewControllerKt.MainViewController()` to wrap Compose UI

### Current Status
**Kotlin/Framework**: ✅ Ready (compiles successfully)
**Xcode Project**: ⚠️ Needs manual creation (or Xcode command line tools setup)
**Testing**: 🔄 Pending Xcode configuration

---

**Last Updated**: 2025-10-21
**Status**: Implementation Phase → Xcode Setup Required
