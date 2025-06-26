# Postura

**Postura** is a real-time pose detection and posture analysis Android app built with Kotlin, Jetpack Compose, CameraX, and TensorFlow Lite (MoveNet). It helps users monitor and improve their posture during workouts or daily activities by providing instant visual and textual feedback.

## 🚀 Features

- **Real-time Pose Detection**: Uses MoveNet Thunder model for fast, accurate body keypoint detection.
- **Posture Analysis**: Evaluates shoulder alignment and head position to give actionable feedback.
- **Visual Feedback**: Overlays keypoints and skeleton on the camera preview with color-coded confidence.
- **Instant Corrections**: Shows suggestions like "Level your shoulders" or "Keep your head straight".
- **Detection Tips**: In-app tips for best results (lighting, distance, etc).
- **Debug Mode**: See raw keypoint confidence and detection stats for development/testing.

## 📱 Screenshots

*Add screenshots here to showcase the UI and feedback overlays.*

## 🛠️ Tech Stack
- **Kotlin** & **Jetpack Compose** for modern Android UI
- **CameraX** for camera integration
- **TensorFlow Lite** (MoveNet Thunder) for pose estimation
- **Hilt** for dependency injection
- **StateFlow** for reactive state management

## ⚡ Getting Started

### Prerequisites
- Android Studio (latest recommended)
- Android device or emulator (with camera)

### Setup
1. **Clone the repository:**
   ```sh
   git clone https://github.com/aditya0l/Postura.git
   cd Postura
   ```
2. **Open in Android Studio**
3. **Sync Gradle** and let dependencies download
4. **Run the app** on your device or emulator

### Usage
- Grant camera permission when prompted
- Stand 2-3 feet from the camera in a well-lit area
- Watch the overlay and feedback panel for posture suggestions
- Adjust your posture based on the real-time feedback

## 🤖 How It Works
- Captures camera frames using CameraX
- Processes frames with MoveNet Thunder (TensorFlow Lite)
- Extracts 17 body keypoints (nose, eyes, shoulders, elbows, wrists, hips, knees, ankles)
- Analyzes alignment and head position for posture quality
- Displays feedback and suggestions in real time

## 💡 Contribution
Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## 📄 License
This project is [MIT](LICENSE) licensed.

---

**Postura** – Your AI-powered posture coach! 