#!/bin/bash

################################################################################
# buildWeb.sh - WasmJS Website Build Script
#
# Purpose: Build the GenAI Flashcards wasmJs website with automatic Java
#          installation on Linux systems that don't have it.
#
# Usage: ./buildWeb.sh [options]
# Options:
#   --clean         Clean build directories before building
#   --skip-java     Skip Java installation check
#   --output-dir    Custom output directory (default: ./dist)
#   --help          Show this help message
################################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

# Color codes for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Configuration
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly BUILD_START_TIME=$(date +%s)
readonly REQUIRED_JAVA_VERSION=17
OUTPUT_DIR="${OUTPUT_DIR:-${SCRIPT_DIR}/dist}"
CLEAN_BUILD=false
SKIP_JAVA_CHECK=false

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Parse command line arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --clean)
                CLEAN_BUILD=true
                shift
                ;;
            --skip-java)
                SKIP_JAVA_CHECK=true
                shift
                ;;
            --output-dir)
                OUTPUT_DIR="$2"
                shift 2
                ;;
            --help)
                echo "Usage: $0 [options]"
                echo "Options:"
                echo "  --clean         Clean build directories before building"
                echo "  --skip-java     Skip Java installation check"
                echo "  --output-dir    Custom output directory (default: ./dist)"
                echo "  --help          Show this help message"
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
}

# Detect Linux distribution
detect_linux_distro() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        echo "$ID"
    elif [ -f /etc/redhat-release ]; then
        echo "rhel"
    elif [ -f /etc/debian_version ]; then
        echo "debian"
    else
        echo "unknown"
    fi
}

# Check if Java is installed and meets version requirements
check_java() {
    if $SKIP_JAVA_CHECK; then
        log_warn "Skipping Java installation check"
        return 0
    fi

    log_info "Checking Java installation..."

    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')

        if [ "$java_version" -ge "$REQUIRED_JAVA_VERSION" ]; then
            log_success "Java $java_version is already installed (required: Java $REQUIRED_JAVA_VERSION)"
            return 0
        else
            log_warn "Java $java_version found, but Java $REQUIRED_JAVA_VERSION or higher is required"
        fi
    else
        log_warn "Java is not installed"
    fi

    return 1
}

# Install Java based on Linux distribution
install_java() {
    local distro=$(detect_linux_distro)

    log_info "Detected Linux distribution: $distro"
    log_info "Installing OpenJDK $REQUIRED_JAVA_VERSION..."

    case $distro in
        ubuntu|debian)
            log_info "Using apt package manager..."
            sudo apt-get update -qq
            sudo apt-get install -y openjdk-${REQUIRED_JAVA_VERSION}-jdk
            ;;
        fedora|rhel|centos)
            log_info "Using yum/dnf package manager..."
            if command -v dnf &> /dev/null; then
                sudo dnf install -y java-${REQUIRED_JAVA_VERSION}-openjdk-devel
            else
                sudo yum install -y java-${REQUIRED_JAVA_VERSION}-openjdk-devel
            fi
            ;;
        arch|manjaro)
            log_info "Using pacman package manager..."
            sudo pacman -Sy --noconfirm jdk${REQUIRED_JAVA_VERSION}-openjdk
            ;;
        alpine)
            log_info "Using apk package manager..."
            sudo apk add --no-cache openjdk${REQUIRED_JAVA_VERSION}
            ;;
        *)
            log_error "Unsupported Linux distribution: $distro"
            log_error "Please install OpenJDK $REQUIRED_JAVA_VERSION manually"
            exit 1
            ;;
    esac

    # Verify installation
    if command -v java &> /dev/null; then
        local installed_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        log_success "Java installed successfully: $installed_version"
    else
        log_error "Java installation failed"
        exit 1
    fi
}

# Ensure Java is available
ensure_java() {
    if ! check_java; then
        if [[ "$OSTYPE" == "linux-gnu"* ]]; then
            install_java
        elif [[ "$OSTYPE" == "darwin"* ]]; then
            log_error "Java $REQUIRED_JAVA_VERSION is required but not installed"
            log_error "On macOS, install Java using Homebrew:"
            log_error "  brew install openjdk@${REQUIRED_JAVA_VERSION}"
            exit 1
        else
            log_error "Java $REQUIRED_JAVA_VERSION is required but not installed"
            log_error "Please install Java manually"
            exit 1
        fi
    fi
}

# Clean build directories
clean_build() {
    if $CLEAN_BUILD; then
        log_info "Cleaning build directories..."
        ./gradlew clean
        log_success "Build directories cleaned"
    fi
}

# Build the wasmJs target
build_wasmjs() {
    log_info "Building wasmJs target..."

    # Ensure Gradle wrapper is executable
    chmod +x gradlew

    # Run the build
    ./gradlew :composeApp:wasmJsBrowserProductionWebpack

    if [ $? -eq 0 ]; then
        log_success "Build completed successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Verify build output exists
verify_build_output() {
    local build_output="${SCRIPT_DIR}/composeApp/build/dist/wasmJs/productionExecutable"

    log_info "Verifying build output..."

    if [ ! -d "$build_output" ]; then
        log_error "Build output directory not found: $build_output"
        exit 1
    fi

    local required_files=("index.html" "composeApp.js")
    for file in "${required_files[@]}"; do
        if [ ! -f "${build_output}/${file}" ]; then
            log_error "Required file not found: $file"
            exit 1
        fi
    done

    log_success "Build output verified"
}

# Copy build artifacts to output directory
copy_artifacts() {
    log_info "Copying build artifacts to $OUTPUT_DIR..."

    local build_output="${SCRIPT_DIR}/composeApp/build/dist/wasmJs/productionExecutable"

    # Create output directory if it doesn't exist
    mkdir -p "$OUTPUT_DIR"

    # Copy all files
    cp -r "$build_output"/* "$OUTPUT_DIR/"

    # Generate checksums for verification
    log_info "Generating checksums..."
    if command -v sha256sum &> /dev/null; then
        (cd "$OUTPUT_DIR" && find . -type f -exec sha256sum {} \; > checksums.txt)
    elif command -v shasum &> /dev/null; then
        (cd "$OUTPUT_DIR" && find . -type f -exec shasum -a 256 {} \; > checksums.txt)
    fi

    log_success "Artifacts copied to $OUTPUT_DIR"
}

# Display build summary
display_summary() {
    local build_end_time=$(date +%s)
    local build_duration=$((build_end_time - BUILD_START_TIME))

    echo ""
    echo "================================================================="
    log_success "BUILD COMPLETE"
    echo "================================================================="
    echo ""
    echo "Build Duration: ${build_duration}s"
    echo "Output Directory: $OUTPUT_DIR"
    echo ""
    echo "Build artifacts:"
    ls -lh "$OUTPUT_DIR" | grep -v "^total" | awk '{print "  - " $9 " (" $5 ")"}'
    echo ""
    echo "To deploy, serve the contents of: $OUTPUT_DIR"
    echo "Example: python3 -m http.server 8000 --directory $OUTPUT_DIR"
    echo ""
    echo "================================================================="
}

# Main execution
main() {
    log_info "Starting wasmJs build process..."

    parse_arguments "$@"
    ensure_java
    clean_build
    build_wasmjs
    verify_build_output
    copy_artifacts
    display_summary
}

# Run main function with all arguments
main "$@"
