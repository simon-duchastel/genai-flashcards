#!/bin/bash

# Build script for iOS framework
# Usage: ./buildIos.sh [simulator|device|all]

set -e

BUILD_TYPE="${1:-simulator}"

echo "🚀 Building GenAI Flashcards for iOS..."
echo ""

case $BUILD_TYPE in
  simulator)
    echo "📱 Building for iOS Simulator (Apple Silicon)..."
    ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
    echo ""
    echo "✅ Framework built successfully!"
    echo "   Location: composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework"
    ;;

  intel-sim)
    echo "📱 Building for iOS Simulator (Intel)..."
    ./gradlew :composeApp:linkDebugFrameworkIosX64
    echo ""
    echo "✅ Framework built successfully!"
    echo "   Location: composeApp/build/bin/iosX64/debugFramework/ComposeApp.framework"
    ;;

  device)
    echo "📱 Building for iOS Device..."
    ./gradlew :composeApp:linkDebugFrameworkIosArm64
    echo ""
    echo "✅ Framework built successfully!"
    echo "   Location: composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework"
    ;;

  all)
    echo "📱 Building for all iOS targets..."
    ./gradlew :composeApp:linkDebugFrameworkIos
    echo ""
    echo "✅ All frameworks built successfully!"
    ;;

  *)
    echo "❌ Unknown build type: $BUILD_TYPE"
    echo ""
    echo "Usage: ./buildIos.sh [simulator|intel-sim|device|all]"
    echo ""
    echo "Options:"
    echo "  simulator   - Build for iOS Simulator (Apple Silicon) [default]"
    echo "  intel-sim   - Build for iOS Simulator (Intel Mac)"
    echo "  device      - Build for physical iOS device"
    echo "  all         - Build for all iOS targets"
    exit 1
    ;;
esac

echo ""
echo "🎉 Next steps:"
echo "   1. Open iosApp.xcodeproj in Xcode"
echo "   2. Select a simulator/device"
echo "   3. Click Run (⌘R)"
echo ""
