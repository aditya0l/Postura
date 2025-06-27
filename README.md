# Postura - AI-Powered Posture Analysis App

**Postura** is a real-time pose detection and posture analysis Android application that helps users monitor and improve their posture during workouts, work sessions, or daily activities. Built with modern Android development practices, it provides instant visual and textual feedback using computer vision and machine learning.

## 🎯 Project Overview

Postura uses **TensorFlow Lite** with the **MoveNet Thunder** model to detect 17 body keypoints in real-time. The app analyzes shoulder alignment, head position, and overall posture quality, providing actionable feedback to help users maintain proper posture.

### Key Features
- **Real-time Pose Detection**: 17 body keypoints detection at 30+ FPS
- **Posture Quality Analysis**: Evaluates shoulder alignment and head position
- **Visual Feedback Overlay**: Color-coded keypoints and skeleton visualization
- **Instant Corrections**: Real-time suggestions for posture improvement
- **Detection Tips**: In-app guidance for optimal results
- **Debug Mode**: Development tools for confidence monitoring

## 🛠️ Technology Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI framework
- **CameraX** - Camera integration and lifecycle management
- **TensorFlow Lite** - On-device machine learning inference
- **MoveNet Thunder** - Pre-trained pose estimation model

### Architecture & Patterns
- **MVVM (Model-View-ViewModel)** - Clean architecture pattern
- **Repository Pattern** - Data layer abstraction
- **Dependency Injection** - Hilt for service management
- **Reactive Programming** - StateFlow for state management
- **Clean Architecture** - Separation of concerns

### Libraries & Dependencies
```kotlin
// Core Android
implementation("androidx.core:core-ktx")
implementation("androidx.lifecycle:lifecycle-runtime-ktx")
implementation("androidx.activity:activity-compose")

// Compose UI
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Camera
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.13.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.2")
implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.2")

// Dependency Injection
implementation("com.google.dagger:hilt-android:2.48")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Permissions
implementation("com.google.accompanist:accompanist-permissions:0.30.1")
```

## 🏗️ Project Architecture

### Directory Structure
```
app/src/main/java/com/example/postura/
├── data/
│   └── pose/
│       ├── PoseAnalyzer.kt      # Image analysis and pose detection
│       └── PoseDetector.kt      # TensorFlow Lite model interface
├── domain/
│   └── model/
│       ├── BodyPart.kt          # Body part enumeration
│       └── KeyPoint.kt          # Keypoint data model
├── di/
│   └── AppModule.kt             # Hilt dependency injection
├── pose/
│   └── PoseOverlay.kt           # Visual overlay components
├── uio/
│   ├── CameraPreview.kt         # Camera preview and lifecycle
│   └── CameraScreen.kt          # Permission handling
├── utils/
│   └── BitmapUtils.kt           # Image processing utilities
├── viewmodel/
│   └── PoseViewModel.kt         # State management
├── MainActivity.kt              # App entry point
└── PosturaApp.kt               # Application class
```

### Data Flow
1. **Camera Input** → CameraX captures frames
2. **Image Processing** → Convert to TensorFlow Lite format
3. **Pose Detection** → MoveNet model inference
4. **Posture Analysis** → Analyze keypoint relationships
5. **Feedback Generation** → Create posture quality assessment
6. **UI Update** → Display results via StateFlow

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Arctic Fox or later)
- **Android SDK** (API level 24+)
- **Physical Android device** with camera (recommended)
- **Git** for version control

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/aditya0l/Postura.git
   cd Postura
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned Postura directory
   - Click "OK"

3. **Sync project**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues if prompted
   - Ensure all dependencies are downloaded

4. **Configure device**
   - Connect your Android device via USB
   - Enable Developer Options and USB Debugging
   - Or set up an Android emulator with camera support

5. **Run the application**
   - Click the "Run" button (green play icon)
   - Select your target device
   - Wait for the app to install and launch

## 📱 How to Use the App

### First Launch
1. **Grant Permissions**: Allow camera access when prompted
2. **Position Yourself**: Stand 2-3 feet from the camera
3. **Ensure Good Lighting**: Well-lit environment for best detection
4. **Face the Camera**: Position yourself directly in front of the camera

### Understanding the Interface

#### Visual Elements
- **Green Dots**: High confidence keypoints (>5% confidence)
- **Yellow Dots**: Medium confidence keypoints (2-5% confidence)
- **Red Dots**: Low confidence keypoints (<2% confidence)
- **Green Lines**: Skeleton connections between keypoints
- **Status Overlay**: Top-left corner shows detection quality
- **Tips Panel**: Top-right corner with usage tips
- **Debug Panel**: Bottom-right corner with technical details
- **Feedback Panel**: Bottom-left corner with posture analysis

#### Posture Quality Indicators
- **🟢 Excellent Posture**: Shoulders level, head straight
- **🟡 Good Posture**: Minor adjustments needed
- **🔴 Poor Posture**: Significant corrections required
- **⚪ Unknown**: Insufficient detection quality

### Getting Better Results

#### Optimal Conditions
- **Distance**: 2-3 feet from camera
- **Lighting**: Bright, even lighting (avoid backlighting)
- **Background**: Plain, uncluttered background
- **Clothing**: Form-fitting clothes (avoid loose/baggy)
- **Movement**: Stay relatively still for analysis

