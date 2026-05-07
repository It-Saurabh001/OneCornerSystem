# OneCornerSystem

A comprehensive Android application for managing local shops and their services. This is a final year project built with modern Android development technologies. The app enables users to discover services, book appointments, and communicate with service providers through an integrated chat system.

---

## 📱 Project Overview

**OneCornerSystem** is an Android application designed to help shop owners manage their businesses efficiently.

The application provides features for:

- Shop management and location tracking
- Service and product catalog management
- Order management
- Customer interaction
- Real-time chat and messaging
- Location-based services

---

## 🏗️ Project Architecture

### Tech Stack

| Component | Technology |
|------------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Backend** | Firebase (Authentication, Firestore, Realtime Database) |
| **Architecture** | MVVM + Hilt Dependency Injection |
| **Navigation** | Jetpack Navigation Compose |
| **Image Handling** | Coil + CameraX |
| **Location Services** | Google Play Services Location |
| **Data Persistence** | DataStore Preferences |
| **Image Cropping** | Android Image Cropper |

---

### Build Configuration

- **Minimum SDK:** 30
- **Target SDK:** 35
- **Compile SDK:** 35
- **Java Version:** 17

---

## 🛠️ Technology Stack Details

### Core Dependencies

```kotlin
// Jetpack Components
- androidx.core:core-ktx
- androidx.lifecycle:lifecycle-runtime-ktx
- androidx.activity:activity-compose
- androidx.compose.material3
- androidx.credentials & Google ID

// Firebase
- firebase-auth
- firebase-database
- firebase-firestore

// Navigation & Dependency Injection
- androidx.navigation:navigation-compose
- androidx.hilt:hilt-navigation-compose
- com.google.dagger.hilt (v2.56.2)

// Data Management
- androidx.datastore:datastore-preferences

// UI & Media
- coil-compose
- androidx.compose.material.icons.extended
- com.vanniktech:android-image-cropper (v4.7.0)

// Camera & Location
- androidx.camera (CameraX - v1.5.3)
- com.google.android.gms:play-services-location (v21.3.0)
- com.google.accompanist:accompanist-permissions (v0.37.3)

// Build Tools
- KSP (Kotlin Symbol Processing) v2.1.21-2.0.1
```

---

## 📂 Project Structure

```text
OneCornerSystem/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/com/saurabh/onecornersystem/
│   │       └── res/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── CHANGES_SUMMARY.md
├── build_output.txt
├── build_errors.txt
└── README.md
```

---

## ✨ Key Features

### Shop Management
- ✅ Create and edit shop profiles
- ✅ Location management with latitude/longitude coordinates
- ✅ Shop information display and updates

### Service Management
- ✅ Add services/products with descriptions
- ✅ Image attachment for services (camera & gallery)
- ✅ Service listing and availability toggle
- ✅ Service deletion functionality

### Order Management
- ✅ View orders by shop
- ✅ Order status tracking
- ✅ Order filtering and display

### Authentication & Security
- ✅ Firebase Authentication integration
- ✅ Google Sign-In support
- ✅ Persistent login using DataStore
- ✅ Splash screen support

### Location Features
- ✅ GPS-based location tracking
- ✅ Google Play Services Location integration
- ✅ Shop location coordinates storage

### User Interface
- ✅ Modern Jetpack Compose UI
- ✅ Material 3 Design System
- ✅ Extended Material Icons
- ✅ Responsive layouts

### Chat & Messaging
- ✅ Real-time chat between customers and shop owners
- ✅ Firestore real-time listeners for live updates
- ✅ Per-booking chat rooms
- ✅ Customer and shop chat lists
- ✅ Server-side timestamped messaging
- ✅ Unread message counters
- ✅ Batch mark-as-read functionality
- ✅ Optimized listener lifecycle management
- ✅ Robust logging and error handling

---

## 📸 Screenshots

<div align="center">

<br/>

<img src="https://github.com/user-attachments/assets/113eb11b-0e18-48fb-b8c2-a048c0dad638" width="145" />
<img src="https://github.com/user-attachments/assets/472822cc-5f9e-4946-949e-b9e1ab47d743" width="145" />
<img src="https://github.com/user-attachments/assets/9ccd7a24-d385-45a7-bc0b-9db1ff5ab39f" width="145" />
<img src="https://github.com/user-attachments/assets/592e3e1d-a6ef-4817-bd1f-4ac4af2e0d25" width="145" />

<br/>

