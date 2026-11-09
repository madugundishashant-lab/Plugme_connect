# Plugme Connect 🫧⚡

**Plugme Connect** is a native Android digital business card and profile sharing application built with Kotlin, Jetpack Compose, and Material Design 3. It allows users to share their entire digital identity (social networks, gamer tags, personal info, website links) via one beautiful, high-resolution QR code or deep link.

---

## 🚀 Key Features

1. **Vault (Default Screen)**:
   - Gradient hero banner with `{n} Plugs` pill
   - Real-time debounced search (180ms) preserving focus and scroll state
   - 4 Filter chips: **All**, **Recent**, **Gaming**, and **Social**
   - 3 Stat bubbles displaying live count of Plugs, Games, and Social links
   - Featured row of circular star connections (for top contacts)
   - 3-column responsive grid of glassmorphic **BubbleCards** with:
     - 2-letter uppercase initials on gradient avatar
     - Online status indicator dot (every 3rd card)
     - Platform emoji strip with `+N` badge
     - 6 pastel cycle card color accents
     - Floating subtle staggered animation
   - Empty states for zero contacts and search queries

2. **Contact Detail**:
   - High-res avatar bubble, `@username`, age, gender, email
   - Interactive platform link chips opening directly via `UriHandler`
   - Gamer tags list with one-tap copy to clipboard
   - Edit contact action & Remove contact with double confirmation

3. **Edit / Add Contact**:
   - Required fields validation (Full Name, Username)
   - Age clamping (1–150)
   - Real-time save and vault update

4. **My QR Code & Camera Scanner**:
   - 240.dp ZXing QR code rendered off the main thread (`Dispatchers.Default`)
   - Foreground `#00AAFF` (Cyan) and crisp contrast
   - Quick action buttons: **⚡ Share**, **📷 Scan**, and **🔄 Refresh**
   - CameraX + ML Kit QR code analyzer with runtime camera permission handling
   - Automatic deduplication by username/name on scan with instant vault addition and feedback snackbars

5. **Links & Gaming Management**:
   - Dropdown platform catalog:
     - 6 Free platforms: Instagram, Snapchat, X (Twitter), Telegram, Facebook, LinkedIn
     - 19 Pro platforms: YouTube, Discord, TikTok, GitHub, Spotify, Twitch, Reddit, Pinterest, Threads, WhatsApp, Signal, Behance, Dribbble, Medium, Patreon, Website, Email, Phone, Custom Link
   - Gaming tags editor for Valorant, Fortnite, Roblox, Minecraft, etc.
   - Limit enforcement:
     - Free plan: up to 6 social links and 5 game entries
     - Pro plan: up to 25 social links and 15 game entries
   - Limit notice banner when free limit is reached

6. **Profile (Me)**:
   - Full Name, Username, Email, Age, Gender
   - Non-destructive merging (preserves existing social & gaming connections)

7. **Settings & Setup**:
   - Version, Plan, and Local-First Storage card
   - Manage Account & Unlock Pro
   - Pro-only **Sync Now** to push data to Firebase Realtime Database
   - **Show Intro Again** to replay onboarding tour
   - **Wipe All Data** with 2-step confirmation (requires typing "RESET")

8. **Auth & Pro Sync**:
   - Email & Password authentication (minimum 6 characters)
   - Real-time error code mapping for friendly user alerts
   - Pro perks overview and sign-out controls

9. **Welcome Onboarding Overlay**:
   - 🫧 First-launch animated modal showing 3 quick steps to get started
   - Can be reopened at any time from Settings

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (BOM 2024.09.00) + Material Design 3
- **Navigation**: Jetpack Navigation Compose
- **Architecture**: MVVM with `ViewModel`, `StateFlow`, and `PlugmeRepository`
- **Local Persistence**: Jetpack Preferences DataStore with `kotlinx.serialization`
- **QR Generation**: ZXing Core (`com.google.zxing:core:3.5.3`) with `ProfileQrGenerator`
- **Barcode Scanner**: Google ML Kit Barcode Scanning + CameraX (Camera2, Lifecycle, View)
- **Cloud Backend**: Firebase Realtime Database + Firebase Authentication
- **Typography**: Local static Google Fonts bundled into resources:
  - `Syne` (Bold / ExtraBold) for titles and numbers
  - `DM Sans` for body text

