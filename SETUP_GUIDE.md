# KRISHNA CONFIG — Complete Setup & Deployment Guide

## Overview
**KRISHNA CONFIG** is an end-to-end cyber-themed security & config management platform comprising:
1. **Android App**: Kotlin + Jetpack Compose with looping ExoPlayer background video, blood-red + neon green cyberpunk glassmorphism UI, Firebase Auth & Firestore sync, UPI payment slip upload, and Safe Zone management.
2. **Telegram Bot Admin**: Python (`bot.py`) and Node.js (`bot.js`) providing 100% remote admin control (approve/reject payments via inline buttons, change QR code, UPI ID, video background URLs, view live stats, manage users).
3. **Web Admin Dashboard**: Cyber-themed HTML + TailwindCSS + Firebase Web SDK dashboard hosted on Firebase Hosting.
4. **Cloud Functions**: Telegram webhook and automatic payment alert dispatch.

---

## 1. Connected Firebase Project Details
- **Project ID**: `screen-monitor-29225`
- **Project Number**: `361994075112`
- **Package Name**: `com.screen.monitor`
- **Database URL**: `https://screen-monitor-29225-default-rtdb.firebaseio.com`
- **Storage Bucket**: `screen-monitor-29225.firebasestorage.app`
- **API Key**: `AIzaSyAmOv_-ygzTiYJ3PqGg656VuXAh-7R2j_I`
- **Config File**: `/app/google-services.json` (created and linked)

---

## 2. Connected Telegram Bot Details
- **Bot Token**: `8831349456:AAGCVE9DfapAGcojAIv54C84cNY7A7uufF4`
- **Admin Chat ID**: `8491850372`
- Pre-filled in `.env`, `/backend/telegram_bot/bot.py`, `/backend/telegram_bot/bot.js`, `/admin_panel/app.js`, and the Android app `FirebaseRepository.kt`.

To run the Telegram Bot:
```bash
cd backend/telegram_bot
# Python:
pip install -r requirements.txt
python bot.py

# Or Node.js:
npm install
node bot.js
```

---

## 3. Telegram Admin Commands Reference

| Command | Action |
|---|---|
| `/start` | Open interactive control panel with quick action buttons |
| `/pending` | View queue of payment screenshots awaiting approval |
| `/setqr <url>` | Update payment QR code across all client apps in real-time |
| `/setupi <upi>` | Update UPI payment ID (default: `krishnaconfig@ybl`) |
| `/setamount <₹>` | Update activation price (default: `499`) |
| `/setvideo <urls>` | Update comma-separated list of background MP4 video URLs |
| `/setcontact <wa> <tg>`| Update WhatsApp and Telegram handles displayed in app |
| `/adduser <email> <pass>` | Manually create verified user account |
| `/deluser <email>` | Revoke and delete user record |
| `/listusers` | List registered client accounts and their bound rigs |
| `/blacklist <uid>` | Block specific hardware / player UID |
| `/removeblacklist <uid>`| Unban and restore access for player UID |
| `/antihack on\|off` | Globally toggle anti-hack integrity shield |
| `/maintenance on\|off`| Put Android app into maintenance lockdown |
| `/stats` | View real-time user registrations and approval rates |
| `/broadcast <msg>` | Push instant notification alert to all app users |

---

## 4. Web Admin Dashboard Hosting

To deploy the web admin dashboard to Firebase Hosting:
```bash
firebase init hosting
# Select existing project
# Public directory: admin_panel
firebase deploy --only hosting
```
Or open `admin_panel/index.html` directly in any web browser!

---

## 5. Android Application Architecture

- **`MainActivity.kt`**: Translucent root scaffold with `CyberBackgroundVideo` playing behind every screen.
- **`CyberBackgroundVideo.kt`**: Seamless looping video playback using AndroidX Media3 ExoPlayer with auto-playlist cycling.
- **`BloodDripOverlay.kt`**: Custom Compose `Canvas` blood dripping animation with neon sparks.
- **`CyberGlassCard.kt`**: 3D Cyberpunk glassmorphism card container with glowing borders.
- **`GlowingButton.kt`**: Pulsating high-intensity cyber action button.
- **`FirebaseRepository.kt`**: Handles Auth, Firestore real-time sync, Storage proof upload, and fallback Telegram Bot API integration.
- **`SecurityIntegrityHelper.kt`**: Root detection, test-key verification, and anti-tamper scanner.