<img src="https://github.com/user-attachments/assets/589650eb-42ea-4930-9c4c-d1c9de43f0ee" width="145" />
<img src="https://github.com/user-attachments/assets/9ee74b94-c054-419b-8ac6-bbdd664313ad" width="145" />
<img src="https://github.com/user-attachments/assets/39580df1-66fb-401c-9c56-68cb72cfd1d7" width="145" />
<img src="https://github.com/user-attachments/assets/8121bad9-e77a-455e-b30e-f0d01a1eef27" width="145" />

<br/>

<img src="https://github.com/user-attachments/assets/8f086565-a3c6-4717-adf2-a56d64c38298" width="145" />
<img src="https://github.com/user-attachments/assets/39b9fac9-bf01-4bb8-a582-c169043257c8" width="145" />
<img src="https://github.com/user-attachments/assets/c35f3721-fcb3-42d1-9745-46a18fff0864" width="145" />
<img src="https://github.com/user-attachments/assets/1caf731d-1b02-434e-9f90-38ab681e6e32" width="145" />

</div>

<p align="center">
  <em>📱 Application Screenshots</em>
</p>

---

## 🪵 Debug Logging Tags

### Service Related

```text
- ShopItemViewModel_Service
- ServiceListScreen
- ServiceCard
- EmptyServiceView
- AddServiceScreen
```

### Shop Management

```text
- EditShopScreen
- ShopItemViewModel_Item
- ShopItemViewModel_Update
- ShopItemViewModel_Delete
- ShopItemViewModel_Availability
- ShopItemViewModel_Refresh
```

### Chat Related

```text
- ChatViewModel
- ChatRepository
- ShopChatScreen
- CustomerChatScreen
- ChatMessagesList
- ChatInputBar
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Latest Version Recommended)
- Android SDK 35
- Kotlin 1.9+
- Java 17 JDK

---

### Installation & Setup

#### 1. Clone Repository

```bash
git clone https://github.com/It-Saurabh001/OneCornerSystem.git
cd OneCornerSystem
```

#### 2. Open in Android Studio

- File → Open → Select project directory
- Allow Gradle sync to complete

#### 3. Configure Firebase

- Create Firebase project
- Download `google-services.json`
- Place it inside `app/`
- Enable:
  - Firebase Authentication
  - Cloud Firestore
  - Realtime Database

#### 4. Build & Run

```bash
./gradlew build
```

Install debug APK:

```bash
./gradlew installDebug
```

---

## 📋 Build Configuration

### Gradle Features

- ✅ Parallel Building
- ✅ Configuration Cache
- ✅ KSP Enabled
- ✅ Hilt Dependency Injection
- ✅ Optimized JVM Memory Allocation

---

### Gradle Properties

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configuration-cache=true
android.useAndroidX=true
android.nonTransitiveRClass=true
ksp.useKSP2=true
```

---

## 🧪 Testing & Validation

### Service Management
- [ ] Create new services
- [ ] Verify service list updates
- [ ] Toggle service availability
- [ ] Delete services
- [ ] Test image uploads

### Shop Management
- [ ] Edit shop details
- [ ] Update coordinates
- [ ] Validate incorrect inputs
- [ ] Verify persistence after refresh

### Navigation
- [ ] Verify Orders navigation
- [ ] Validate navigation arguments
- [ ] Test back-stack handling

### Authentication
- [ ] Firebase login/logout
- [ ] Google Sign-In flow
- [ ] Persistent login validation

### Chat System
- [ ] Start shop chat
- [ ] Start booking-based chat
- [ ] Verify chat reuse
- [ ] Send/receive real-time messages
- [ ] Verify unread count synchronization
- [ ] Validate mark-as-read functionality
- [ ] Ensure listeners do not duplicate

---

## 📊 Language Composition

- **Kotlin:** 810,187 bytes (100%)

---

## 🔐 Security & Best Practices

- ✅ Kotlin-first development
- ✅ Type-safe dependency injection with Hilt
- ✅ Secure Firebase authentication
- ✅ Proper permission handling
- ✅ Data validation and error handling
- ✅ Comprehensive debugging logs

---

## 🤝 Contributing

For contributions and modifications:

1. Create a feature branch
2. Make changes with meaningful commit messages
3. Test thoroughly before pushing
4. Update documentation when required

---

## 📞 Support & Issues

For bugs, issues, or feature requests:

- Create an issue in the repository
- Attach logs/screenshots
- Mention Android version and device details

---

## 🎯 Future Enhancements

Planned future improvements:

- Customer review system
- Analytics dashboard
- Multi-language support
- Offline functionality
- Push notifications
- Payment integration
- Advanced reporting

---

## 📦 Release Information

| Property | Value |
|----------|--------|
| **Version** | 1.0 |
| **Version Code** | 1 |
| **Status** | Development / Testing Phase |

---

**Last Updated:** 07 May 2026  
**Project Status:** Active Development  
**Author:** It-Saurabh001
