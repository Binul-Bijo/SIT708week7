# Lost & Found App

## Student Information
- Name: Binul Bijo  
- Unit: SIT708 Mobile Systems Development  
- Task: Pass Task 7.1 – Lost & Found App  

---

# Project Overview

The Lost & Found App is a mobile app designed for Android devices that utilizes Kotlin Jetpack Compose and SQLite databases.

The intended function of this app is to enable the reporting of lost and found items and reunite them with their owners. This is done through the creation of advertisements for lost and found items, uploading pictures, categorizing items, showing item information, and deleting advertisements after the recovery of lost items.

---

# Features

- Create Lost or Found advertisements
- Upload item images
- Store data using SQLite database
- Filter items by category
- View all saved advertisements
- View detailed item information
- Remove advertisements
- Warning messages using Toast notifications
- Date-based sorting
- Timestamp for each post

---

# Technologies Used

- Kotlin
- Jetpack Compose
- SQLite Database
- Android Studio
- Coil Image Library

---

# Categories Included

- Electronics
- Pets
- Wallets
- Documents
- Keys
- Other

---

# Project Structure

- MainActivity.kt → Main application logic and UI screens
- DatabaseHelper → SQLite database operations
- LostFoundItem → Data model class
- AndroidManifest.xml → Permissions and app configuration

---

# How the App Works

1. User opens the application.
2. User selects:
   - Create New Advert
   - Show Lost & Found Items
3. User enters:
   - Item type
   - Name
   - Phone number
   - Description
   - Date
   - Location
   - Category
   - Upload image
4. Data is stored in SQLite database.
5. Users can filter items by category.
6. Users can view item details and remove advertisements.

---

# Future Improvements

Future improvements may include:
- Firebase Authentication
- Cloud database integration
- Google Maps support
- Push notifications
- AI-based item matching
- Real-time multi-user support

---

# How to Run the Project

1. Open the project in Android Studio.
2. Sync Gradle files.
3. Run the app on:
   - Android Emulator
   - Physical Android Device
4. Grant image permission if requested.

---

# Dependencies

Add this dependency in build.gradle.kts:

```kotlin
implementation("io.coil-kt:coil-compose:2.6.0")
```

---

# Permission Used

Add this permission in AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

---

# Demonstration

The app demonstrates:
- SQLite CRUD operations
- Jetpack Compose UI
- Image upload functionality
- Category filtering
- Dynamic item listing

---

# Author

Binul Bijo  
Deakin University  
SIT708 Mobile Application Development
