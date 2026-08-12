# NudgeX

Your personal productivity assistant for staying on track with tasks, classes, and reminders.

---

## 📌 About the Project

**NudgeX** is a personal productivity assistant designed to help users stay on track with daily commitments, classes, assignments, and competitive programming contests.

- **The Problem**: Managing multiple daily responsibilities often leads to missed deadlines, forgotten lectures, or delayed tasks due to busy schedules.
- **The Solution**: NudgeX acts as a personal daily assistant by delivering timely, interactive "nudges" (reminders) right when action is needed.
- **Key Objective**: Simplify task tracking by enabling 1-tap notification bar actions (marking tasks complete or launching class meeting links) without friction.

---

## 🚀 Key Features

- **User Authentication**: Secure email and password authentication powered by Firebase Auth, preserving user sessions across app launches.
- **Task & Class Scheduling**: Create, edit, and delete entries categorized into **Classes**, **Contests**, **Exams**, **Tasks**, and **Goals**.
- **Flexible Repetition**: Configure one-off tasks or recurring weekly schedules with custom multi-day selection (e.g., Every Friday, Saturday, and Sunday).
- **Meeting & Contest Links**: Attach Zoom, Google Meet, or contest URLs to any item for direct 1-tap navigation.
- **Interactive Reminders & Exact Alarms**: Configurable alarm lead times (**15m, 30m, 1h, 2h, 1d, or At Event Time**) using Android `AlarmManager` and `BroadcastReceiver`.
- **System Notification Drawer Actions**:
  - **`✓ Mark as Done` / `✓ Mark Joined`**: Completes the item directly from your phone's notification bar and updates your streak without opening the app.
  - **`🎥 Join Class` / `🏆 Give Contest`**: Instantly launches the associated video meeting or contest link in your browser.
- **Competitive Programming Contest Explorer**: Live synchronization of upcoming contests from Codeforces, CodeChef, and LeetCode platforms with direct web links.
- **Daily Active Streak Counter**: Algorithmic streak tracker calculating consecutive active days for problem-solving and daily habits with grace period logic.
- **Offline Data Storage & Multi-Account Isolation**: Persistent local database built with Room DB and Kotlin Flow with `userId` isolation, ensuring task privacy and schedule restoration across user logins.
- **In-App Guide & Documentation**: Interactive bottom sheet guide and Profile information card explaining core use cases.
- **Dark Theme Support**: Material Design 3 light and dark themes with persistent preference toggling.

---

## 🔄 How It Works

1. **Sign In**: Log in or create an account using your full name and email.
2. **Add a Task or Class**: Tap the **+** button to create a task, specify its category, date, 12-hour time, repetition schedule, and meeting link.
3. **Explore Contests**: Browse live competitive programming contests from Codeforces, LeetCode, and CodeChef, and add them to your planner in one tap.
4. **Receive Timely Nudges**: At the scheduled reminder time, receive a loud notification featuring the NudgeX app symbol and actionable buttons.
5. **Take Immediate Action**: Tap **"Join Class"** to open your meeting or **"Mark as Done"** to log your progress and build your daily streak.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI & Layouts**: Android XML Layouts, Material Design 3, View Binding, ConstraintLayout
- **Architecture**: MVVM (Model-View-ViewModel) Pattern & Repository Pattern
- **Local Database**: Room Database (SQLite ORM) with Kotlin Coroutines & Flow
- **Authentication**: Firebase Authentication SDK
- **Network Sync**: Retrofit 2 & Gson Converter
- **Alarms & Notifications**: Android `AlarmManager` (`RTC_WAKEUP`), `BroadcastReceiver`, and `NotificationChannel`

---

## 🏗️ Project Structure

```text
app/src/main/
├── java/com/example/prepx/
│   ├── data/
│   │   ├── api/          # Retrofit API interfaces (Codeforces, CodeChef)
│   │   ├── auth/         # Firebase Auth Repository & Session Management
│   │   ├── db/           # Room Database, DAOs, and Type Converters
│   │   ├── model/        # Data Entities (PlannerItem, ItemType, RepeatType)
│   │   └── repository/   # Unified Data Repository
│   ├── reminder/         # AlarmScheduler & ReminderReceiver (BroadcastReceiver)
│   ├── ui/
│   │   ├── auth/         # LoginActivity & SignUpActivity
│   │   ├── contests/     # ContestsFragment & ContestsAdapter
│   │   ├── dialog/       # AddTaskBottomSheetDialog & AppGuideBottomSheetDialog
│   │   ├── home/         # HomeFragment & PlannerAdapter
│   │   ├── main/         # MainActivity & Navigation Setup
│   │   ├── profile/      # ProfileFragment & Preferences
│   │   └── splash/       # SplashActivity
│   └── util/             # StreakCalculator & DateTimeUtils
├── res/
│   ├── layout/           # XML Screen and Component Layouts
│   ├── mipmap/           # High-Res White Background Launcher Icons
│   └── values/           # Colors, Themes, and String Resources
└── AndroidManifest.xml
```

---

## ⚡ Installation & Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/NudgeX.git
   ```

2. **Open in Android Studio**:
   - Open Android Studio.
   - Select **Open** and navigate to the cloned `NudgeX` project directory.

3. **Configure Firebase Authentication**:
   - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with package name `com.example.prepx`.
   - Download your project's `google-services.json` file.
   - Place `google-services.json` inside the `app/` directory (`app/google-services.json`).
   - Enable **Email/Password** authentication in your Firebase Console.

4. **Sync & Run**:
   - Click **Sync Project with Gradle Files** in Android Studio.
   - Connect a physical Android device (Android 8.0+ / API 26+) or launch an Android Virtual Device (AVD).
   - Press **Run ▶** (Shift + F10).
