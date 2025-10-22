# iOS App for GenAI Flashcards

This directory contains the iOS app implementation using Kotlin Multiplatform and Compose Multiplatform.

## ✅ Current Status

The Kotlin multiplatform code is **ready** and compiles successfully! The following components are complete:

- ✅ iOS targets configured (iosX64, iosArm64, iosSimulatorArm64)
- ✅ Main entry point (`MainViewController()`) in `composeApp/src/iosMain/kotlin/main.kt`
- ✅ iOS-specific storage implementations using NSUserDefaults
- ✅ Swift wrapper files created (`iosApp.swift`, `ContentView.swift`)
- ✅ Kotlin code compiles for iOS

## 🚀 Next Steps: Setup Xcode

To complete the iOS app setup and run it, follow these steps:

### 1. Install Xcode (if not already installed)

```bash
# Install Xcode from the App Store, then run:
sudo xcode-select --install
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
```

### 2. Create Xcode Project

Since the Xcode project file (`.xcodeproj`) is complex to generate manually, the easiest approach is:

**Option A: Create from scratch in Xcode**
1. Open Xcode
2. File → New → Project
3. Select "iOS" → "App"
4. Configure:
   - Product Name: `iosApp`
   - Team: Your Apple Developer Team
   - Organization Identifier: `com.simonduchastel` (or your preference)
   - Interface: SwiftUI
   - Language: Swift
5. Save in this directory (overwrite existing files if prompted)

**Option B: Use Kotlin Multiplatform wizard**
Visit https://kmp.jetbrains.com/ to generate a complete project template with Xcode project included.

### 3. Configure the Xcode Project

Once you have the `.xcodeproj` file:

#### A. Add the Compose Framework

1. In Xcode, select your project in the navigator
2. Select the "iosApp" target
3. Go to "Build Phases"
4. Add a new "Run Script Phase" **BEFORE** "Compile Sources":
   ```bash
   cd "$SRCROOT/.."
   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   ```
5. Name it "Build Kotlin Framework"

#### B. Link the Framework

1. Still in "Build Phases" → "Link Binary with Libraries"
2. Click "+" and add `ComposeApp.framework`
3. The framework will be at: `composeApp/build/bin/ios*/debugFramework/ComposeApp.framework`

#### C. Configure Framework Search Paths

1. Go to "Build Settings"
2. Search for "Framework Search Paths"
3. Add: `$(SRCROOT)/../composeApp/build/bin/ios$(NATIVE_ARCH_ACTUAL)-iphonesimulator/debugFramework`

### 4. Replace Swift Files

Copy the pre-created Swift files to your Xcode project:

```bash
# From the iosApp directory
cp iosApp/iosApp.swift <your-xcode-project>/iosApp/
cp iosApp/ContentView.swift <your-xcode-project>/iosApp/
```

Or manually update them to match the files in `iosApp/iosApp/`.

### 5. Build and Run

```bash
# Build the Kotlin framework first
cd ..
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Then open in Xcode
open iosApp/iosApp.xcodeproj
```

In Xcode:
1. Select an iOS simulator (e.g., iPhone 15 Pro)
2. Click the "Run" button (▶️)
3. The app should build and launch! 🎉

## 📁 Project Structure

```
iosApp/
├── iosApp/
│   ├── iosApp.swift           # Main app entry point
│   ├── ContentView.swift      # SwiftUI view wrapping Compose
│   └── Info.plist             # iOS app configuration
├── Configuration/
│   └── Config.xcconfig         # Build configuration
└── README.md                   # This file
```

## 🔧 How It Works

1. **Kotlin Code**: The `MainViewController()` function in `composeApp/src/iosMain/kotlin/main.kt` creates a UIViewController containing the entire Compose UI
2. **Swift Wrapper**: `ContentView.swift` calls `MainViewControllerKt.MainViewController()` to get the Compose view controller
3. **SwiftUI Integration**: The view controller is wrapped in a `UIViewControllerRepresentable` to integrate with SwiftUI

## 🐛 Troubleshooting

### "Module 'ComposeApp' not found"
- Make sure you've built the Kotlin framework first: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- Check that the framework search paths are configured correctly in Xcode

### "No such module 'ComposeApp'"
- Clean build folder in Xcode: Product → Clean Build Folder
- Rebuild the Kotlin framework

### "xcrun: error: unable to find utility"
- Make sure Xcode command line tools are installed: `xcode-select --install`

## 📚 Additional Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Official KMP Wizard](https://kmp.jetbrains.com/)
