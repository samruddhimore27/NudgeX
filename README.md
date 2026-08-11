# PrepX 🚀
> **Smart Competitive Programming & Task Planner App for Android**

PrepX is a modern, native Android application designed for students and competitive programmers to schedule study tasks, track daily active streaks, receive custom alarm notifications, and synchronize upcoming contests live from Codeforces, LeetCode, and CodeChef.

---

## 📱 Features

- **🏆 Live Contest Synchronization**: Automatically fetches upcoming competitive programming contests from Codeforces, LeetCode, and CodeChef APIs.
- **📅 Category Planner**: Schedule Contests, Classes, Exams, Tasks, and Goals using Google Material Date & 12-Hour Time Pickers.
- **⏰ Configurable Alarm System**: Custom wall-clock exact alarm reminders (**15m, 30m, 1h, 2h, 1d, At Event Time**) powered by Android `AlarmManager` and `BroadcastReceiver`.
- **🔥 Daily Streak Tracker**: Algorithmic streak counter with a 1-day grace period logic so missing a task today doesn't break yesterday's streak.
- **🔐 Cloud Authentication**: Firebase Email/Password signup and login with offline session persistence.
- **🌙 Dark Theme Toggle**: Right-aligned `MaterialSwitch` theme toggle with instant `SharedPreferences` persistence and Toast notifications.
- **📱 Clean Modern UI**: Built with Material Design 3 tokens, full-bleed splash screen, and responsive card layouts.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 1.9+
- **Architecture**: MVVM (Model-View-ViewModel) Pattern
- **UI Framework**: XML, Material Design 3, View Binding
- **Database**: Room Database (SQLite ORM) with Kotlin Flows
- **Asynchronous Logic**: Coroutines & `StateFlow`
- **Network Sync**: Retrofit 2 & Gson
- **Alarms & System Alerts**: `AlarmManager` (`RTC_WAKEUP`) & `NotificationChannel`
- **Backend Auth**: Firebase Auth SDK

---

## 🏗️ Project Structure

```text
com.example.prepx/
├── data/
│   ├── api/          # Retrofit REST API services (Codeforces, CodeChef)
│   ├── auth/         # Firebase Auth Repository & Local Session Manager
│   ├── db/           # Room Database, DAOs, & Type Converters
│   ├── model/        # Data Entities (PlannerItem, ItemType, PlatformType)
│   └── repository/   # Single Source of Truth Repository
├── reminder/         # AlarmScheduler & ReminderReceiver (BroadcastReceiver)
├── ui/
│   ├── auth/         # Login & Sign Up Activities
│   ├── contests/     # Contests List Fragment & Adapter
│   ├── dialog/       # AddTask BottomSheet Dialog & Pickers
│   ├── home/         # Dashboard Fragment & Planner Adapter
│   ├── main/         # MainActivity & Bottom Navigation
│   ├── profile/      # Profile & Theme Preference Fragment
│   └── splash/       # Fullscreen Splash Screen Activity
└── util/             # StreakCalculator & DateTimeUtils
```

---

## 🚀 Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/PrepX.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio $\rightarrow$ **Open An Existing Project** $\rightarrow$ Select `PrepX`.
3. **Build & Run**:
   - Sync Gradle and press **Run ▶️** targeting Android API 26+.

---

## 📄 License
Licensed under the [MIT License](LICENSE).
