# CabShare - Real-time Ride Sharing App

CabShare is a modern Android application designed to help users find and share cab rides with others heading to the same destination. It features real-time chat, location-based trip matching, and profile management.

## 🚀 Features
- **Trip Matching**: Finds nearby users with similar pickup and destination points.
- **Real-time Chat**: Connect with potential travel partners instantly.
- **Location Integration**: Uses Google Maps for precise location selection.
- **Profile Management**: Customize your profile with a bio and photo.
- **Notifications**: Get alerted for new matches, messages, and trip updates.
- **Help & Support**: Integrated FAQ and ticket submission system.

## 🛠 Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Messaging**: Firebase Cloud Messaging (FCM)
- **Maps**: Google Maps SDK & Places API
- **UI Components**: Material Design 3, ViewBinding, Glide (Image Loading)

## 📦 Getting Started
To run this project locally, follow these steps:

### 1. Prerequisites
- Android Studio Hedgehog or newer
- A Google Maps API Key
- A Firebase Project

### 2. Setup Configuration
Because this is a public repository, sensitive keys are hidden. You must provide your own:

**A. Google Maps API Key**
Create a `local.properties` file in your root directory (if not present) and add:
```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_KEY_HERE
```

**B. Firebase Configuration**
1. Create a project on the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with the package name `com.example.cabshare`.
3. Download the `google-services.json` file.
4. Place it in the `app/` directory of the project.

### 3. Build & Run
1. Sync the project with Gradle files.
2. Ensure you have the required Firestore indexes (links provided in Android Studio Logcat on first run).
3. Run the app on an emulator or a physical device.

## 🤝 Contributing
Contributions are welcome! Please fork the repository and create a pull request.

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
