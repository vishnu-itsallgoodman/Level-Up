# Level Up — Android RPG Fitness & Habit Tracker

> "Complete your quests. Build your streak. Level up."

A completely offline Android application that turns your real-life habits and workouts into a personal RPG progression system.

---

## Running in Android Studio

### Requirements
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26+
- JDK 17

### Steps
1. Open Android Studio → **File → Open** → select the `LevelUp` folder.
2. Wait for Gradle sync to complete (first sync downloads ~500 MB of dependencies).
3. Connect an Android device (API 26+) or create an emulator.
4. Press the **Run** button (▶) or use **Run → Run 'app'**.

---

## Installing the APK on a Physical Device

1. Build the release APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
2. The APK appears at: `app/build/outputs/apk/debug/app-debug.apk`
3. Enable **Developer Options → USB Debugging** on your phone.
4. Connect via USB and run:
   ```
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   Or transfer the APK to your phone and open it with **Files**.

---

## Adding the Home-Screen Widget

1. Long-press an empty area on your Android home screen.
2. Tap **Widgets**.
3. Scroll to **Level Up**.
4. Long-press the widget and drag it to your home screen.
5. The widget shows your level, rank, streak, XP, and today's quest list.
6. It updates every 30 minutes automatically, and immediately when you complete a quest.
7. Tap the widget to open the app.

---

## Changing Workout Targets

### From the App
1. Open the app → tap **Settings** (bottom nav bar).
2. Under **WORKOUT TARGETS**, use the **−** and **+** buttons to adjust:
   - **Push-ups** target (default 50, steps of 5)
   - **Sit-ups** target (default 50, steps of 5)
   - **Crunches** target (default 50, steps of 5)

### In Code (for developers)
- **Default targets**: [`WorkoutType.kt`](app/src/main/java/com/levelup/app/domain/model/QuestModels.kt)
  ```kotlin
  enum class WorkoutType(val displayName: String, val defaultTarget: Int, val xpReward: Int) {
      PUSHUPS("Push-ups",  50, XpConfig.XP_PUSHUPS),
      SITUPS("Sit-ups",    50, XpConfig.XP_SITUPS),
      CRUNCHES("Crunches", 50, XpConfig.XP_CRUNCHES)
  }
  ```
- **XP rewards**: [`QuestModels.kt`](app/src/main/java/com/levelup/app/domain/model/QuestModels.kt)
  ```kotlin
  object XpConfig {
      const val XP_PUSHUPS    = 20
      const val XP_SITUPS     = 20
      const val XP_CRUNCHES   = 20
      const val XP_HABIT_DEFAULT = 10
      const val XP_DAILY_COMPLETE_BONUS = 15
  }
  ```

---

## XP and Level System

### How XP is Earned
| Action | XP |
|---|---|
| Complete push-up target | 20 XP |
| Complete sit-up target | 20 XP |
| Complete crunches target | 20 XP |
| Complete any habit | 10 XP (configurable per habit) |
| Complete ALL daily quests | +15 XP bonus |

XP is **not** awarded again if you uncheck and re-check the same quest. Unchecking reverses the XP.

### Level Formula
- **XP required for Level N → Level N+1** = `100 × N`
  - Level 1 → 2: 100 XP
  - Level 2 → 3: 200 XP
  - Level 3 → 4: 300 XP
  - ...and so on.

To change the formula, edit [`LevelConfig`](app/src/main/java/com/levelup/app/domain/model/PlayerModels.kt):
```kotlin
object LevelConfig {
    const val BASE_XP: Int = 100
    fun xpForLevel(level: Int): Int = BASE_XP * level
}
```

### Rank Thresholds
| Rank | Levels |
|---|---|
| E | 1–9 |
| D | 10–19 |
| C | 20–29 |
| B | 30–39 |
| A | 40–49 |
| S | 50+ |

To change thresholds, edit the `Rank` enum in [`PlayerModels.kt`](app/src/main/java/com/levelup/app/domain/model/PlayerModels.kt).

---

## Architecture

```
app/src/main/java/com/levelup/app/
├── data/
│   ├── database/         Room entities + DAOs + LevelUpDatabase
│   └── repository/       LevelUpRepository (single source of truth)
├── domain/
│   ├── model/            PlayerModels, QuestModels (XpConfig, WorkoutType, LevelConfig, Rank)
│   └── usecase/          QuestUseCases, StatsUseCases
├── ui/
│   ├── screens/          HomeScreen, HistoryScreen, StatsScreen, AchievementsScreen,
│   │                     SettingsScreen, OnboardingScreen — each with ViewModel
│   ├── components/       SharedComponents (SystemCard, XpProgressBar, RankBadge, StatBar)
│   ├── theme/            Color, Type, Theme
│   └── MainNavGraph.kt   Navigation host + bottom bar
├── widget/               LevelUpWidget (Glance), LevelUpWidgetReceiver, updateAllWidgets()
├── utils/                DateUtils, WidgetRefreshWorker
├── di/                   DatabaseModule (Hilt)
├── LevelUpApplication.kt
└── MainActivity.kt
```

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2025.02.00 | Compose UI framework |
| Compose Material3 | (BOM) | Material 3 dark theme |
| Navigation Compose | 2.8.9 | Screen navigation |
| Room | 2.7.0 | Local SQLite database |
| Hilt (Dagger) | 2.54 | Dependency injection |
| Hilt Navigation Compose | 1.2.0 | ViewModel injection in Compose |
| Jetpack Glance | 1.1.1 | Home-screen widget (Compose-based) |
| DataStore Preferences | 1.1.4 | Widget state storage |
| WorkManager | 2.10.0 | Background widget refresh |
| Hilt Work | 1.2.0 | Hilt + WorkManager integration |
| KSP | 2.1.0-1.0.29 | Kotlin Symbol Processing (Room + Hilt) |

No network libraries. No Firebase. No external image loading. Completely offline.

---

## First-Run Experience

On first launch:
1. **SYSTEM INITIALIZED** — welcome screen.
2. Configure workout targets (Push-ups, Sit-ups, Crunches).
3. Default habits are pre-loaded.
4. Press **BEGIN** → **ENTER THE SYSTEM** → main dashboard.

---

## Streak Rules

- A streak increments when **all active daily quests** are completed.
- A streak resets if a day is missed.
- The same day can only extend the streak once.
- Uses `LocalDate` for date logic — immune to timezone/timestamp drift.

---

## Data Persistence

All data is stored in a Room database (`levelup.db`) in the app's private storage. It survives:
- App restarts
- Device reboots
- Configuration changes

"Reset All Progress" in Settings wipes the database (with confirmation).