---

## 🔲 Profile QR Code Generation Utility (`ProfileQrGenerator`)

The `ProfileQrGenerator` utility (`com.example.qr.ProfileQrGenerator`) converts user profile information into scannable, shareable QR codes and digital business cards using the ZXing library:

### Features & Capabilities:
- **ZXing MultiFormatWriter Integration**: Encodes profile data using `BarcodeFormat.QR_CODE` with high-error-correction (`ErrorCorrectionLevel.H` - 30% recovery capability).
- **Embedded Center Avatar Badge**: Smoothly draws the user's initials avatar inside a protective ring in the center of the QR code without compromising scanning reliability.
- **Customizable Styling (`QrConfig`)**: Configurable dimensions, foreground/background colors (defaulting to Plugme Cyan `#00AAFF` and pure white), and margin sizes.
- **Full Digital Card Generation (`generateShareableCardBitmap`)**: Generates an export-ready business card containing:
  - Top header banner
  - User initials avatar bubble, display name, and handle
  - Centered high-contrast ZXing QR code
  - Platform indicators (e.g. 📷 𝕏 💼 👾)
  - Clear scanning instructions footer
- **Android Intent & FileProvider Sharing (`createShareIntent`)**: Compresses the QR card into a cached PNG file, acquires a content URI via `FileProvider`, and builds an `ACTION_SEND` intent with read permissions for seamless sharing to messaging, social, or email apps.
- **RFC 4648 URL-safe Base64 Serialization**: Zero external dependencies on platform Base64 implementations, ensuring 100% deterministic encoding/decoding on both Android devices and JVM unit test runners.

---

## 📁 Project Setup & `google-services.json`

### 1. Offline & Local-First Mode (Works out-of-the-box!)
The app is built **local-first**. Core functionality (Vault, Contacts, Profile, QR generation, QR scanning, Links) works **100% offline without network or Firebase configuration**.

In `app/build.gradle.kts`:
```kotlin
googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }
```
This ensures the project **builds and runs cleanly** even if `google-services.json` is not initially present.

### 2. Setting Up Firebase Pro Sync
To enable cloud backup and Pro account features:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create or select a project.
3. Add an Android App with package name:
   ```
   com.aistudio.plugme.cnzvr
   ```
4. Enable **Authentication** with Email/Password sign-in provider.
5. Enable **Realtime Database** (read/write rules under `users/{uid}`).
6. Download `google-services.json` and place it in the `/app` module directory:
   ```
   /app/google-services.json
   ```
7. Rebuild the project.

---

## 🔐 Permissions

The app declares only the strictly required permissions in `AndroidManifest.xml`:
- `android.permission.INTERNET`: Used for optional Firebase sync, sharing, and external link navigation.
- `android.permission.CAMERA`: Handled via runtime permission launcher only when the user taps **📷 Scan** to scan a physical QR code. If denied, a helpful message is displayed without disrupting the app.

---

## 🔗 Deep Link Specification

Plugme Connect supports instant profile sharing via URL-safe Base64 encoded query parameters:
```
https://plugmeconnect.app/?d=<URL_SAFE_BASE64_PAYLOAD>
```
When opened:
1. An `intent-filter` in `AndroidManifest.xml` catches `https://plugmeconnect.app/`.
2. `MainActivity.processDeepLink()` decodes the payload into a `Contact` object.
3. Contacts are deduplicated by username or name to prevent duplicates.
4. The intent data is cleared immediately to prevent re-adding on screen rotation.

---

## 👨‍💻 Credits

- **Product**: A MS Tech Product
- **Author**: Built by Shashant Madugundi, 17
- **Contact & Support**: `mspace119@gmail.com`
