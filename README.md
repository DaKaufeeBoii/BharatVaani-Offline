# BharatVaani - Offline Indian Language Translator 🇮🇳

A beautiful, fully offline Android app for translating between Indian languages using Google's ML Kit. Supports English, Hindi, Telugu, Tamil, and Marathi with voice input/output capabilities.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)

## 🌟 Features

- **🔒 Completely Offline**: No internet required after initial model download
- **🎤 Voice Input/Output**: Speak to translate and hear translations
- **🌍 5 Indian Languages**: English, Hindi, Telugu, Tamil, Marathi
- **🔄 Bidirectional Translation**: Translate between any language pair
- **🎨 Beautiful UI**: Material Design 3 with custom theming
- **⚡ Fast & Efficient**: Optimized for mobile devices
- **📱 Modern Architecture**: MVVM with Hilt dependency injection

## 🚀 Screenshots

*[Add screenshots here after taking them from the app]*

## 📋 Requirements

- **Android**: API 24+ (Android 7.0)
- **Storage**: ~500MB free space for language models
- **Permissions**: Microphone (for voice input), Internet (for initial downloads)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Translation Engine**: Google ML Kit Translate
- **Voice Recognition**: Vosk (offline STT)
- **Text-to-Speech**: Android TTS
- **Build System**: Gradle (KTS)

## 🏗️ Project Structure

```
app/
├── src/main/java/com/kaufee/bv/
│   ├── data/
│   │   ├── manager/          # LanguageModelManager, TtsManager, SttManager
│   │   ├── repository/       # TranslationRepositoryImpl
│   │   └── model/           # Data models
│   ├── domain/
│   │   └── repository/      # TranslationRepository interface
│   ├── di/                  # Hilt dependency injection modules
│   ├── ui/
│   │   ├── components/      # Reusable Compose components
│   │   ├── splash/          # Splash screen
│   │   └── theme/           # Material Design theming
│   ├── util/                # Utilities and constants
│   ├── viewmodel/           # TranslationViewModel
│   ├── MainActivity.kt      # Main activity
│   └── BVApplication.kt     # Application class
├── src/main/assets/         # Offline STT models
└── src/main/res/           # Android resources
```

## 🚀 Getting Started

### Prerequisites

1. **Android Studio**: Arctic Fox or later
2. **JDK**: 11 or later
3. **Android SDK**: API 24+

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
   cd YOUR_REPO_NAME
   ```

2. **Open in Android Studio:**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it

3. **Build the project:**
   - Wait for Gradle sync to complete
   - Build → Make Project (Ctrl+F9)

4. **Run on device/emulator:**
   - Connect an Android device or start an emulator
   - Run → Run 'app' (Shift+F10)

### First Time Setup

1. **Launch the app** - You'll see the animated splash screen
2. **Download language models** - Tap "Download Language Models" button
3. **Grant permissions** - Allow microphone access for voice input
4. **Start translating!** - Use text input or voice commands

## 📖 Usage

### Text Translation
1. Select source and target languages using the language chips
2. Type or paste text in the input field
3. Translation appears automatically in the output field
4. Tap the copy button to copy the translation

### Voice Translation
1. Tap the microphone button to start voice input
2. Speak clearly in the source language
3. Translation will appear and be spoken automatically
4. Tap the speaker button to hear the translation again

### Language Models
- Models are downloaded once and stored locally
- Each language pair requires separate model download
- Models are cached and survive app updates
- Total storage: ~300-500MB for all languages

## 🔧 Configuration

### Supported Languages

| Language | Code | Native Name | Flag |
|----------|------|-------------|------|
| English | en | English | 🇬🇧 |
| Hindi | hi | हिन्दी | 🇮🇳 |
| Telugu | te | తెలుగు | 🇮🇳 |
| Tamil | ta | தமிழ் | 🇮🇳 |
| Marathi | mr | मराठी | 🇮🇳 |

### Build Variants

- **Debug**: Development build with logging
- **Release**: Production build with optimizations

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] App launches without crashes
- [ ] Language model download works
- [ ] Text translation functions correctly
- [ ] Voice input captures speech accurately
- [ ] Voice output speaks translations clearly
- [ ] Language switching works properly
- [ ] Error handling displays appropriate messages
- [ ] Offline mode works without internet

## 📦 Build & Release

### Debug APK
```bash
./gradlew assembleDebug
```

### Release APK
```bash
./gradlew assembleRelease
```

### Bundle (AAB)
```bash
./gradlew bundleRelease
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Kotlin coding standards
- Use meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Ensure code builds without warnings

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Google ML Kit**: For providing offline translation capabilities
- **Vosk**: For offline speech recognition models
- **Material Design**: For beautiful UI components
- **Android Jetpack**: For modern Android development tools

## 📞 Support

If you have any questions or issues:

1. Check the [Issues](https://github.com/YOUR_USERNAME/YOUR_REPO_NAME/issues) page
2. Create a new issue with detailed description
3. Include device information and steps to reproduce

## 🎯 Roadmap

- [ ] Add more Indian languages (Bengali, Gujarati, etc.)
- [ ] Improve voice recognition accuracy
- [ ] Add conversation mode for back-and-forth translation
- [ ] Implement favorite translations storage
- [ ] Add dark mode toggle
- [ ] Support for camera text recognition
- [ ] Integration with system share menu

---

**Made with ❤️ for India 🇮🇳**

*Empowering communication across Indian languages, completely offline.*
