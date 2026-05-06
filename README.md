# OneCornerSystem

A comprehensive Android application for managing local shops and their services. This is a final year project built with modern Android development technologies.

## 📱 Project Overview

**OneCornerSystem** is an Android application designed to help shop owners manage their businesses efficiently. It provides features for:
- Shop management and location tracking
- Service/product catalog management
- Order management
- Customer interaction
- Location-based services

## 🏗️ Project Architecture

### Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Backend** | Firebase (Authentication, Firestore, Realtime Database) |
| **Architecture** | MVVM + Hilt Dependency Injection |
| **Navigation** | Jetpack Navigation Compose |
| **Image Handling** | Coil + Camera X |
| **Location Services** | Google Play Services Location |
| **Data Persistence** | DataStore Preferences |
| **Image Cropping** | Android Image Cropper |

### Build Configuration

- **Minimum SDK:** 30
- **Target SDK:** 35
- **Compile SDK:** 35
- **Java Version:** 17

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

// Navigation & DI
- androidx.navigation:navigation-compose
- androidx.hilt:hilt-navigation-compose
- com.google.dagger.hilt (v2.56.2)

// Data Management
- androidx.datastore:datastore-preferences

// UI & Media
- coil-compose (Image Loading)
- androidx.compose.material.icons.extended
- com.vanniktech:android-image-cropper (v4.7.0)

// Camera & Location
- androidx.camera (CameraX - v1.5.3)
- com.google.android.gms:play-services-location (v21.3.0)
- com.google.accompanist:accompanist-permissions (v0.37.3)

// Build Tools
- KSP (Kotlin Symbol Processing) v2.1.21-2.0.1
```

## 📂 Project Structure

```
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
  <em>📱 App screenshots </em>
</p>

## 🔄 Recent Updates & Fixes

### Service and Location Management (Latest)

#### Navigation Improvements
- Added `OrdersByShop` screen class for proper route handling
- Fixed `orders/{shopId}` navigation route
- Improved navigation graph structure

#### Shop Location Management
- Added location coordinate fields in EditShopScreen
- Shop owners can now update latitude and longitude
- Proper error handling for invalid location values
- Existing location data persists during edits

#### Comprehensive Logging
- Enhanced service creation logging in `AddServiceScreen.kt`
- Added service list state tracking in `ServiceListScreen.kt`
- Comprehensive logging in `ShopItemViewModel.kt`:
  - Service operations (creation, fetching, availability toggle)
  - Item management operations
  - Update and deletion operations
  - Refresh operations

#### Debug Logging Tags
```
Service-Related:
- ShopItemViewModel_Service
- ServiceListScreen
- ServiceCard
- EmptyServiceView
- AddServiceScreen

Shop Management:
- EditShopScreen
- ShopItemViewModel_Item
- ShopItemViewModel_Update
- ShopItemViewModel_Delete
- ShopItemViewModel_Availability
- ShopItemViewModel_Refresh
```

#### How to Debug
```bash
# Track service creation
adb logcat ShopItemViewModel_Service:D AddServiceScreen:D

# Track service display
adb logcat ServiceListScreen:D EmptyServiceView:D

# Track shop location updates
adb logcat EditShopScreen:D ShopViewModel_Update:D

# Track all service operations
adb logcat ShopItemViewModel_Service:D ServiceListScreen:D ServiceCard:D AddServiceScreen:D
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (Latest version recommended)
- Android SDK 35
- Kotlin 1.9+
- Java 17 JDK

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/It-Saurabh001/OneCornerSystem.git
   cd OneCornerSystem
   ```

2. **Open in Android Studio**
   - File → Open → Select the project directory
   - Let Gradle sync complete

3. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable required services:
     - Authentication (Google Sign-In)
     - Cloud Firestore
     - Realtime Database

4. **Build & Run**
   ```bash
   ./gradlew build
   ```
   - Connect an Android device or use emulator
   - Run from Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## 📋 Build Configuration

### Gradle Build Features

- **Parallel Building:** Enabled for faster builds
- **Configuration Cache:** Enabled for incremental builds
- **KSP (Kotlin Symbol Processing):** v2.1.21-2.0.1
- **Hilt DI:** v2.56.2
- **JVM Arguments:** -Xmx4096m for sufficient memory

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

## 🧪 Testing & Validation

### Testing Checklist
- [ ] Create a new service and verify it appears in the services list
- [ ] Check Logcat for proper log messages showing service creation flow
- [ ] Toggle service availability and verify logs show the change
- [ ] Edit shop location and verify location fields accept latitude/longitude
- [ ] Verify "no services found" message appears when list is empty
- [ ] Test error scenarios (invalid inputs) and check error logging
- [ ] Verify Orders button navigation works without errors
- [ ] Test camera and gallery image selection for services
- [ ] Verify Firebase authentication and data persistence

## 📊 Language Composition

- **Kotlin:** 810,187 bytes (100%)

## 📝 Modified Files & Recent Changes

### Latest Modifications
1. `NavGraph.kt` - Added OrdersByShop route with parameters
2. `EditShopScreen.kt` - Added location coordinate fields
3. `AddServiceScreen.kt` - Added comprehensive logging
4. `ServiceListScreen.kt` - Added state and error logging
5. `ShopItemViewModel.kt` - Enhanced logging across all operations
6. `ShopOwnerHomeScreen2.kt` - Verified navigation implementation

**Note:** All changes are backward compatible and preserve existing functionality.

## 🔐 Security & Best Practices

- ✅ Kotlin-first development
- ✅ Type-safe dependency injection with Hilt
- ✅ Secure Firebase authentication
- ✅ Proper permission handling for camera and location
- ✅ Data validation and error handling
- ✅ Comprehensive logging for debugging

## 📄 Repository Information

- **Owner:** It-Saurabh001
- **Repository:** OneCornerSystem
- **Repo ID:** 1166732934
- **Visibility:** Private
- **Created:** 25 February 2026
- **Last Updated:** 29 April 2026
- **License:** No specific license (Consider adding)
- **Default Branch:** main

## 🤝 Contributing

This is a final year project. For contributions or modifications:
1. Create a feature branch
2. Make your changes with clear commit messages
3. Test thoroughly before creating pull requests
4. Update documentation as needed

## 📞 Support & Issues

For issues, bugs, or feature requests:
- Create an issue in the GitHub repository
- Include detailed description and error logs
- Provide device information and Android version

## 📚 Documentation Files

- **CHANGES_SUMMARY.md** - Detailed summary of recent fixes and improvements
- **build_output.txt** - Complete build output log
- **build_errors.txt** - Build error tracking
- **.gitignore** - Git ignore configuration

## 🎯 Future Enhancements

Potential features for future versions:
- Customer review system
- Advanced analytics dashboard
- Multi-language support
- Offline functionality
- Push notifications
- Payment integration
- Advanced reporting features

## 📦 Release Information

- **Version:** 1.0
- **Version Code:** 1
- **Status:** Development/Testing Phase

---

**Last Updated:** 29 April 2026  
**Project Status:** Active Development  
**Author:** It-Saurabh001