#### Testing Different Scenarios
- **Standing**: Test basic posture detection
- **Sitting**: Analyze seated posture
- **Movement**: Test dynamic pose tracking
- **Different Angles**: Try slight variations in positioning

## 🔧 Development & Customization

### Adding New Features

#### 1. New Posture Analysis Rules
```kotlin
// In PoseAnalyzer.kt, add new analysis methods
private fun analyzeBackAlignment(keypoints: List<KeyPoint>): PostureQuality {
    // Implement your analysis logic
    return PostureQuality.GOOD
}
```

#### 2. Custom Visual Overlays
```kotlin
// In PoseOverlay.kt, add new drawing functions
private fun DrawScope.drawCustomIndicator(keypoint: KeyPoint) {
    // Your custom visualization
}
```

#### 3. Additional Feedback Types
```kotlin
// Extend PostureFeedback data class
data class PostureFeedback(
    val quality: PostureQuality,
    val message: String,
    val corrections: List<String>,
    val customMetrics: Map<String, Float> = emptyMap() // Add custom metrics
)
```

### Performance Optimization

#### 1. Frame Rate Control
```kotlin
// In PoseAnalyzer.kt, add frame skipping
private var lastProcessedFrame = 0L
private val PROCESSING_INTERVAL = 33L // ~30 FPS

override fun analyze(image: ImageProxy) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastProcessedFrame < PROCESSING_INTERVAL) {
        image.close()
        return
    }
    lastProcessedFrame = currentTime
    // ... existing analysis code
}
```

#### 2. Resolution Scaling
```kotlin
// In PoseAnalyzer.kt, add resolution options
enum class ProcessingQuality {
    LOW(128), MEDIUM(256), HIGH(512)
}
```

### Model Customization

#### 1. Different Pose Models
- Replace `movenet_thunder.tflite` with other models
- Update `PoseDetector.kt` for different output formats
- Adjust keypoint mapping in `BodyPart.kt`

#### 2. Custom Training
- Use TensorFlow Lite Model Maker
- Train on your specific use cases
- Optimize for your target devices

## 🧪 Testing

### Unit Tests
```bash
# Run unit tests
./gradlew test

# Run specific test class
./gradlew test --tests PoseAnalyzerTest
```

### Instrumented Tests
```bash
# Run on connected device
./gradlew connectedAndroidTest
```

### Performance Testing
- Monitor FPS in debug logs
- Check memory usage with Android Profiler
- Test on different device specifications

## 🐛 Troubleshooting

### Common Issues

#### 1. Black Camera Screen
- **Cause**: Camera permissions not granted
- **Solution**: Grant camera permissions in app settings

#### 2. Low Detection Confidence
- **Cause**: Poor lighting or distance
- **Solution**: Improve lighting, move closer to camera

#### 3. App Crashes
- **Cause**: Out of memory or model loading issues
- **Solution**: Check device compatibility, reduce image resolution

#### 4. Slow Performance
- **Cause**: High-resolution processing
- **Solution**: Lower processing quality, enable frame skipping

### Debug Mode
Enable debug mode to see:
- Raw keypoint coordinates and confidence scores
- Processing time and FPS metrics
- Model inference details
- Memory usage statistics

## 🤝 Contributing

### How to Contribute

1. **Fork the repository**
   ```bash
   git clone https://github.com/your-username/Postura.git
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow the existing code style
   - Add tests for new functionality
   - Update documentation

4. **Test thoroughly**
   - Run unit tests: `./gradlew test`
   - Test on multiple devices
   - Verify performance impact

5. **Submit a pull request**
   - Provide clear description of changes
   - Include screenshots if UI changes
   - Reference any related issues

### Contribution Guidelines

#### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused

#### Commit Messages
```
feat: add new posture analysis rule
fix: resolve camera permission issue
docs: update README with new features
test: add unit tests for PoseAnalyzer
refactor: improve performance of pose detection
```

#### Pull Request Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement

## Testing
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] Performance impact assessed

## Screenshots
Add screenshots for UI changes
```

### Areas for Improvement

#### High Priority
- [ ] Add more posture analysis rules (back alignment, hip position)
- [ ] Implement pose history and trends
- [ ] Add calibration mode for different body types
- [ ] Support for multiple camera angles

#### Medium Priority
- [ ] Export posture data and reports
- [ ] Integration with fitness apps
- [ ] Customizable feedback thresholds
- [ ] Offline mode improvements

#### Low Priority
- [ ] Social features and challenges
- [ ] Integration with smart devices
- [ ] Advanced analytics dashboard
- [ ] Multi-language support

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **TensorFlow Lite** team for the MoveNet model
- **Google** for CameraX and Jetpack Compose
- **Android community** for best practices and examples

## 📞 Support

- **Issues**: Report bugs and feature requests on [GitHub Issues](https://github.com/aditya0l/Postura/issues)
- **Discussions**: Join conversations on [GitHub Discussions](https://github.com/aditya0l/Postura/discussions)
- **Email**: For private inquiries, contact the maintainer

---

**Postura** - Empowering better posture through AI! 🚀

*Built with ❤️ using modern Android development practices* 