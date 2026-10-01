# Harmes Agent — Autonomous On-Device AI Platform for Android

**Harmes Agent** is an on-device-first hybrid AI agent and intelligent operating assistant built with modern **Jetpack Compose**, **Kotlin**, **Room Database**, and **Material 3**.

---

## 🚀 Quick Start: Pushing to GitHub & Running on Android Mobile

### Step 1: Push from Google AI Studio to GitHub

You have two easy ways to push this project to GitHub:

#### Method A: Using AI Studio Direct Export (One-Click)
1. In the Google AI Studio top-right or settings navigation header, click **Export / Settings**.
2. Select **Push to GitHub** (or **Download as ZIP**).
3. If prompted, authorize your GitHub account and choose your repository name (e.g., `harmes-android-agent`).
4. Click **Confirm Push**. Your complete codebase, resources, and Gradle build files are now on GitHub!

#### Method B: Using Local Git CLI (If Downloaded as ZIP)
```bash
# 1. Unzip the downloaded project and enter directory
cd harmes-android-agent

# 2. Initialize git repository
git init
git add .
git commit -m "Initial commit: Harmes Agent Android application"

# 3. Link to your GitHub repository and push
git branch -M main
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPOSITORY>.git
git push -u origin main
```

---

### Step 2: Open and Run in Android Studio

1. **Install Android Studio**: Download [Android Studio Ladybug or newer](https://developer.android.com/studio).
2. **Clone your repository**:
   ```bash
   git clone https://github.com/<YOUR_USERNAME>/<YOUR_REPOSITORY>.git
   cd <YOUR_REPOSITORY>
   ```
3. In Android Studio, click **File > Open** and select the root project folder.
4. Let Gradle sync project dependencies automatically.

---

### Step 3: Build the APK for Your Android Mobile Phone

You can generate the installer APK either inside Android Studio or from the command line:

#### Option 1: Command Line (Fastest)
Run the Gradle assemble command:
- **macOS / Linux**:
  ```bash
  gradle assembleDebug
  ```
  *(or `./gradlew assembleDebug` once wrapper script is present)*
- **Windows**:
  ```cmd
  gradle assembleDebug
  ```
The compiled APK file will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

#### Option 2: Android Studio GUI
1. In the top menu, go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. When the build finishes, click the **"locate"** link in the popup balloon to find `app-debug.apk`.

---

### Step 4: Install the APK on Your Android Phone

1. **Send the APK to your phone**:
   - Transfer via USB cable.
   - Or upload `app-debug.apk` to Google Drive / Telegram / WhatsApp and download it directly on your phone.
   - Or install via ADB:
     ```bash
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```
2. **Open the APK on your phone**:
   - Tap the downloaded file.
   - If prompted with *"Install unknown apps"*, tap **Settings** and toggle **"Allow from this source"**.
   - Tap **Install**.
3. Open **Harmes** from your home screen or app drawer!

---

## 🛠️ Architecture & System Capabilities

- **Hybrid Model Router**: Seamlessly arbitrates between local on-device quantized models (NPU/NNAPI, GPU, CPU) and high-capacity cloud endpoints (Gemini API) with dynamic fallback.
- **Autonomous Planner & Execution Engine**: Breaks down complex user objectives into structured milestones with progress tracking and interactive milestone completion.
- **4-Layer Memory Vault**: Working, Episodic, Semantic, and Knowledge context stored securely on-device with Room Database and cosine vector similarity search.
- **Zero-Trust Security Gate**: Tools and device features are governed with risk levels (`LOW`, `MEDIUM`, `HIGH`) and explicit confirmation dialogs for sensitive device actions.
- **Hardware Telemetry Monitor**: Live CPU core inspection, RAM consumption, battery temperature sensor, thermal status, and NNAPI detection.
- **Vision & Optical Intelligence**: CameraX optical scanner and Android Photo Picker for document OCR and chart parsing.
- **Voice Agent**: Integrated speech recognition and audio text-to-speech output.

---

## 📋 Requirements
- **Android Target**: Android 16 (API 36)
- **Minimum SDK**: Android 8.0 (API 26)
- **Kotlin**: 2.2.10
- **UI Framework**: Jetpack Compose (Material 3)
